package org.fourthline.cling.model.message.discovery;

import java.net.URL;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.DeviceUSNHeader;
import org.fourthline.cling.model.message.header.InterfaceMacHeader;
import org.fourthline.cling.model.message.header.LocationHeader;
import org.fourthline.cling.model.message.header.MaxAgeHeader;
import org.fourthline.cling.model.message.header.ServiceUSNHeader;
import org.fourthline.cling.model.message.header.UDNHeader;
import org.fourthline.cling.model.message.header.USNRootDeviceHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.types.NamedDeviceType;
import org.fourthline.cling.model.types.NamedServiceType;
import org.fourthline.cling.model.types.UDN;

public class IncomingSearchResponse
  extends IncomingDatagramMessage<UpnpResponse>
{
  public IncomingSearchResponse(IncomingDatagramMessage<UpnpResponse> source)
  {
    super(source);
  }
  
  public boolean isSearchResponseMessage()
  {
    UpnpHeader st = getHeaders().getFirstHeader(UpnpHeader.Type.ST);
    UpnpHeader usn = getHeaders().getFirstHeader(UpnpHeader.Type.USN);
    UpnpHeader ext = getHeaders().getFirstHeader(UpnpHeader.Type.EXT);
    return (st != null) && (st.getValue() != null) && (usn != null) && (usn.getValue() != null) && (ext != null);
  }
  
  public UDN getRootDeviceUDN()
  {
    UpnpHeader<UDN> udnHeader = getHeaders().getFirstHeader(UpnpHeader.Type.USN, USNRootDeviceHeader.class);
    if (udnHeader != null) {
      return (UDN)udnHeader.getValue();
    }
    udnHeader = getHeaders().getFirstHeader(UpnpHeader.Type.USN, UDNHeader.class);
    if (udnHeader != null) {
      return (UDN)udnHeader.getValue();
    }
    UpnpHeader<NamedDeviceType> deviceTypeHeader = getHeaders().getFirstHeader(UpnpHeader.Type.USN, DeviceUSNHeader.class);
    if (deviceTypeHeader != null) {
      return ((NamedDeviceType)deviceTypeHeader.getValue()).getUdn();
    }
    UpnpHeader<NamedServiceType> serviceTypeHeader = getHeaders().getFirstHeader(UpnpHeader.Type.USN, ServiceUSNHeader.class);
    if (serviceTypeHeader != null) {
      return ((NamedServiceType)serviceTypeHeader.getValue()).getUdn();
    }
    return null;
  }
  
  public URL getLocationURL()
  {
    LocationHeader header = (LocationHeader)getHeaders().getFirstHeader(UpnpHeader.Type.LOCATION, LocationHeader.class);
    if (header != null) {
      return (URL)header.getValue();
    }
    return null;
  }
  
  public Integer getMaxAge()
  {
    MaxAgeHeader header = (MaxAgeHeader)getHeaders().getFirstHeader(UpnpHeader.Type.MAX_AGE, MaxAgeHeader.class);
    if (header != null) {
      return (Integer)header.getValue();
    }
    return null;
  }
  
  public byte[] getInterfaceMacHeader()
  {
    InterfaceMacHeader header = (InterfaceMacHeader)getHeaders().getFirstHeader(UpnpHeader.Type.EXT_IFACE_MAC, InterfaceMacHeader.class);
    if (header != null) {
      return (byte[])header.getValue();
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\discovery\IncomingSearchResponse.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */