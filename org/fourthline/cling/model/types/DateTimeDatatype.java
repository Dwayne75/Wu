package org.fourthline.cling.model.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeDatatype
  extends AbstractDatatype<Calendar>
{
  protected String[] readFormats;
  protected String writeFormat;
  
  public DateTimeDatatype(String[] readFormats, String writeFormat)
  {
    this.readFormats = readFormats;
    this.writeFormat = writeFormat;
  }
  
  public Calendar valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    Date d = getDateValue(s, this.readFormats);
    if (d == null) {
      throw new InvalidValueException("Can't parse date/time from: " + s);
    }
    Calendar c = Calendar.getInstance(getTimeZone());
    c.setTime(d);
    
    return c;
  }
  
  public String getString(Calendar value)
    throws InvalidValueException
  {
    if (value == null) {
      return "";
    }
    SimpleDateFormat sdt = new SimpleDateFormat(this.writeFormat);
    sdt.setTimeZone(getTimeZone());
    return sdt.format(value.getTime());
  }
  
  protected String normalizeTimeZone(String value)
  {
    if (value.endsWith("Z")) {
      value = value.substring(0, value.length() - 1) + "+0000";
    } else if ((value.length() > 7) && 
      (value.charAt(value.length() - 3) == ':') && (
      (value.charAt(value.length() - 6) == '-') || (value.charAt(value.length() - 6) == '+'))) {
      value = value.substring(0, value.length() - 3) + value.substring(value.length() - 2);
    }
    return value;
  }
  
  protected Date getDateValue(String value, String[] formats)
  {
    value = normalizeTimeZone(value);
    
    Date d = null;
    for (String format : formats)
    {
      SimpleDateFormat sdt = new SimpleDateFormat(format);
      sdt.setTimeZone(getTimeZone());
      try
      {
        d = sdt.parse(value);
      }
      catch (ParseException localParseException) {}
    }
    return d;
  }
  
  protected TimeZone getTimeZone()
  {
    return TimeZone.getDefault();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\DateTimeDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */