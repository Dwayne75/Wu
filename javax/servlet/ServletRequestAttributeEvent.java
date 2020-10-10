package javax.servlet;

public class ServletRequestAttributeEvent
  extends ServletRequestEvent
{
  private String name;
  private Object value;
  
  public ServletRequestAttributeEvent(ServletContext sc, ServletRequest request, String name, Object value)
  {
    super(sc, request);
    this.name = name;
    this.value = value;
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletRequestAttributeEvent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */