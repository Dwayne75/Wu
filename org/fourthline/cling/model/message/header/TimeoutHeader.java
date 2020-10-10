package org.fourthline.cling.model.message.header;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeoutHeader
  extends UpnpHeader<Integer>
{
  public static final Integer INFINITE_VALUE = Integer.valueOf(Integer.MAX_VALUE);
  public static final Pattern PATTERN = Pattern.compile("Second-(?:([0-9]+)|infinite)");
  
  public TimeoutHeader()
  {
    setValue(Integer.valueOf(1800));
  }
  
  public TimeoutHeader(int timeoutSeconds)
  {
    setValue(Integer.valueOf(timeoutSeconds));
  }
  
  public TimeoutHeader(Integer timeoutSeconds)
  {
    setValue(timeoutSeconds);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    Matcher matcher = PATTERN.matcher(s);
    if (!matcher.matches()) {
      throw new InvalidHeaderException("Can't parse timeout seconds integer from: " + s);
    }
    if (matcher.group(1) != null) {
      setValue(Integer.valueOf(Integer.parseInt(matcher.group(1))));
    } else {
      setValue(INFINITE_VALUE);
    }
  }
  
  public String getString()
  {
    return "Second-" + (((Integer)getValue()).equals(INFINITE_VALUE) ? "infinite" : (Serializable)getValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\TimeoutHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */