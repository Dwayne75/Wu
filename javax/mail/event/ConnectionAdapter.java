package javax.mail.event;

public abstract class ConnectionAdapter
  implements ConnectionListener
{
  public void opened(ConnectionEvent e) {}
  
  public void disconnected(ConnectionEvent e) {}
  
  public void closed(ConnectionEvent e) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\event\ConnectionAdapter.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */