package javax.servlet;

import java.util.EventListener;

public abstract interface ServletContextAttributeListener
  extends EventListener
{
  public abstract void attributeAdded(ServletContextAttributeEvent paramServletContextAttributeEvent);
  
  public abstract void attributeRemoved(ServletContextAttributeEvent paramServletContextAttributeEvent);
  
  public abstract void attributeReplaced(ServletContextAttributeEvent paramServletContextAttributeEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletContextAttributeListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */