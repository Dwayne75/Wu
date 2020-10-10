package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.ServiceTypeHeader;
import org.fourthline.cling.model.message.header.ServiceUSNHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.NotificationSubtype;
import org.fourthline.cling.model.types.ServiceType;

public class OutgoingNotificationRequestServiceType
  extends OutgoingNotificationRequest
{
  public OutgoingNotificationRequestServiceType(Location location, LocalDevice device, NotificationSubtype type, ServiceType serviceType)
  {
    super(location, device, type);
    
    getHeaders().add(UpnpHeader.Type.NT, new ServiceTypeHeader(serviceType));
    getHeaders().add(UpnpHeader.Type.USN, new ServiceUSNHeader(device.getIdentity().getUdn(), serviceType));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\discovery\OutgoingNotificationRequestServiceType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */