package javax.servlet.http;

import java.util.EventListener;

public abstract interface HttpSessionActivationListener
  extends EventListener
{
  public abstract void sessionWillPassivate(HttpSessionEvent paramHttpSessionEvent);
  
  public abstract void sessionDidActivate(HttpSessionEvent paramHttpSessionEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\http\HttpSessionActivationListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */