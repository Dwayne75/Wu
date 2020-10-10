package org.apache.http.client;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;

public abstract interface AuthCache
{
  public abstract void put(HttpHost paramHttpHost, AuthScheme paramAuthScheme);
  
  public abstract AuthScheme get(HttpHost paramHttpHost);
  
  public abstract void remove(HttpHost paramHttpHost);
  
  public abstract void clear();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\AuthCache.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */