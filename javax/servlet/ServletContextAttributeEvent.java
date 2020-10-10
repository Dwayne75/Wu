package javax.servlet;

public class ServletContextAttributeEvent
  extends ServletContextEvent
{
  private String name;
  private Object value;
  
  public ServletContextAttributeEvent(ServletContext source, String name, Object value)
  {
    super(source);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletContextAttributeEvent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */