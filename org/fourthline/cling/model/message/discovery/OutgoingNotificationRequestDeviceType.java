package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.message.header.DeviceUSNHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.NotificationSubtype;

public class OutgoingNotificationRequestDeviceType
  extends OutgoingNotificationRequest
{
  public OutgoingNotificationRequestDeviceType(Location location, LocalDevice device, NotificationSubtype type)
  {
    super(location, device, type);
    
    getHeaders().add(UpnpHeader.Type.NT, new DeviceTypeHeader(device.getType()));
    getHeaders().add(UpnpHeader.Type.USN, new DeviceUSNHeader(device.getIdentity().getUdn(), device.getType()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\discovery\OutgoingNotificationRequestDeviceType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */