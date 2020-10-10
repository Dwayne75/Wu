package javax.mail.event;

import java.util.EventListener;

public abstract interface StoreListener
  extends EventListener
{
  public abstract void notification(StoreEvent paramStoreEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\event\StoreListener.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */