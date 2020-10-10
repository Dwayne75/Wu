package winterwell.jtwitter.ecosystem;

import java.text.ParseException;
import java.util.Map;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;
import winterwell.jtwitter.InternalUtils;
import winterwell.jtwitter.Twitter.IHttpClient;
import winterwell.jtwitter.TwitterException.Parsing;
import winterwell.jtwitter.URLConnectionHttpClient;

public class TwitterCounter
{
  final String apiKey;
  Twitter.IHttpClient client = new URLConnectionHttpClient();
  
  /**
   * @deprecated
   */
  public Twitter.IHttpClient getClient()
  {
    return this.client;
  }
  
  public TwitterCounter(String twitterCounterApiKey)
  {
    this.apiKey = twitterCounterApiKey;
  }
  
  public TwitterCounterStats getStats(Number twitterUserId)
  {
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "twitter_id", twitterUserId, "apikey", this.apiKey });
    
    String json = this.client.getPage("http://api.twittercounter.com/", vars, false);
    try
    {
      JSONObject jo = new JSONObject(json);
      return new TwitterCounterStats(jo);
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
    catch (ParseException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\ecosystem\TwitterCounter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */