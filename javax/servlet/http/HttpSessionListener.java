package javax.servlet.http;

import java.util.EventListener;

public abstract interface HttpSessionListener
  extends EventListener
{
  public abstract void sessionCreated(HttpSessionEvent paramHttpSessionEvent);
  
  public abstract void sessionDestroyed(HttpSessionEvent paramHttpSessionEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\http\HttpSessionListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */