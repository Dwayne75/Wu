package javax.mail.event;

import java.util.EventListener;

public abstract interface MessageCountListener
  extends EventListener
{
  public abstract void messagesAdded(MessageCountEvent paramMessageCountEvent);
  
  public abstract void messagesRemoved(MessageCountEvent paramMessageCountEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\event\MessageCountListener.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */