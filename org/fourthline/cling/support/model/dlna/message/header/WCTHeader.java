package org.fourthline.cling.support.model.dlna.message.header;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.message.header.InvalidHeaderException;

public class WCTHeader
  extends DLNAHeader<Boolean>
{
  static final Pattern pattern = Pattern.compile("^[01]{1}$", 2);
  
  public WCTHeader()
  {
    setValue(Boolean.valueOf(false));
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (pattern.matcher(s).matches())
    {
      setValue(Boolean.valueOf(s.equals("1")));
      return;
    }
    throw new InvalidHeaderException("Invalid SCID header value: " + s);
  }
  
  public String getString()
  {
    return ((Boolean)getValue()).booleanValue() ? "1" : "0";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\WCTHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */