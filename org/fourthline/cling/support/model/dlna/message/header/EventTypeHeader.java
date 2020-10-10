package org.fourthline.cling.support.model.dlna.message.header;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.message.header.InvalidHeaderException;

public class EventTypeHeader
  extends DLNAHeader<String>
{
  static final Pattern pattern = Pattern.compile("^[0-9]{4}$", 2);
  
  public EventTypeHeader()
  {
    setValue("0000");
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (pattern.matcher(s).matches())
    {
      setValue(s);
      return;
    }
    throw new InvalidHeaderException("Invalid EventType header value: " + s);
  }
  
  public String getString()
  {
    return ((String)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\EventTypeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */