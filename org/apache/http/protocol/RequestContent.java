package org.apache.http.protocol;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.annotation.Immutable;

@Immutable
public class RequestContent
  implements HttpRequestInterceptor
{
  private final boolean overwrite;
  
  public RequestContent()
  {
    this(false);
  }
  
  public RequestContent(boolean overwrite)
  {
    this.overwrite = overwrite;
  }
  
  public void process(HttpRequest request, HttpContext context)
    throws HttpException, IOException
  {
    if (request == null) {
      throw new IllegalArgumentException("HTTP request may not be null");
    }
    if ((request instanceof HttpEntityEnclosingRequest))
    {
      if (this.overwrite)
      {
        request.removeHeaders("Transfer-Encoding");
        request.removeHeaders("Content-Length");
      }
      else
      {
        if (request.containsHeader("Transfer-Encoding")) {
          throw new ProtocolException("Transfer-encoding header already present");
        }
        if (request.containsHeader("Content-Length")) {
          throw new ProtocolException("Content-Length header already present");
        }
      }
      ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
      HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
      if (entity == null)
      {
        request.addHeader("Content-Length", "0");
        return;
      }
      if ((entity.isChunked()) || (entity.getContentLength() < 0L))
      {
        if (ver.lessEquals(HttpVersion.HTTP_1_0)) {
          throw new ProtocolException("Chunked transfer encoding not allowed for " + ver);
        }
        request.addHeader("Transfer-Encoding", "chunked");
      }
      else
      {
        request.addHeader("Content-Length", Long.toString(entity.getContentLength()));
      }
      if ((entity.getContentType() != null) && (!request.containsHeader("Content-Type"))) {
        request.addHeader(entity.getContentType());
      }
      if ((entity.getContentEncoding() != null) && (!request.containsHeader("Content-Encoding"))) {
        request.addHeader(entity.getContentEncoding());
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\protocol\RequestContent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */