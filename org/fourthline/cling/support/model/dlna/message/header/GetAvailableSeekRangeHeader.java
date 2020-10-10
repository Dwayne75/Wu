package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;

public class GetAvailableSeekRangeHeader
  extends DLNAHeader<Integer>
{
  public GetAvailableSeekRangeHeader()
  {
    setValue(Integer.valueOf(1));
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0) {
      try
      {
        int t = Integer.parseInt(s);
        if (t == 1) {
          return;
        }
      }
      catch (Exception localException) {}
    }
    throw new InvalidHeaderException("Invalid GetAvailableSeekRange header value: " + s);
  }
  
  public String getString()
  {
    return ((Integer)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\GetAvailableSeekRangeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */