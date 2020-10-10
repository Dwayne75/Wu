package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;

public class MaxPrateHeader
  extends DLNAHeader<Long>
{
  public MaxPrateHeader()
  {
    setValue(Long.valueOf(0L));
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(Long.valueOf(Long.parseLong(s)));
      return;
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new InvalidHeaderException("Invalid SCID header value: " + s);
    }
  }
  
  public String getString()
  {
    return ((Long)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\MaxPrateHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */