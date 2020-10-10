package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpRequest.Method;
import org.fourthline.cling.model.message.header.HostHeader;
import org.fourthline.cling.model.message.header.LocationHeader;
import org.fourthline.cling.model.message.header.MaxAgeHeader;
import org.fourthline.cling.model.message.header.NTSHeader;
import org.fourthline.cling.model.message.header.ServerHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.NotificationSubtype;

public abstract class OutgoingNotificationRequest
  extends OutgoingDatagramMessage<UpnpRequest>
{
  private NotificationSubtype type;
  
  protected OutgoingNotificationRequest(Location location, LocalDevice device, NotificationSubtype type)
  {
    super(new UpnpRequest(UpnpRequest.Method.NOTIFY), 
    
      ModelUtil.getInetAddressByName("239.255.255.250"), 1900);
    
    this.type = type;
    
    getHeaders().add(UpnpHeader.Type.MAX_AGE, new MaxAgeHeader(device.getIdentity().getMaxAgeSeconds()));
    getHeaders().add(UpnpHeader.Type.LOCATION, new LocationHeader(location.getURL()));
    
    getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());
    getHeaders().add(UpnpHeader.Type.HOST, new HostHeader());
    getHeaders().add(UpnpHeader.Type.NTS, new NTSHeader(type));
  }
  
  public NotificationSubtype getType()
  {
    return this.type;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\discovery\OutgoingNotificationRequest.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */