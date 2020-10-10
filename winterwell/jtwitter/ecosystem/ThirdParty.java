package winterwell.jtwitter.ecosystem;

import winterwell.json.JSONException;
import winterwell.json.JSONObject;
import winterwell.jtwitter.InternalUtils;
import winterwell.jtwitter.Twitter.IHttpClient;
import winterwell.jtwitter.TwitterException.Parsing;
import winterwell.jtwitter.URLConnectionHttpClient;
import winterwell.jtwitter.User;

public class ThirdParty
{
  private Twitter.IHttpClient client;
  
  public ThirdParty()
  {
    this(new URLConnectionHttpClient());
  }
  
  public ThirdParty(Twitter.IHttpClient client)
  {
    this.client = client;
  }
  
  public double getInfochimpTrustRank(User user, String apiKey)
  {
    String json = this.client.getPage(
      "http://api.infochimps.com/soc/net/tw/trstrank.json", 
      InternalUtils.asMap(new Object[] {"screen_name", user.screenName, "apikey", 
      apiKey }), false);
    try
    {
      JSONObject results = new JSONObject(json);
      Double score = Double.valueOf(results.getDouble("trstrank"));
      return score.doubleValue();
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\ecosystem\ThirdParty.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */