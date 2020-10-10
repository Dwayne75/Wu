package org.fourthline.cling.model.message.header;

import java.util.Locale;

public class RootDeviceHeader
  extends UpnpHeader<String>
{
  public RootDeviceHeader()
  {
    setValue("upnp:rootdevice");
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (!s.toLowerCase(Locale.ROOT).equals(getValue())) {
      throw new InvalidHeaderException("Invalid root device NT header value: " + s);
    }
  }
  
  public String getString()
  {
    return (String)getValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\RootDeviceHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */