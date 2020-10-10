package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;

public class SCIDHeader
  extends DLNAHeader<String>
{
  public SCIDHeader()
  {
    setValue("");
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0)
    {
      setValue(s);
      return;
    }
    throw new InvalidHeaderException("Invalid SCID header value: " + s);
  }
  
  public String getString()
  {
    return ((String)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\SCIDHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */