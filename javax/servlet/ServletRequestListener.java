package javax.servlet;

import java.util.EventListener;

public abstract interface ServletRequestListener
  extends EventListener
{
  public abstract void requestDestroyed(ServletRequestEvent paramServletRequestEvent);
  
  public abstract void requestInitialized(ServletRequestEvent paramServletRequestEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletRequestListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */