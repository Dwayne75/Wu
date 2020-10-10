package javax.mail.event;

import java.util.EventListener;

public abstract interface MessageChangedListener
  extends EventListener
{
  public abstract void messageChanged(MessageChangedEvent paramMessageChangedEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\event\MessageChangedListener.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */