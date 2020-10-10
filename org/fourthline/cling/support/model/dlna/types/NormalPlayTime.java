package org.fourthline.cling.support.model.dlna.types;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.types.InvalidValueException;

public class NormalPlayTime
{
  public static enum Format
  {
    SECONDS,  TIME;
    
    private Format() {}
  }
  
  static final Pattern pattern = Pattern.compile("^(\\d+):(\\d{1,2}):(\\d{1,2})(\\.(\\d{1,3}))?|(\\d+)(\\.(\\d{1,3}))?$", 2);
  private long milliseconds;
  
  public NormalPlayTime(long milliseconds)
  {
    if (milliseconds < 0L) {
      throw new InvalidValueException("Invalid parameter milliseconds: " + milliseconds);
    }
    this.milliseconds = milliseconds;
  }
  
  public NormalPlayTime(long hours, long minutes, long seconds, long milliseconds)
    throws InvalidValueException
  {
    if (hours < 0L) {
      throw new InvalidValueException("Invalid parameter hours: " + hours);
    }
    if ((minutes < 0L) || (minutes > 59L)) {
      throw new InvalidValueException("Invalid parameter minutes: " + hours);
    }
    if ((seconds < 0L) || (seconds > 59L)) {
      throw new InvalidValueException("Invalid parameter seconds: " + hours);
    }
    if ((milliseconds < 0L) || (milliseconds > 999L)) {
      throw new InvalidValueException("Invalid parameter milliseconds: " + milliseconds);
    }
    this.milliseconds = ((hours * 60L * 60L + minutes * 60L + seconds) * 1000L + milliseconds);
  }
  
  public long getMilliseconds()
  {
    return this.milliseconds;
  }
  
  public void setMilliseconds(long milliseconds)
  {
    if (milliseconds < 0L) {
      throw new InvalidValueException("Invalid parameter milliseconds: " + milliseconds);
    }
    this.milliseconds = milliseconds;
  }
  
  public String getString()
  {
    return getString(Format.SECONDS);
  }
  
  public String getString(Format format)
  {
    long seconds = TimeUnit.MILLISECONDS.toSeconds(this.milliseconds);
    long ms = this.milliseconds % 1000L;
    switch (format)
    {
    case TIME: 
      seconds = TimeUnit.MILLISECONDS.toSeconds(this.milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this.milliseconds));
      long hours = TimeUnit.MILLISECONDS.toHours(this.milliseconds);
      long minutes = TimeUnit.MILLISECONDS.toMinutes(this.milliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(this.milliseconds));
      return String.format(Locale.ROOT, "%d:%02d:%02d.%03d", new Object[] { Long.valueOf(hours), Long.valueOf(minutes), Long.valueOf(seconds), Long.valueOf(ms) });
    }
    return String.format(Locale.ROOT, "%d.%03d", new Object[] { Long.valueOf(seconds), Long.valueOf(ms) });
  }
  
  public static NormalPlayTime valueOf(String s)
    throws InvalidValueException
  {
    Matcher matcher = pattern.matcher(s);
    if (matcher.matches())
    {
      int msMultiplier = 0;
      try
      {
        if (matcher.group(1) != null)
        {
          msMultiplier = (int)Math.pow(10.0D, 3 - matcher.group(5).length());
          
          return new NormalPlayTime(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)), Long.parseLong(matcher.group(3)), Long.parseLong(matcher.group(5)) * msMultiplier);
        }
        msMultiplier = (int)Math.pow(10.0D, 3 - matcher.group(8).length());
        
        return new NormalPlayTime(Long.parseLong(matcher.group(6)) * 1000L + Long.parseLong(matcher.group(8)) * msMultiplier);
      }
      catch (NumberFormatException localNumberFormatException) {}
    }
    throw new InvalidValueException("Can't parse NormalPlayTime: " + s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\types\NormalPlayTime.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */