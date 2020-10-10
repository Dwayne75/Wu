package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.UDN;

public class UDNHeader
  extends UpnpHeader<UDN>
{
  public UDNHeader() {}
  
  public UDNHeader(UDN udn)
  {
    setValue(udn);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (!s.startsWith("uuid:")) {
      throw new InvalidHeaderException("Invalid UDA header value, must start with 'uuid:': " + s);
    }
    if (s.contains("::urn")) {
      throw new InvalidHeaderException("Invalid UDA header value, must not contain '::urn': " + s);
    }
    UDN udn = new UDN(s.substring("uuid:".length()));
    setValue(udn);
  }
  
  public String getString()
  {
    return ((UDN)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\UDNHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */