package winterwell.jtwitter;

import java.util.Date;
import winterwell.json.JSONObject;

public final class RateLimit
{
  public static final String RES_STREAM_USER = "/stream/user";
  public static final String RES_STREAM_KEYWORD = "/stream/keyword";
  public static final String RES_USERS_BULK_SHOW = "/users/lookup";
  public static final String RES_USERS_SHOW1 = "/users/show";
  public static final String RES_USER_TIMELINE = "/statuses/user_timeline";
  public static final String RES_SEARCH = "/search/tweets";
  public static final String RES_STATUS_SHOW = "/statuses/show";
  public static final String RES_USERS_SEARCH = "/users/search";
  public static final String RES_FRIENDSHIPS_SHOW = "/friendships/show";
  public static final String RES_TRENDS = "/trends/place";
  public static final String RES_LISTS_SHOW = "/lists/show";
  private String limit;
  private String remaining;
  private String reset;
  
  public RateLimit(String limit, String remaining, String reset)
  {
    this.limit = limit;
    this.remaining = remaining;
    this.reset = reset;
  }
  
  RateLimit(JSONObject jrl)
  {
    this(jrl.getString("limit"), jrl.getString("remaining"), jrl.getString("reset"));
  }
  
  public int getLimit()
  {
    return Integer.valueOf(this.limit).intValue();
  }
  
  public int getRemaining()
  {
    return Integer.valueOf(this.remaining).intValue();
  }
  
  public Date getReset()
  {
    return InternalUtils.parseDate(this.reset);
  }
  
  public boolean isOutOfDate()
  {
    return getReset().getTime() < System.currentTimeMillis();
  }
  
  public String toString()
  {
    return this.remaining;
  }
  
  public void waitForReset()
  {
    Long r = Long.valueOf(this.reset);
    long now = System.currentTimeMillis();
    long wait = r.longValue() - now;
    if (wait < 0L) {
      return;
    }
    try
    {
      Thread.sleep(wait);
    }
    catch (InterruptedException e)
    {
      throw new TwitterException(e);
    }
  }
  
  public static String getResource(String url)
  {
    if (!url.startsWith("https://api.twitter.com/1.1")) {
      return null;
    }
    int s = "https://api.twitter.com/1.1".length();
    int e = url.indexOf(".json", s);
    if (e == -1) {
      return null;
    }
    int e1 = url.indexOf("/", s + 1);
    if ((e1 == -1) || (e1 > e)) {
      return url.substring(s, e);
    }
    int e2 = url.indexOf("/", e1 + 1);
    if ((e2 == -1) || (e2 > e)) {
      return url.substring(s, e);
    }
    return url.substring(s, e2);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\RateLimit.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */