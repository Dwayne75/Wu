package org.fourthline.cling.transport.impl.jetty;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpMessage.BodyType;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.transport.spi.AbstractStreamClient;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.StreamClient;
import org.seamless.util.Exceptions;
import org.seamless.util.MimeType;

public class StreamClientImpl
  extends AbstractStreamClient<StreamClientConfigurationImpl, HttpContentExchange>
{
  private static final Logger log = Logger.getLogger(StreamClient.class.getName());
  protected final StreamClientConfigurationImpl configuration;
  protected final HttpClient client;
  
  public StreamClientImpl(StreamClientConfigurationImpl configuration)
    throws InitializationException
  {
    this.configuration = configuration;
    
    log.info("Starting Jetty HttpClient...");
    this.client = new HttpClient();
    
    this.client.setThreadPool(new ExecutorThreadPool(
      getConfiguration().getRequestExecutorService())
      {
        protected void doStop()
          throws Exception
        {}
      });
    this.client.setTimeout((configuration.getTimeoutSeconds() + 5) * 1000);
    this.client.setConnectTimeout((configuration.getTimeoutSeconds() + 5) * 1000);
    
    this.client.setMaxRetries(configuration.getRequestRetryCount());
    try
    {
      this.client.start();
    }
    catch (Exception ex)
    {
      throw new InitializationException("Could not start Jetty HTTP client: " + ex, ex);
    }
  }
  
  public StreamClientConfigurationImpl getConfiguration()
  {
    return this.configuration;
  }
  
  protected HttpContentExchange createRequest(StreamRequestMessage requestMessage)
  {
    return new HttpContentExchange(getConfiguration(), this.client, requestMessage);
  }
  
  protected Callable<StreamResponseMessage> createCallable(final StreamRequestMessage requestMessage, final HttpContentExchange exchange)
  {
    new Callable()
    {
      public StreamResponseMessage call()
        throws Exception
      {
        if (StreamClientImpl.log.isLoggable(Level.FINE)) {
          StreamClientImpl.log.fine("Sending HTTP request: " + requestMessage);
        }
        StreamClientImpl.this.client.send(exchange);
        int exchangeState = exchange.waitForDone();
        if (exchangeState == 7) {
          try
          {
            return exchange.createResponse();
          }
          catch (Throwable t)
          {
            StreamClientImpl.log.log(Level.WARNING, "Error reading response: " + requestMessage, Exceptions.unwrap(t));
            return null;
          }
        }
        if (exchangeState == 11) {
          return null;
        }
        if (exchangeState == 9) {
          return null;
        }
        StreamClientImpl.log.warning("Unhandled HTTP exchange status: " + exchangeState);
        return null;
      }
    };
  }
  
  protected void abort(HttpContentExchange exchange)
  {
    exchange.cancel();
  }
  
  protected boolean logExecutionException(Throwable t)
  {
    return false;
  }
  
  public void stop()
  {
    try
    {
      this.client.stop();
    }
    catch (Exception ex)
    {
      log.info("Error stopping HTTP client: " + ex);
    }
  }
  
  public static class HttpContentExchange
    extends ContentExchange
  {
    protected final StreamClientConfigurationImpl configuration;
    protected final HttpClient client;
    protected final StreamRequestMessage requestMessage;
    protected Throwable exception;
    
    public HttpContentExchange(StreamClientConfigurationImpl configuration, HttpClient client, StreamRequestMessage requestMessage)
    {
      super();
      this.configuration = configuration;
      this.client = client;
      this.requestMessage = requestMessage;
      applyRequestURLMethod();
      applyRequestHeaders();
      applyRequestBody();
    }
    
    protected void onConnectionFailed(Throwable t)
    {
      StreamClientImpl.log.log(Level.WARNING, "HTTP connection failed: " + this.requestMessage, Exceptions.unwrap(t));
    }
    
    protected void onException(Throwable t)
    {
      StreamClientImpl.log.log(Level.WARNING, "HTTP request failed: " + this.requestMessage, Exceptions.unwrap(t));
    }
    
    public StreamClientConfigurationImpl getConfiguration()
    {
      return this.configuration;
    }
    
    public StreamRequestMessage getRequestMessage()
    {
      return this.requestMessage;
    }
    
    protected void applyRequestURLMethod()
    {
      UpnpRequest requestOperation = (UpnpRequest)getRequestMessage().getOperation();
      if (StreamClientImpl.log.isLoggable(Level.FINE)) {
        StreamClientImpl.log.fine("Preparing HTTP request message with method '" + requestOperation
        
          .getHttpMethodName() + "': " + 
          getRequestMessage());
      }
      setURL(requestOperation.getURI().toString());
      setMethod(requestOperation.getHttpMethodName());
    }
    
    protected void applyRequestHeaders()
    {
      UpnpHeaders headers = getRequestMessage().getHeaders();
      if (StreamClientImpl.log.isLoggable(Level.FINE)) {
        StreamClientImpl.log.fine("Writing headers on HttpContentExchange: " + headers.size());
      }
      if (!headers.containsKey(UpnpHeader.Type.USER_AGENT)) {
        setRequestHeader(UpnpHeader.Type.USER_AGENT
          .getHttpName(), 
          getConfiguration().getUserAgentValue(
          getRequestMessage().getUdaMajorVersion(), 
          getRequestMessage().getUdaMinorVersion()));
      }
      for (Iterator localIterator1 = headers.entrySet().iterator(); localIterator1.hasNext();)
      {
        entry = (Map.Entry)localIterator1.next();
        for (String v : (List)entry.getValue())
        {
          String headerName = (String)entry.getKey();
          if (StreamClientImpl.log.isLoggable(Level.FINE)) {
            StreamClientImpl.log.fine("Setting header '" + headerName + "': " + v);
          }
          addRequestHeader(headerName, v);
        }
      }
      Map.Entry<String, List<String>> entry;
    }
    
    protected void applyRequestBody()
    {
      if (getRequestMessage().hasBody()) {
        if (getRequestMessage().getBodyType() == UpnpMessage.BodyType.STRING)
        {
          if (StreamClientImpl.log.isLoggable(Level.FINE)) {
            StreamClientImpl.log.fine("Writing textual request body: " + getRequestMessage());
          }
          MimeType contentType = getRequestMessage().getContentTypeHeader() != null ? (MimeType)getRequestMessage().getContentTypeHeader().getValue() : ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8;
          
          String charset = getRequestMessage().getContentTypeCharset() != null ? getRequestMessage().getContentTypeCharset() : "UTF-8";
          
          setRequestContentType(contentType.toString());
          try
          {
            buffer = new ByteArrayBuffer(getRequestMessage().getBodyString(), charset);
          }
          catch (UnsupportedEncodingException ex)
          {
            ByteArrayBuffer buffer;
            throw new RuntimeException("Unsupported character encoding: " + charset, ex);
          }
          ByteArrayBuffer buffer;
          setRequestHeader("Content-Length", String.valueOf(buffer.length()));
          setRequestContent(buffer);
        }
        else
        {
          if (StreamClientImpl.log.isLoggable(Level.FINE)) {
            StreamClientImpl.log.fine("Writing binary request body: " + getRequestMessage());
          }
          if (getRequestMessage().getContentTypeHeader() == null) {
            throw new RuntimeException("Missing content type header in request message: " + this.requestMessage);
          }
          MimeType contentType = (MimeType)getRequestMessage().getContentTypeHeader().getValue();
          
          setRequestContentType(contentType.toString());
          
          ByteArrayBuffer buffer = new ByteArrayBuffer(getRequestMessage().getBodyBytes());
          setRequestHeader("Content-Length", String.valueOf(buffer.length()));
          setRequestContent(buffer);
        }
      }
    }
    
    protected StreamResponseMessage createResponse()
    {
      UpnpResponse responseOperation = new UpnpResponse(getResponseStatus(), UpnpResponse.Status.getByStatusCode(getResponseStatus()).getStatusMsg());
      if (StreamClientImpl.log.isLoggable(Level.FINE)) {
        StreamClientImpl.log.fine("Received response: " + responseOperation);
      }
      StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);
      
      UpnpHeaders headers = new UpnpHeaders();
      HttpFields responseFields = getResponseFields();
      for (Iterator localIterator1 = responseFields.getFieldNamesCollection().iterator(); localIterator1.hasNext();)
      {
        name = (String)localIterator1.next();
        for (String value : responseFields.getValuesCollection(name)) {
          headers.add(name, value);
        }
      }
      String name;
      responseMessage.setHeaders(headers);
      
      byte[] bytes = getResponseContentBytes();
      if ((bytes != null) && (bytes.length > 0) && (responseMessage.isContentTypeMissingOrText()))
      {
        if (StreamClientImpl.log.isLoggable(Level.FINE)) {
          StreamClientImpl.log.fine("Response contains textual entity body, converting then setting string on message");
        }
        try
        {
          responseMessage.setBodyCharacters(bytes);
        }
        catch (UnsupportedEncodingException ex)
        {
          throw new RuntimeException("Unsupported character encoding: " + ex, ex);
        }
      }
      else if ((bytes != null) && (bytes.length > 0))
      {
        if (StreamClientImpl.log.isLoggable(Level.FINE)) {
          StreamClientImpl.log.fine("Response contains binary entity body, setting bytes on message");
        }
        responseMessage.setBody(UpnpMessage.BodyType.BYTES, bytes);
      }
      else if (StreamClientImpl.log.isLoggable(Level.FINE))
      {
        StreamClientImpl.log.fine("Response did not contain entity body");
      }
      if (StreamClientImpl.log.isLoggable(Level.FINE)) {
        StreamClientImpl.log.fine("Response message complete: " + responseMessage);
      }
      return responseMessage;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\jetty\StreamClientImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */