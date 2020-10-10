package winterwell.jtwitter.ecosystem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public class TwitterCounterStats
{
  public final String screenName;
  public final Date dateUpdated;
  public final int followDays;
  public final double avgGrowth;
  public final long rank;
  public final ArrayList<DateValue> data;
  
  public String toString()
  {
    if (this.data.isEmpty()) {
      return "TwitterCounterStats[@" + this.screenName + " no data]";
    }
    Date s = ((DateValue)this.data.get(0)).date;
    Date e = ((DateValue)this.data.get(this.data.size() - 1)).date;
    return "TwitterCounterStats[@" + this.screenName + " " + this.data.size() + " pts from " + s + " to " + e + "]";
  }
  
  public static final class DateValue
    implements Comparable<DateValue>
  {
    public final int value;
    public final Date date;
    
    DateValue(Date date, int v)
    {
      this.date = date;
      this.value = v;
    }
    
    public String toString()
    {
      return this.date + ": " + this.value;
    }
    
    public int compareTo(DateValue o)
    {
      return this.date.compareTo(o.date);
    }
  }
  
  static final SimpleDateFormat format = new SimpleDateFormat("'date'yyyy-MM-dd");
  static final SimpleDateFormat duformat = new SimpleDateFormat("yyyy-MM-dd");
  public final String website;
  
  TwitterCounterStats(JSONObject jo)
    throws JSONException, ParseException
  {
    this.screenName = jo.getString("username");
    this.dateUpdated = duformat.parse(jo.getString("date_updated"));
    this.followDays = jo.getInt("follow_days");
    this.avgGrowth = jo.getDouble("average_growth");
    this.website = jo.optString("url");
    this.rank = jo.getLong("rank");
    Map<String, ?> perdate = jo.getJSONObject("followersperdate").getMap();
    this.data = new ArrayList(perdate.size());
    for (String key : perdate.keySet())
    {
      Date date = format.parse(key);
      int v = ((Integer)perdate.get(key)).intValue();
      this.data.add(new DateValue(date, v));
    }
    Collections.sort(this.data);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\ecosystem\TwitterCounterStats.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */