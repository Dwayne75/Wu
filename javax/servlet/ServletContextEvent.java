package javax.servlet;

import java.util.EventObject;

public class ServletContextEvent
  extends EventObject
{
  public ServletContextEvent(ServletContext source)
  {
    super(source);
  }
  
  public ServletContext getServletContext()
  {
    return (ServletContext)super.getSource();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletContextEvent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */