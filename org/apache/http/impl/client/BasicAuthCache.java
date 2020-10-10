package org.apache.http.impl.client;

import java.util.HashMap;
import org.apache.http.HttpHost;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.AuthScheme;
import org.apache.http.client.AuthCache;

@NotThreadSafe
public class BasicAuthCache
  implements AuthCache
{
  private final HashMap<HttpHost, AuthScheme> map;
  
  public BasicAuthCache()
  {
    this.map = new HashMap();
  }
  
  protected HttpHost getKey(HttpHost host)
  {
    if (host.getPort() <= 0)
    {
      int port = host.getSchemeName().equalsIgnoreCase("https") ? 443 : 80;
      return new HttpHost(host.getHostName(), port, host.getSchemeName());
    }
    return host;
  }
  
  public void put(HttpHost host, AuthScheme authScheme)
  {
    if (host == null) {
      throw new IllegalArgumentException("HTTP host may not be null");
    }
    this.map.put(getKey(host), authScheme);
  }
  
  public AuthScheme get(HttpHost host)
  {
    if (host == null) {
      throw new IllegalArgumentException("HTTP host may not be null");
    }
    return (AuthScheme)this.map.get(getKey(host));
  }
  
  public void remove(HttpHost host)
  {
    if (host == null) {
      throw new IllegalArgumentException("HTTP host may not be null");
    }
    this.map.remove(getKey(host));
  }
  
  public void clear()
  {
    this.map.clear();
  }
  
  public String toString()
  {
    return this.map.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\client\BasicAuthCache.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */