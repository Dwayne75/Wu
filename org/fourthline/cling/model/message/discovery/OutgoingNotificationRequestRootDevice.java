package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.InterfaceMacHeader;
import org.fourthline.cling.model.message.header.RootDeviceHeader;
import org.fourthline.cling.model.message.header.USNRootDeviceHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.NotificationSubtype;

public class OutgoingNotificationRequestRootDevice
  extends OutgoingNotificationRequest
{
  public OutgoingNotificationRequestRootDevice(Location location, LocalDevice device, NotificationSubtype type)
  {
    super(location, device, type);
    
    getHeaders().add(UpnpHeader.Type.NT, new RootDeviceHeader());
    getHeaders().add(UpnpHeader.Type.USN, new USNRootDeviceHeader(device.getIdentity().getUdn()));
    if (("true".equals(System.getProperty("org.fourthline.cling.network.announceMACAddress"))) && 
      (location.getNetworkAddress().getHardwareAddress() != null)) {
      getHeaders().add(UpnpHeader.Type.EXT_IFACE_MAC, new InterfaceMacHeader(location
      
        .getNetworkAddress().getHardwareAddress()));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\discovery\OutgoingNotificationRequestRootDevice.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */