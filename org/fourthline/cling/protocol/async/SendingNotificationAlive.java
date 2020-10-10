package org.fourthline.cling.protocol.async;

import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.NotificationSubtype;
import org.fourthline.cling.transport.RouterException;

public class SendingNotificationAlive
  extends SendingNotification
{
  private static final Logger log = Logger.getLogger(SendingNotification.class.getName());
  
  public SendingNotificationAlive(UpnpService upnpService, LocalDevice device)
  {
    super(upnpService, device);
  }
  
  protected void execute()
    throws RouterException
  {
    log.fine("Sending alive messages (" + getBulkRepeat() + " times) for: " + getDevice());
    super.execute();
  }
  
  protected NotificationSubtype getNotificationSubtype()
  {
    return NotificationSubtype.ALIVE;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\async\SendingNotificationAlive.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */