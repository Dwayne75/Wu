package javax.mail.event;

import java.util.EventListener;

public abstract interface TransportListener
  extends EventListener
{
  public abstract void messageDelivered(TransportEvent paramTransportEvent);
  
  public abstract void messageNotDelivered(TransportEvent paramTransportEvent);
  
  public abstract void messagePartiallyDelivered(TransportEvent paramTransportEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\event\TransportListener.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */