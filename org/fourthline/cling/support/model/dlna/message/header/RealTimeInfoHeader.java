package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.support.model.dlna.types.NormalPlayTime;

public class RealTimeInfoHeader
  extends DLNAHeader<NormalPlayTime>
{
  public static final String PREFIX = "DLNA.ORG_TLAG=";
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if ((s.length() != 0) && (s.startsWith("DLNA.ORG_TLAG="))) {
      try
      {
        s = s.substring("DLNA.ORG_TLAG=".length());
        setValue(s.equals("*") ? null : NormalPlayTime.valueOf(s));
        return;
      }
      catch (Exception localException) {}
    }
    throw new InvalidHeaderException("Invalid RealTimeInfo header value: " + s);
  }
  
  public String getString()
  {
    NormalPlayTime v = (NormalPlayTime)getValue();
    if (v == null) {
      return "DLNA.ORG_TLAG=*";
    }
    return "DLNA.ORG_TLAG=" + v.getString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\RealTimeInfoHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */