package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.support.model.dlna.types.BufferInfoType;

public class BufferInfoHeader
  extends DLNAHeader<BufferInfoType>
{
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0) {
      try
      {
        setValue(BufferInfoType.valueOf(s));
        return;
      }
      catch (Exception localException) {}
    }
    throw new InvalidHeaderException("Invalid BufferInfo header value: " + s);
  }
  
  public String getString()
  {
    return ((BufferInfoType)getValue()).getString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\BufferInfoHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */