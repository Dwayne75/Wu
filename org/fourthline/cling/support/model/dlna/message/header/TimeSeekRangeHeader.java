package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.types.BytesRange;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.model.dlna.types.NormalPlayTimeRange;
import org.fourthline.cling.support.model.dlna.types.TimeSeekRangeType;

public class TimeSeekRangeHeader
  extends DLNAHeader<TimeSeekRangeType>
{
  public TimeSeekRangeHeader() {}
  
  public TimeSeekRangeHeader(TimeSeekRangeType timeSeekRange)
  {
    setValue(timeSeekRange);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0)
    {
      String[] params = s.split(" ");
      if (params.length > 0) {
        try
        {
          TimeSeekRangeType t = new TimeSeekRangeType(NormalPlayTimeRange.valueOf(params[0]));
          if (params.length > 1) {
            t.setBytesRange(BytesRange.valueOf(params[1]));
          }
          setValue(t);
          return;
        }
        catch (InvalidValueException invalidValueException)
        {
          throw new InvalidHeaderException("Invalid TimeSeekRange header value: " + s + "; " + invalidValueException.getMessage());
        }
      }
    }
    throw new InvalidHeaderException("Invalid TimeSeekRange header value: " + s);
  }
  
  public String getString()
  {
    TimeSeekRangeType t = (TimeSeekRangeType)getValue();
    String s = t.getNormalPlayTimeRange().getString();
    if (t.getBytesRange() != null) {
      s = s + " " + t.getBytesRange().getString(true);
    }
    return s;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\TimeSeekRangeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */