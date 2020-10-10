package org.apache.http.client;

import java.util.Date;
import java.util.List;
import org.apache.http.cookie.Cookie;

public abstract interface CookieStore
{
  public abstract void addCookie(Cookie paramCookie);
  
  public abstract List<Cookie> getCookies();
  
  public abstract boolean clearExpired(Date paramDate);
  
  public abstract void clear();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\CookieStore.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */