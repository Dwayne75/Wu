package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.NotificationSubtype;

public class STAllHeader
  extends UpnpHeader<NotificationSubtype>
{
  public STAllHeader()
  {
    setValue(NotificationSubtype.ALL);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (!s.equals(NotificationSubtype.ALL.getHeaderString())) {
      throw new InvalidHeaderException("Invalid ST header value (not " + NotificationSubtype.ALL + "): " + s);
    }
  }
  
  public String getString()
  {
    return ((NotificationSubtype)getValue()).getHeaderString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\STAllHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */