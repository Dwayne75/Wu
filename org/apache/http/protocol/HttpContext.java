package org.apache.http.protocol;

public abstract interface HttpContext
{
  public static final String RESERVED_PREFIX = "http.";
  
  public abstract Object getAttribute(String paramString);
  
  public abstract void setAttribute(String paramString, Object paramObject);
  
  public abstract Object removeAttribute(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\protocol\HttpContext.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */