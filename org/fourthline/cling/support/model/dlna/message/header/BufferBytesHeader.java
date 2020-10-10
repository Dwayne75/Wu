package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class BufferBytesHeader
  extends DLNAHeader<UnsignedIntegerFourBytes>
{
  public BufferBytesHeader()
  {
    setValue(new UnsignedIntegerFourBytes(0L));
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(new UnsignedIntegerFourBytes(s));
      return;
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new InvalidHeaderException("Invalid header value: " + s);
    }
  }
  
  public String getString()
  {
    return ((UnsignedIntegerFourBytes)getValue()).getValue().toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\BufferBytesHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */