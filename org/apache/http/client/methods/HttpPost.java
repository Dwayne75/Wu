package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
public class HttpPost
  extends HttpEntityEnclosingRequestBase
{
  public static final String METHOD_NAME = "POST";
  
  public HttpPost() {}
  
  public HttpPost(URI uri)
  {
    setURI(uri);
  }
  
  public HttpPost(String uri)
  {
    setURI(URI.create(uri));
  }
  
  public String getMethod()
  {
    return "POST";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\methods\HttpPost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */