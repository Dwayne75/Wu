package javax.servlet;

import java.util.EventListener;

public abstract interface ServletRequestAttributeListener
  extends EventListener
{
  public abstract void attributeAdded(ServletRequestAttributeEvent paramServletRequestAttributeEvent);
  
  public abstract void attributeRemoved(ServletRequestAttributeEvent paramServletRequestAttributeEvent);
  
  public abstract void attributeReplaced(ServletRequestAttributeEvent paramServletRequestAttributeEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletRequestAttributeListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */