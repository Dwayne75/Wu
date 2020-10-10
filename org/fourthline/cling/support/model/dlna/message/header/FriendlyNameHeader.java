package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;

public class FriendlyNameHeader
  extends DLNAHeader<String>
{
  public FriendlyNameHeader()
  {
    setValue("");
  }
  
  public FriendlyNameHeader(String name)
  {
    setValue(name);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0)
    {
      setValue(s);
      return;
    }
    throw new InvalidHeaderException("Invalid GetAvailableSeekRange header value: " + s);
  }
  
  public String getString()
  {
    return (String)getValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\FriendlyNameHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */