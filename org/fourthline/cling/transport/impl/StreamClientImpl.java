package org.fourthline.cling.transport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpMessage.BodyType;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.StreamClient;
import org.seamless.http.Headers;
import org.seamless.util.Exceptions;
import org.seamless.util.URIUtil;
import org.seamless.util.io.IO;

public class StreamClientImpl
  implements StreamClient
{
  static final String HACK_STREAM_HANDLER_SYSTEM_PROPERTY = "hackStreamHandlerProperty";
  private static final Logger log = Logger.getLogger(StreamClient.class.getName());
  protected final StreamClientConfigurationImpl configuration;
  
  public StreamClientImpl(StreamClientConfigurationImpl configuration)
    throws InitializationException
  {
    this.configuration = configuration;
    if ((ModelUtil.ANDROID_EMULATOR) || (ModelUtil.ANDROID_RUNTIME)) {
      throw new InitializationException("This client does not work on Android. The design of HttpURLConnection is broken, we can not add additional 'permitted' HTTP methods. Read the Cling manual.");
    }
    log.fine("Using persistent HTTP stream client connections: " + configuration.isUsePersistentConnections());
    System.setProperty("http.keepAlive", Boolean.toString(configuration.isUsePersistentConnections()));
    if (System.getProperty("hackStreamHandlerProperty") == null)
    {
      log.fine("Setting custom static URLStreamHandlerFactory to work around bad JDK defaults");
      try
      {
        URL.setURLStreamHandlerFactory(
        
          (URLStreamHandlerFactory)Class.forName("org.fourthline.cling.transport.impl.FixedSunURLStreamHandler").newInstance());
      }
      catch (Throwable t)
      {
        throw new InitializationException("Failed to set modified URLStreamHandlerFactory in this environment. Can't use bundled default client based on HTTPURLConnection, see manual.");
      }
      System.setProperty("hackStreamHandlerProperty", "alreadyWorkedAroundTheEvilJDK");
    }
  }
  
  public StreamClientConfigurationImpl getConfiguration()
  {
    return this.configuration;
  }
  
  public StreamResponseMessage sendRequest(StreamRequestMessage requestMessage)
  {
    UpnpRequest requestOperation = (UpnpRequest)requestMessage.getOperation();
    log.fine("Preparing HTTP request message with method '" + requestOperation.getHttpMethodName() + "': " + requestMessage);
    
    URL url = URIUtil.toURL(requestOperation.getURI());
    
    HttpURLConnection urlConnection = null;
    try
    {
      urlConnection = (HttpURLConnection)url.openConnection();
      
      urlConnection.setRequestMethod(requestOperation.getHttpMethodName());
      
      urlConnection.setReadTimeout(this.configuration.getTimeoutSeconds() * 1000);
      urlConnection.setConnectTimeout(this.configuration.getTimeoutSeconds() * 1000);
      
      applyRequestProperties(urlConnection, requestMessage);
      applyRequestBody(urlConnection, requestMessage);
      
      log.fine("Sending HTTP request: " + requestMessage);
      InputStream inputStream = urlConnection.getInputStream();
      return createResponse(urlConnection, inputStream);
    }
    catch (ProtocolException ex)
    {
      log.log(Level.WARNING, "HTTP request failed: " + requestMessage, Exceptions.unwrap(ex));
      return null;
    }
    catch (IOException ex)
    {
      StreamResponseMessage localStreamResponseMessage2;
      if (urlConnection == null)
      {
        log.log(Level.WARNING, "HTTP request failed: " + requestMessage, Exceptions.unwrap(ex));
        return null;
      }
      if ((ex instanceof SocketTimeoutException))
      {
        log.info("Timeout of " + 
          getConfiguration().getTimeoutSeconds() + " seconds while waiting for HTTP request to complete, aborting: " + requestMessage);
        
        return null;
      }
      if (log.isLoggable(Level.FINE)) {
        log.fine("Exception occurred, trying to read the error stream: " + Exceptions.unwrap(ex));
      }
      try
      {
        InputStream inputStream = urlConnection.getErrorStream();
        return createResponse(urlConnection, inputStream);
      }
      catch (Exception errorEx)
      {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Could not read error stream: " + errorEx);
        }
        return null;
      }
    }
    catch (Exception ex)
    {
      log.log(Level.WARNING, "HTTP request failed: " + requestMessage, Exceptions.unwrap(ex));
      return null;
    }
    finally
    {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }
  }
  
  public void stop() {}
  
  protected void applyRequestProperties(HttpURLConnection urlConnection, StreamRequestMessage requestMessage)
  {
    urlConnection.setInstanceFollowRedirects(false);
    if (!requestMessage.getHeaders().containsKey(UpnpHeader.Type.USER_AGENT)) {
      urlConnection.setRequestProperty(UpnpHeader.Type.USER_AGENT
        .getHttpName(), 
        getConfiguration().getUserAgentValue(requestMessage.getUdaMajorVersion(), requestMessage.getUdaMinorVersion()));
    }
    applyHeaders(urlConnection, requestMessage.getHeaders());
  }
  
  protected void applyHeaders(HttpURLConnection urlConnection, Headers headers)
  {
    log.fine("Writing headers on HttpURLConnection: " + headers.size());
    for (Iterator localIterator1 = headers.entrySet().iterator(); localIterator1.hasNext();)
    {
      entry = (Map.Entry)localIterator1.next();
      for (String v : (List)entry.getValue())
      {
        String headerName = (String)entry.getKey();
        log.fine("Setting header '" + headerName + "': " + v);
        urlConnection.setRequestProperty(headerName, v);
      }
    }
    Map.Entry<String, List<String>> entry;
  }
  
  protected void applyRequestBody(HttpURLConnection urlConnection, StreamRequestMessage requestMessage)
    throws IOException
  {
    if (requestMessage.hasBody())
    {
      urlConnection.setDoOutput(true);
    }
    else
    {
      urlConnection.setDoOutput(false);
      return;
    }
    if (requestMessage.getBodyType().equals(UpnpMessage.BodyType.STRING)) {
      IO.writeUTF8(urlConnection.getOutputStream(), requestMessage.getBodyString());
    } else if (requestMessage.getBodyType().equals(UpnpMessage.BodyType.BYTES)) {
      IO.writeBytes(urlConnection.getOutputStream(), requestMessage.getBodyBytes());
    }
    urlConnection.getOutputStream().flush();
  }
  
  protected StreamResponseMessage createResponse(HttpURLConnection urlConnection, InputStream inputStream)
    throws Exception
  {
    if (urlConnection.getResponseCode() == -1)
    {
      log.warning("Received an invalid HTTP response: " + urlConnection.getURL());
      log.warning("Is your Cling-based server sending connection heartbeats with RemoteClientInfo#isRequestCancelled? This client can't handle heartbeats, read the manual.");
      
      return null;
    }
    UpnpResponse responseOperation = new UpnpResponse(urlConnection.getResponseCode(), urlConnection.getResponseMessage());
    
    log.fine("Received response: " + responseOperation);
    
    StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);
    
    responseMessage.setHeaders(new UpnpHeaders(urlConnection.getHeaderFields()));
    
    byte[] bodyBytes = null;
    InputStream is = null;
    try
    {
      is = inputStream;
      if (inputStream != null) {
        bodyBytes = IO.readBytes(is);
      }
    }
    finally
    {
      if (is != null) {
        is.close();
      }
    }
    if ((bodyBytes != null) && (bodyBytes.length > 0) && (responseMessage.isContentTypeMissingOrText()))
    {
      log.fine("Response contains textual entity body, converting then setting string on message");
      responseMessage.setBodyCharacters(bodyBytes);
    }
    else if ((bodyBytes != null) && (bodyBytes.length > 0))
    {
      log.fine("Response contains binary entity body, setting bytes on message");
      responseMessage.setBody(UpnpMessage.BodyType.BYTES, bodyBytes);
    }
    else
    {
      log.fine("Response did not contain entity body");
    }
    log.fine("Response message complete: " + responseMessage);
    return responseMessage;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\StreamClientImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */