package org.fourthline.cling.model.message.header;

import java.net.URI;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;

public class UDADeviceTypeHeader
  extends DeviceTypeHeader
{
  public UDADeviceTypeHeader() {}
  
  public UDADeviceTypeHeader(URI uri)
  {
    super(uri);
  }
  
  public UDADeviceTypeHeader(DeviceType value)
  {
    super(value);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(UDADeviceType.valueOf(s));
    }
    catch (Exception ex)
    {
      throw new InvalidHeaderException("Invalid UDA device type header value, " + ex.getMessage());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\UDADeviceTypeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */