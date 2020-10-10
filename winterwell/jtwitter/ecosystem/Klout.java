package winterwell.jtwitter.ecosystem;

import java.util.HashMap;
import java.util.Map;
import winterwell.json.JSONArray;
import winterwell.json.JSONObject;
import winterwell.jtwitter.InternalUtils;
import winterwell.jtwitter.Twitter.IHttpClient;
import winterwell.jtwitter.URLConnectionHttpClient;

public class Klout
{
  final String API_KEY;
  
  public Klout(String apiKey)
  {
    this.API_KEY = apiKey;
  }
  
  Twitter.IHttpClient client = new URLConnectionHttpClient();
  
  public Map<String, Double> getScore(String... userNames)
  {
    String unames = InternalUtils.join(userNames);
    Map vars = InternalUtils.asMap(new Object[] { "key", this.API_KEY, "users", unames });
    String json = this.client.getPage("http://api.klout.com/1/klout.json", vars, false);
    JSONObject jo = new JSONObject(json);
    JSONArray users = jo.getJSONArray("users");
    Map<String, Double> scores = new HashMap(users.length());
    int i = 0;
    for (int n = users.length(); i < n; i++)
    {
      JSONObject u = users.getJSONObject(i);
      scores.put(u.getString("twitter_screen_name"), Double.valueOf(u.getDouble("kscore")));
    }
    return scores;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\ecosystem\Klout.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */