package org.fourthline.cling.model.message.header;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaxAgeHeader
  extends UpnpHeader<Integer>
{
  public static final Pattern MAX_AGE_REGEX = Pattern.compile(".*max-age\\s*=\\s*([0-9]+).*");
  
  public MaxAgeHeader(Integer maxAge)
  {
    setValue(maxAge);
  }
  
  public MaxAgeHeader()
  {
    setValue(Integer.valueOf(1800));
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    Matcher matcher = MAX_AGE_REGEX.matcher(s.toLowerCase(Locale.ROOT));
    if (!matcher.matches()) {
      throw new InvalidHeaderException("Invalid cache-control value, can't parse max-age seconds: " + s);
    }
    Integer maxAge = Integer.valueOf(Integer.parseInt(matcher.group(1)));
    setValue(maxAge);
  }
  
  public String getString()
  {
    return "max-age=" + ((Integer)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\MaxAgeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */