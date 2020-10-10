package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
public class HttpTrace
  extends HttpRequestBase
{
  public static final String METHOD_NAME = "TRACE";
  
  public HttpTrace() {}
  
  public HttpTrace(URI uri)
  {
    setURI(uri);
  }
  
  public HttpTrace(String uri)
  {
    setURI(URI.create(uri));
  }
  
  public String getMethod()
  {
    return "TRACE";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\methods\HttpTrace.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */