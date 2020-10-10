package javax.servlet.http;

public class HttpSessionBindingEvent
  extends HttpSessionEvent
{
  private String name;
  private Object value;
  
  public HttpSessionBindingEvent(HttpSession session, String name)
  {
    super(session);
    this.name = name;
  }
  
  public HttpSessionBindingEvent(HttpSession session, String name, Object value)
  {
    super(session);
    this.name = name;
    this.value = value;
  }
  
  public HttpSession getSession()
  {
    return super.getSession();
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public Object getValue()
  {
    return this.value;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\http\HttpSessionBindingEvent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */