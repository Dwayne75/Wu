package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
public class HttpPatch
  extends HttpEntityEnclosingRequestBase
{
  public static final String METHOD_NAME = "PATCH";
  
  public HttpPatch() {}
  
  public HttpPatch(URI uri)
  {
    setURI(uri);
  }
  
  public HttpPatch(String uri)
  {
    setURI(URI.create(uri));
  }
  
  public String getMethod()
  {
    return "PATCH";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\methods\HttpPatch.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */