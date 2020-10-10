package org.apache.http.protocol;

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.annotation.Immutable;

@Immutable
public class ResponseConnControl
  implements HttpResponseInterceptor
{
  public void process(HttpResponse response, HttpContext context)
    throws HttpException, IOException
  {
    if (response == null) {
      throw new IllegalArgumentException("HTTP response may not be null");
    }
    if (context == null) {
      throw new IllegalArgumentException("HTTP context may not be null");
    }
    int status = response.getStatusLine().getStatusCode();
    if ((status == 400) || (status == 408) || (status == 411) || (status == 413) || (status == 414) || (status == 503) || (status == 501))
    {
      response.setHeader("Connection", "Close");
      return;
    }
    Header explicit = response.getFirstHeader("Connection");
    if ((explicit != null) && ("Close".equalsIgnoreCase(explicit.getValue()))) {
      return;
    }
    HttpEntity entity = response.getEntity();
    if (entity != null)
    {
      ProtocolVersion ver = response.getStatusLine().getProtocolVersion();
      if ((entity.getContentLength() < 0L) && ((!entity.isChunked()) || (ver.lessEquals(HttpVersion.HTTP_1_0))))
      {
        response.setHeader("Connection", "Close");
        return;
      }
    }
    HttpRequest request = (HttpRequest)context.getAttribute("http.request");
    if (request != null)
    {
      Header header = request.getFirstHeader("Connection");
      if (header != null) {
        response.setHeader("Connection", header.getValue());
      } else if (request.getProtocolVersion().lessEquals(HttpVersion.HTTP_1_0)) {
        response.setHeader("Connection", "Close");
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\protocol\ResponseConnControl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */