package javax.mail.event;

import java.util.EventListener;

public abstract interface ConnectionListener
  extends EventListener
{
  public abstract void opened(ConnectionEvent paramConnectionEvent);
  
  public abstract void disconnected(ConnectionEvent paramConnectionEvent);
  
  public abstract void closed(ConnectionEvent paramConnectionEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\event\ConnectionListener.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */