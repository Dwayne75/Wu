package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class EventSequenceHeader
  extends UpnpHeader<UnsignedIntegerFourBytes>
{
  public EventSequenceHeader() {}
  
  public EventSequenceHeader(long value)
  {
    setValue(new UnsignedIntegerFourBytes(value));
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (!"0".equals(s)) {
      while (s.startsWith("0")) {
        s = s.substring(1);
      }
    }
    try
    {
      setValue(new UnsignedIntegerFourBytes(s));
    }
    catch (NumberFormatException ex)
    {
      throw new InvalidHeaderException("Invalid event sequence, " + ex.getMessage());
    }
  }
  
  public String getString()
  {
    return ((UnsignedIntegerFourBytes)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\EventSequenceHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */