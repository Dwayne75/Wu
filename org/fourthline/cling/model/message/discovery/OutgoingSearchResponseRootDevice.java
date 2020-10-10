package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.RootDeviceHeader;
import org.fourthline.cling.model.message.header.USNRootDeviceHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;

public class OutgoingSearchResponseRootDevice
  extends OutgoingSearchResponse
{
  public OutgoingSearchResponseRootDevice(IncomingDatagramMessage request, Location location, LocalDevice device)
  {
    super(request, location, device);
    
    getHeaders().add(UpnpHeader.Type.ST, new RootDeviceHeader());
    getHeaders().add(UpnpHeader.Type.USN, new USNRootDeviceHeader(device.getIdentity().getUdn()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\discovery\OutgoingSearchResponseRootDevice.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */