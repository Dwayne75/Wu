package javax.servlet.http;

import java.util.EventObject;

public class HttpSessionEvent
  extends EventObject
{
  public HttpSessionEvent(HttpSession source)
  {
    super(source);
  }
  
  public HttpSession getSession()
  {
    return (HttpSession)super.getSource();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\http\HttpSessionEvent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */