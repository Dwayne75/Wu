package org.seamless.util.time;

import java.io.Serializable;
import java.util.Date;

public class DateRange
  implements Serializable
{
  protected Date start;
  protected Date end;
  public DateRange() {}
  
  public static enum Preset
  {
    ALL(new DateRange(null)),  YEAR_TO_DATE(new DateRange(new Date(DateRange.getCurrentYear(), 0, 1))),  MONTH_TO_DATE(new DateRange(new Date(DateRange.getCurrentYear(), DateRange.getCurrentMonth(), 1))),  LAST_MONTH(DateRange.getMonthOf(new Date(DateRange.getCurrentYear(), DateRange.getCurrentMonth() - 1, 1))),  LAST_YEAR(new DateRange(new Date(DateRange.getCurrentYear() - 1, 0, 1), new Date(DateRange.getCurrentYear() - 1, 11, 31)));
    
    DateRange dateRange;
    
    private Preset(DateRange dateRange)
    {
      this.dateRange = dateRange;
    }
    
    public DateRange getDateRange()
    {
      return this.dateRange;
    }
  }
  
  public DateRange(Date start)
  {
    this.start = start;
  }
  
  public DateRange(Date start, Date end)
  {
    this.start = start;
    this.end = end;
  }
  
  public DateRange(String startUnixtime, String endUnixtime)
    throws NumberFormatException
  {
    if (startUnixtime != null) {
      this.start = new Date(Long.valueOf(startUnixtime).longValue());
    }
    if (endUnixtime != null) {
      this.end = new Date(Long.valueOf(endUnixtime).longValue());
    }
  }
  
  public Date getStart()
  {
    return this.start;
  }
  
  public Date getEnd()
  {
    return this.end;
  }
  
  public boolean isStartAfter(Date date)
  {
    return (getStart() != null) && (getStart().getTime() > date.getTime());
  }
  
  public Date getOneDayBeforeStart()
  {
    if (getStart() == null) {
      throw new IllegalStateException("Can't get day before start date because start date is null");
    }
    return new Date(getStart().getTime() - 86400000L);
  }
  
  public static int getCurrentYear()
  {
    return new Date().getYear();
  }
  
  public static int getCurrentMonth()
  {
    return new Date().getMonth();
  }
  
  public static int getCurrentDayOfMonth()
  {
    return new Date().getDate();
  }
  
  public boolean hasStartOrEnd()
  {
    return (getStart() != null) || (getEnd() != null);
  }
  
  public static int getDaysInMonth(Date date)
  {
    int month = date.getMonth();
    int year = date.getYear() + 1900;
    boolean isLeapYear = (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0));
    int[] daysInMonth = { 31, isLeapYear ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    return daysInMonth[month];
  }
  
  public static DateRange getMonthOf(Date date)
  {
    return new DateRange(new Date(date.getYear(), date.getMonth(), 1), new Date(date.getYear(), date.getMonth(), getDaysInMonth(date)));
  }
  
  public boolean isInRange(Date date)
  {
    return (getStart() != null) && (getStart().getTime() < date.getTime()) && ((getEnd() == null) || (getEnd().getTime() > date.getTime()));
  }
  
  public boolean isValid()
  {
    return (getStart() != null) && ((getEnd() == null) || (getStart().getTime() <= getEnd().getTime()));
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    DateRange dateRange = (DateRange)o;
    if (this.end != null ? !this.end.equals(dateRange.end) : dateRange.end != null) {
      return false;
    }
    if (this.start != null ? !this.start.equals(dateRange.start) : dateRange.start != null) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.start != null ? this.start.hashCode() : 0;
    result = 31 * result + (this.end != null ? this.end.hashCode() : 0);
    return result;
  }
  
  public static DateRange valueOf(String s)
  {
    if (!s.contains("dr=")) {
      return null;
    }
    String dr = s.substring(s.indexOf("dr=") + 3);
    dr = dr.substring(0, dr.indexOf(";"));
    String[] split = dr.split(",");
    if (split.length != 2) {
      return null;
    }
    try
    {
      return new DateRange(!split[0].equals("0") ? new Date(Long.valueOf(split[0]).longValue()) : null, !split[1].equals("0") ? new Date(Long.valueOf(split[1]).longValue()) : null);
    }
    catch (Exception ex) {}
    return null;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("dr=");
    sb.append(getStart() != null ? Long.valueOf(getStart().getTime()) : "0");
    sb.append(",");
    sb.append(getEnd() != null ? Long.valueOf(getEnd().getTime()) : "0");
    sb.append(";");
    return sb.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\time\DateRange.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */