package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.header.EXTHeader;
import org.fourthline.cling.model.message.header.InterfaceMacHeader;
import org.fourthline.cling.model.message.header.LocationHeader;
import org.fourthline.cling.model.message.header.MaxAgeHeader;
import org.fourthline.cling.model.message.header.ServerHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;

public class OutgoingSearchResponse
  extends OutgoingDatagramMessage<UpnpResponse>
{
  public OutgoingSearchResponse(IncomingDatagramMessage request, Location location, LocalDevice device)
  {
    super(new UpnpResponse(UpnpResponse.Status.OK), request.getSourceAddress(), request.getSourcePort());
    
    getHeaders().add(UpnpHeader.Type.MAX_AGE, new MaxAgeHeader(device.getIdentity().getMaxAgeSeconds()));
    getHeaders().add(UpnpHeader.Type.LOCATION, new LocationHeader(location.getURL()));
    getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());
    getHeaders().add(UpnpHeader.Type.EXT, new EXTHeader());
    if (("true".equals(System.getProperty("org.fourthline.cling.network.announceMACAddress"))) && 
      (location.getNetworkAddress().getHardwareAddress() != null)) {
      getHeaders().add(UpnpHeader.Type.EXT_IFACE_MAC, new InterfaceMacHeader(location
      
        .getNetworkAddress().getHardwareAddress()));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\discovery\OutgoingSearchResponse.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */