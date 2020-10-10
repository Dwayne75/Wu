package javax.servlet;

import java.util.EventListener;

public abstract interface ServletContextListener
  extends EventListener
{
  public abstract void contextInitialized(ServletContextEvent paramServletContextEvent);
  
  public abstract void contextDestroyed(ServletContextEvent paramServletContextEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletContextListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */