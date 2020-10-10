package javax.servlet.http;

import java.util.Enumeration;

/**
 * @deprecated
 */
public abstract interface HttpSessionContext
{
  /**
   * @deprecated
   */
  public abstract HttpSession getSession(String paramString);
  
  /**
   * @deprecated
   */
  public abstract Enumeration<String> getIds();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\http\HttpSessionContext.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */