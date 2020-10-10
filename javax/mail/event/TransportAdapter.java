package javax.mail.event;

public abstract class TransportAdapter
  implements TransportListener
{
  public void messageDelivered(TransportEvent e) {}
  
  public void messageNotDelivered(TransportEvent e) {}
  
  public void messagePartiallyDelivered(TransportEvent e) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\event\TransportAdapter.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */