package winterwell.jtwitter;

import java.util.Map;
import winterwell.json.JSONObject;

public class Twitter_Analytics
{
  private Twitter.IHttpClient http;
  
  Twitter_Analytics(Twitter.IHttpClient http)
  {
    this.http = http;
  }
  
  public int getUrlCount(String url)
  {
    Map vars = InternalUtils.asMap(new Object[] { "url", url });
    String json = this.http.getPage("http://urls.api.twitter.com/1/urls/count.json", 
      vars, false);
    JSONObject jo = new JSONObject(json);
    return jo.getInt("count");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\Twitter_Analytics.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */