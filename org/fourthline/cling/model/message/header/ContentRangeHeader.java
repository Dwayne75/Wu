package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.BytesRange;
import org.fourthline.cling.model.types.InvalidValueException;

public class ContentRangeHeader
  extends UpnpHeader<BytesRange>
{
  public static final String PREFIX = "bytes ";
  
  public ContentRangeHeader() {}
  
  public ContentRangeHeader(BytesRange value)
  {
    setValue(value);
  }
  
  public ContentRangeHeader(String s)
  {
    setString(s);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(BytesRange.valueOf(s, "bytes "));
    }
    catch (InvalidValueException invalidValueException)
    {
      throw new InvalidHeaderException("Invalid Range Header: " + invalidValueException.getMessage());
    }
  }
  
  public String getString()
  {
    return ((BytesRange)getValue()).getString(true, "bytes ");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\ContentRangeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */