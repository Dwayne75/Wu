package javax.servlet;

import java.util.EventObject;

public class ServletRequestEvent
  extends EventObject
{
  private final transient ServletRequest request;
  
  public ServletRequestEvent(ServletContext sc, ServletRequest request)
  {
    super(sc);
    this.request = request;
  }
  
  public ServletRequest getServletRequest()
  {
    return this.request;
  }
  
  public ServletContext getServletContext()
  {
    return (ServletContext)super.getSource();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletRequestEvent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */