package org.seamless.util.time;

import java.io.Serializable;

public enum DateRangeOption
  implements Serializable
{
  ALL("All dates", DateRange.Preset.ALL.getDateRange()),  MONTH_TO_DATE("Month to date", DateRange.Preset.MONTH_TO_DATE.getDateRange()),  YEAR_TO_DATE("Year to date", DateRange.Preset.YEAR_TO_DATE.getDateRange()),  LAST_MONTH("Last month", DateRange.Preset.LAST_MONTH.getDateRange()),  LAST_YEAR("Last year", DateRange.Preset.LAST_YEAR.getDateRange()),  CUSTOM("Custom dates", null);
  
  String label;
  DateRange dateRange;
  
  private DateRangeOption(String label, DateRange dateRange)
  {
    this.label = label;
    this.dateRange = dateRange;
  }
  
  public String getLabel()
  {
    return this.label;
  }
  
  public DateRange getDateRange()
  {
    return this.dateRange;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\time\DateRangeOption.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */