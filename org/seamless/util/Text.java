package org.seamless.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Text
{
  public static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ssz";
  
  public static String ltrim(String s)
  {
    return s.replaceAll("(?s)^\\s+", "");
  }
  
  public static String rtrim(String s)
  {
    return s.replaceAll("(?s)\\s+$", "");
  }
  
  public static String displayFilesize(long fileSizeInBytes)
  {
    if (fileSizeInBytes >= 1073741824L) {
      return new BigDecimal(fileSizeInBytes / 1024L / 1024L / 1024L) + " GiB";
    }
    if (fileSizeInBytes >= 1048576L) {
      return new BigDecimal(fileSizeInBytes / 1024L / 1024L) + " MiB";
    }
    if (fileSizeInBytes >= 1024L) {
      return new BigDecimal(fileSizeInBytes / 1024L) + " KiB";
    }
    return new BigDecimal(fileSizeInBytes) + " bytes";
  }
  
  public static Calendar fromISO8601String(TimeZone targetTimeZone, String s)
  {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
    format.setTimeZone(targetTimeZone);
    try
    {
      Calendar cal = new GregorianCalendar();
      cal.setTime(format.parse(s));
      return cal;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public static String toISO8601String(TimeZone targetTimeZone, Date datetime)
  {
    Calendar cal = new GregorianCalendar();
    cal.setTime(datetime);
    return toISO8601String(targetTimeZone, cal);
  }
  
  public static String toISO8601String(TimeZone targetTimeZone, long unixTime)
  {
    Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(unixTime);
    return toISO8601String(targetTimeZone, cal);
  }
  
  public static String toISO8601String(TimeZone targetTimeZone, Calendar cal)
  {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
    format.setTimeZone(targetTimeZone);
    try
    {
      return format.format(cal.getTime());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\Text.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */