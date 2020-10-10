package org.fourthline.cling.transport.impl;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.Connection;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpMessage.BodyType;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpRequest.Method;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.spi.UpnpStream;
import org.seamless.util.Exceptions;
import org.seamless.util.io.IO;

public abstract class HttpExchangeUpnpStream
  extends UpnpStream
{
  private static Logger log = Logger.getLogger(UpnpStream.class.getName());
  private HttpExchange httpExchange;
  
  public HttpExchangeUpnpStream(ProtocolFactory protocolFactory, HttpExchange httpExchange)
  {
    super(protocolFactory);
    this.httpExchange = httpExchange;
  }
  
  public HttpExchange getHttpExchange()
  {
    return this.httpExchange;
  }
  
  public void run()
  {
    try
    {
      log.fine("Processing HTTP request: " + getHttpExchange().getRequestMethod() + " " + getHttpExchange().getRequestURI());
      
      StreamRequestMessage requestMessage = new StreamRequestMessage(UpnpRequest.Method.getByHttpName(getHttpExchange().getRequestMethod()), getHttpExchange().getRequestURI());
      if (((UpnpRequest)requestMessage.getOperation()).getMethod().equals(UpnpRequest.Method.UNKNOWN))
      {
        log.fine("Method not supported by UPnP stack: " + getHttpExchange().getRequestMethod());
        throw new RuntimeException("Method not supported: " + getHttpExchange().getRequestMethod());
      }
      ((UpnpRequest)requestMessage.getOperation()).setHttpMinorVersion(
        getHttpExchange().getProtocol().toUpperCase(Locale.ROOT).equals("HTTP/1.1") ? 1 : 0);
      
      log.fine("Created new request message: " + requestMessage);
      
      requestMessage.setConnection(createConnection());
      
      requestMessage.setHeaders(new UpnpHeaders(getHttpExchange().getRequestHeaders()));
      
      InputStream is = null;
      byte[] bodyBytes;
      try
      {
        is = getHttpExchange().getRequestBody();
        bodyBytes = IO.readBytes(is);
      }
      finally
      {
        if (is != null) {
          is.close();
        }
      }
      log.fine("Reading request body bytes: " + bodyBytes.length);
      if ((bodyBytes.length > 0) && (requestMessage.isContentTypeMissingOrText()))
      {
        log.fine("Request contains textual entity body, converting then setting string on message");
        requestMessage.setBodyCharacters(bodyBytes);
      }
      else if (bodyBytes.length > 0)
      {
        log.fine("Request contains binary entity body, setting bytes on message");
        requestMessage.setBody(UpnpMessage.BodyType.BYTES, bodyBytes);
      }
      else
      {
        log.fine("Request did not contain entity body");
      }
      StreamResponseMessage responseMessage = process(requestMessage);
      if (responseMessage != null)
      {
        log.fine("Preparing HTTP response message: " + responseMessage);
        
        getHttpExchange().getResponseHeaders().putAll(responseMessage
          .getHeaders());
        
        byte[] responseBodyBytes = responseMessage.hasBody() ? responseMessage.getBodyBytes() : null;
        int contentLength = responseBodyBytes != null ? responseBodyBytes.length : -1;
        
        log.fine("Sending HTTP response message: " + responseMessage + " with content length: " + contentLength);
        getHttpExchange().sendResponseHeaders(((UpnpResponse)responseMessage.getOperation()).getStatusCode(), contentLength);
        if (contentLength > 0)
        {
          log.fine("Response message has body, writing bytes to stream...");
          OutputStream os = null;
          try
          {
            os = getHttpExchange().getResponseBody();
            IO.writeBytes(os, responseBodyBytes);
            os.flush();
          }
          finally
          {
            if (os != null) {
              os.close();
            }
          }
        }
      }
      else
      {
        log.fine("Sending HTTP response status: 404");
        getHttpExchange().sendResponseHeaders(404, -1L);
      }
      responseSent(responseMessage);
    }
    catch (Throwable t)
    {
      log.fine("Exception occured during UPnP stream processing: " + t);
      if (log.isLoggable(Level.FINE)) {
        log.log(Level.FINE, "Cause: " + Exceptions.unwrap(t), Exceptions.unwrap(t));
      }
      try
      {
        this.httpExchange.sendResponseHeaders(500, -1L);
      }
      catch (IOException ex)
      {
        log.warning("Couldn't send error response: " + ex);
      }
      responseException(t);
    }
  }
  
  protected abstract Connection createConnection();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\HttpExchangeUpnpStream.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */