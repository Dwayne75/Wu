package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.UDN;

public class USNRootDeviceHeader
  extends UpnpHeader<UDN>
{
  public static final String ROOT_DEVICE_SUFFIX = "::upnp:rootdevice";
  
  public USNRootDeviceHeader() {}
  
  public USNRootDeviceHeader(UDN udn)
  {
    setValue(udn);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if ((!s.startsWith("uuid:")) || (!s.endsWith("::upnp:rootdevice"))) {
      throw new InvalidHeaderException("Invalid root device USN header value, must start with 'uuid:' and end with '::upnp:rootdevice' but is '" + s + "'");
    }
    UDN udn = new UDN(s.substring("uuid:".length(), s.length() - "::upnp:rootdevice".length()));
    setValue(udn);
  }
  
  public String getString()
  {
    return ((UDN)getValue()).toString() + "::upnp:rootdevice";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\USNRootDeviceHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */