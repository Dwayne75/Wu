package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.types.BytesRange;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.model.dlna.types.AvailableSeekRangeType;
import org.fourthline.cling.support.model.dlna.types.AvailableSeekRangeType.Mode;
import org.fourthline.cling.support.model.dlna.types.NormalPlayTimeRange;

public class AvailableSeekRangeHeader
  extends DLNAHeader<AvailableSeekRangeType>
{
  public AvailableSeekRangeHeader() {}
  
  public AvailableSeekRangeHeader(AvailableSeekRangeType timeSeekRange)
  {
    setValue(timeSeekRange);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0)
    {
      String[] params = s.split(" ");
      if (params.length > 1) {
        try
        {
          AvailableSeekRangeType.Mode mode = null;
          NormalPlayTimeRange timeRange = null;
          BytesRange byteRange = null;
          try
          {
            mode = AvailableSeekRangeType.Mode.valueOf("MODE_" + params[0]);
          }
          catch (IllegalArgumentException e)
          {
            throw new InvalidValueException("Invalid AvailableSeekRange Mode");
          }
          boolean useTime = true;
          try
          {
            timeRange = NormalPlayTimeRange.valueOf(params[1], true);
          }
          catch (InvalidValueException timeInvalidValueException)
          {
            try
            {
              byteRange = BytesRange.valueOf(params[1]);
              useTime = false;
            }
            catch (InvalidValueException bytesInvalidValueException)
            {
              throw new InvalidValueException("Invalid AvailableSeekRange Range");
            }
          }
          if (useTime)
          {
            if (params.length > 2)
            {
              byteRange = BytesRange.valueOf(params[2]);
              setValue(new AvailableSeekRangeType(mode, timeRange, byteRange));
            }
            else
            {
              setValue(new AvailableSeekRangeType(mode, timeRange));
            }
          }
          else {
            setValue(new AvailableSeekRangeType(mode, byteRange));
          }
          return;
        }
        catch (InvalidValueException invalidValueException)
        {
          throw new InvalidHeaderException("Invalid AvailableSeekRange header value: " + s + "; " + invalidValueException.getMessage());
        }
      }
    }
    throw new InvalidHeaderException("Invalid AvailableSeekRange header value: " + s);
  }
  
  public String getString()
  {
    AvailableSeekRangeType t = (AvailableSeekRangeType)getValue();
    String s = Integer.toString(t.getModeFlag().ordinal());
    if (t.getNormalPlayTimeRange() != null) {
      s = s + " " + t.getNormalPlayTimeRange().getString(false);
    }
    if (t.getBytesRange() != null) {
      s = s + " " + t.getBytesRange().getString(false);
    }
    return s;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\AvailableSeekRangeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */