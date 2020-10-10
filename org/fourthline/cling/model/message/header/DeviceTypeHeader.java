package org.fourthline.cling.model.message.header;

import java.net.URI;
import org.fourthline.cling.model.types.DeviceType;

public class DeviceTypeHeader
  extends UpnpHeader<DeviceType>
{
  public DeviceTypeHeader() {}
  
  public DeviceTypeHeader(URI uri)
  {
    setString(uri.toString());
  }
  
  public DeviceTypeHeader(DeviceType value)
  {
    setValue(value);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(DeviceType.valueOf(s));
    }
    catch (RuntimeException ex)
    {
      throw new InvalidHeaderException("Invalid device type header value, " + ex.getMessage());
    }
  }
  
  public String getString()
  {
    return ((DeviceType)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\DeviceTypeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */