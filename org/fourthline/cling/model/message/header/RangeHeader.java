package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.BytesRange;
import org.fourthline.cling.model.types.InvalidValueException;

public class RangeHeader
  extends UpnpHeader<BytesRange>
{
  public RangeHeader() {}
  
  public RangeHeader(BytesRange value)
  {
    setValue(value);
  }
  
  public RangeHeader(String s)
  {
    setString(s);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(BytesRange.valueOf(s));
    }
    catch (InvalidValueException invalidValueException)
    {
      throw new InvalidHeaderException("Invalid Range Header: " + invalidValueException.getMessage());
    }
  }
  
  public String getString()
  {
    return ((BytesRange)getValue()).getString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\RangeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */