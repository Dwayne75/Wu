package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.NotificationSubtype;

public class NTSHeader
  extends UpnpHeader<NotificationSubtype>
{
  public NTSHeader() {}
  
  public NTSHeader(NotificationSubtype type)
  {
    setValue(type);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    for (NotificationSubtype type : ) {
      if (s.equals(type.getHeaderString()))
      {
        setValue(type);
        break;
      }
    }
    if (getValue() == null) {
      throw new InvalidHeaderException("Invalid NTS header value: " + s);
    }
  }
  
  public String getString()
  {
    return ((NotificationSubtype)getValue()).getHeaderString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\NTSHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */