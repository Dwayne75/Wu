package org.fourthline.cling.model.message.header;

import java.util.Locale;

public class NTEventHeader
  extends UpnpHeader<String>
{
  public NTEventHeader()
  {
    setValue("upnp:event");
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (!s.toLowerCase(Locale.ROOT).equals(getValue())) {
      throw new InvalidHeaderException("Invalid event NT header value: " + s);
    }
  }
  
  public String getString()
  {
    return (String)getValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\NTEventHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */