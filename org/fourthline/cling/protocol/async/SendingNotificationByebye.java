package org.fourthline.cling.protocol.async;

import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.NotificationSubtype;
import org.fourthline.cling.transport.RouterException;

public class SendingNotificationByebye
  extends SendingNotification
{
  private static final Logger log = Logger.getLogger(SendingNotification.class.getName());
  
  public SendingNotificationByebye(UpnpService upnpService, LocalDevice device)
  {
    super(upnpService, device);
  }
  
  protected void execute()
    throws RouterException
  {
    log.fine("Sending byebye messages (" + getBulkRepeat() + " times) for: " + getDevice());
    super.execute();
  }
  
  protected NotificationSubtype getNotificationSubtype()
  {
    return NotificationSubtype.BYEBYE;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\async\SendingNotificationByebye.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */