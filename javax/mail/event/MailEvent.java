package javax.mail.event;

import java.util.EventObject;

public abstract class MailEvent
  extends EventObject
{
  private static final long serialVersionUID = 1846275636325456631L;
  
  public MailEvent(Object source)
  {
    super(source);
  }
  
  public abstract void dispatch(Object paramObject);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\event\MailEvent.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */