package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.NamedDeviceType;
import org.fourthline.cling.model.types.UDN;

public class DeviceUSNHeader
  extends UpnpHeader<NamedDeviceType>
{
  public DeviceUSNHeader() {}
  
  public DeviceUSNHeader(UDN udn, DeviceType deviceType)
  {
    setValue(new NamedDeviceType(udn, deviceType));
  }
  
  public DeviceUSNHeader(NamedDeviceType value)
  {
    setValue(value);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(NamedDeviceType.valueOf(s));
    }
    catch (Exception ex)
    {
      throw new InvalidHeaderException("Invalid device USN header value, " + ex.getMessage());
    }
  }
  
  public String getString()
  {
    return ((NamedDeviceType)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\DeviceUSNHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */