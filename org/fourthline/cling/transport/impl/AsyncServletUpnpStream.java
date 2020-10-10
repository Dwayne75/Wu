package org.fourthline.cling.transport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

public abstract class AsyncServletUpnpStream
  extends UpnpStream
  implements AsyncListener
{
  private static final Logger log = Logger.getLogger(UpnpStream.class.getName());
  protected final AsyncContext asyncContext;
  protected final HttpServletRequest request;
  protected StreamResponseMessage responseMessage;
  
  public AsyncServletUpnpStream(ProtocolFactory protocolFactory, AsyncContext asyncContext, HttpServletRequest request)
  {
    super(protocolFactory);
    this.asyncContext = asyncContext;
    this.request = request;
    asyncContext.addListener(this);
  }
  
  protected HttpServletRequest getRequest()
  {
    return this.request;
  }
  
  protected HttpServletResponse getResponse()
  {
    ServletResponse response;
    if ((response = this.asyncContext.getResponse()) == null) {
      throw new IllegalStateException("Couldn't get response from asynchronous context, already timed out");
    }
    return (HttpServletResponse)response;
  }
  
  protected void complete()
  {
    try
    {
      this.asyncContext.complete();
    }
    catch (IllegalStateException ex)
    {
      log.info("Error calling servlet container's AsyncContext#complete() method: " + ex);
    }
  }
  
  public void run()
  {
    try
    {
      StreamRequestMessage requestMessage = readRequestMessage();
      if (log.isLoggable(Level.FINER)) {
        log.finer("Processing new request message: " + requestMessage);
      }
      this.responseMessage = process(requestMessage);
      if (this.responseMessage != null)
      {
        if (log.isLoggable(Level.FINER)) {
          log.finer("Preparing HTTP response message: " + this.responseMessage);
        }
        writeResponseMessage(this.responseMessage);
      }
      else
      {
        if (log.isLoggable(Level.FINER)) {
          log.finer("Sending HTTP response status: 404");
        }
        getResponse().setStatus(404);
      }
    }
    catch (Throwable t)
    {
      log.info("Exception occurred during UPnP stream processing: " + t);
      if (log.isLoggable(Level.FINER)) {
        log.log(Level.FINER, "Cause: " + Exceptions.unwrap(t), Exceptions.unwrap(t));
      }
      if (!getResponse().isCommitted())
      {
        log.finer("Response hasn't been committed, returning INTERNAL SERVER ERROR to client");
        getResponse().setStatus(500);
      }
      else
      {
        log.info("Could not return INTERNAL SERVER ERROR to client, response was already committed");
      }
      responseException(t);
    }
    finally
    {
      complete();
    }
  }
  
  public void onStartAsync(AsyncEvent event)
    throws IOException
  {}
  
  public void onComplete(AsyncEvent event)
    throws IOException
  {
    if (log.isLoggable(Level.FINER)) {
      log.finer("Completed asynchronous processing of HTTP request: " + event.getSuppliedRequest());
    }
    responseSent(this.responseMessage);
  }
  
  public void onTimeout(AsyncEvent event)
    throws IOException
  {
    if (log.isLoggable(Level.FINER)) {
      log.finer("Asynchronous processing of HTTP request timed out: " + event.getSuppliedRequest());
    }
    responseException(new Exception("Asynchronous request timed out"));
  }
  
  public void onError(AsyncEvent event)
    throws IOException
  {
    if (log.isLoggable(Level.FINER)) {
      log.finer("Asynchronous processing of HTTP request error: " + event.getThrowable());
    }
    responseException(event.getThrowable());
  }
  
  protected StreamRequestMessage readRequestMessage()
    throws IOException
  {
    String requestMethod = getRequest().getMethod();
    String requestURI = getRequest().getRequestURI();
    if (log.isLoggable(Level.FINER)) {
      log.finer("Processing HTTP request: " + requestMethod + " " + requestURI);
    }
    try
    {
      requestMessage = new StreamRequestMessage(UpnpRequest.Method.getByHttpName(requestMethod), URI.create(requestURI));
    }
    catch (IllegalArgumentException ex)
    {
      StreamRequestMessage requestMessage;
      throw new RuntimeException("Invalid request URI: " + requestURI, ex);
    }
    StreamRequestMessage requestMessage;
    if (((UpnpRequest)requestMessage.getOperation()).getMethod().equals(UpnpRequest.Method.UNKNOWN)) {
      throw new RuntimeException("Method not supported: " + requestMethod);
    }
    requestMessage.setConnection(createConnection());
    
    UpnpHeaders headers = new UpnpHeaders();
    Enumeration<String> headerNames = getRequest().getHeaderNames();
    while (headerNames.hasMoreElements())
    {
      String headerName = (String)headerNames.nextElement();
      Enumeration<String> headerValues = getRequest().getHeaders(headerName);
      while (headerValues.hasMoreElements())
      {
        String headerValue = (String)headerValues.nextElement();
        headers.add(headerName, headerValue);
      }
    }
    requestMessage.setHeaders(headers);
    
    InputStream is = null;
    byte[] bodyBytes;
    try
    {
      is = getRequest().getInputStream();
      bodyBytes = IO.readBytes(is);
    }
    finally
    {
      if (is != null) {
        is.close();
      }
    }
    if (log.isLoggable(Level.FINER)) {
      log.finer("Reading request body bytes: " + bodyBytes.length);
    }
    if ((bodyBytes.length > 0) && (requestMessage.isContentTypeMissingOrText()))
    {
      if (log.isLoggable(Level.FINER)) {
        log.finer("Request contains textual entity body, converting then setting string on message");
      }
      requestMessage.setBodyCharacters(bodyBytes);
    }
    else if (bodyBytes.length > 0)
    {
      if (log.isLoggable(Level.FINER)) {
        log.finer("Request contains binary entity body, setting bytes on message");
      }
      requestMessage.setBody(UpnpMessage.BodyType.BYTES, bodyBytes);
    }
    else if (log.isLoggable(Level.FINER))
    {
      log.finer("Request did not contain entity body");
    }
    return requestMessage;
  }
  
  protected void writeResponseMessage(StreamResponseMessage responseMessage)
    throws IOException
  {
    if (log.isLoggable(Level.FINER)) {
      log.finer("Sending HTTP response status: " + ((UpnpResponse)responseMessage.getOperation()).getStatusCode());
    }
    getResponse().setStatus(((UpnpResponse)responseMessage.getOperation()).getStatusCode());
    for (Iterator localIterator1 = responseMessage.getHeaders().entrySet().iterator(); localIterator1.hasNext();)
    {
      entry = (Map.Entry)localIterator1.next();
      for (String value : (List)entry.getValue()) {
        getResponse().addHeader((String)entry.getKey(), value);
      }
    }
    Map.Entry<String, List<String>> entry;
    getResponse().setDateHeader("Date", System.currentTimeMillis());
    
    byte[] responseBodyBytes = responseMessage.hasBody() ? responseMessage.getBodyBytes() : null;
    int contentLength = responseBodyBytes != null ? responseBodyBytes.length : -1;
    if (contentLength > 0)
    {
      getResponse().setContentLength(contentLength);
      log.finer("Response message has body, writing bytes to stream...");
      IO.writeBytes(getResponse().getOutputStream(), responseBodyBytes);
    }
  }
  
  protected abstract Connection createConnection();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\AsyncServletUpnpStream.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */