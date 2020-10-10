package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.support.model.dlna.types.NormalPlayTimeRange;

public class AvailableRangeHeader
  extends DLNAHeader<NormalPlayTimeRange>
{
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0) {
      try
      {
        setValue(NormalPlayTimeRange.valueOf(s, true));
        return;
      }
      catch (Exception localException) {}
    }
    throw new InvalidHeaderException("Invalid AvailableRange header value: " + s);
  }
  
  public String getString()
  {
    return ((NormalPlayTimeRange)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\AvailableRangeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */