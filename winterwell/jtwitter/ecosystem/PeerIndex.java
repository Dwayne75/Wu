package winterwell.jtwitter.ecosystem;

import java.util.Map;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;
import winterwell.jtwitter.InternalUtils;
import winterwell.jtwitter.Twitter.IHttpClient;
import winterwell.jtwitter.TwitterException.Parsing;
import winterwell.jtwitter.URLConnectionHttpClient;
import winterwell.jtwitter.User;

public class PeerIndex
{
  final String API_KEY;
  
  public PeerIndex(String apiKey)
  {
    this.API_KEY = apiKey;
  }
  
  Twitter.IHttpClient client = new URLConnectionHttpClient();
  
  public PeerIndexProfile getProfile(User user)
  {
    Map vars = InternalUtils.asMap(new Object[] { user.screenName == null ? "twitter_screen_name" : "twitter_id", user.screenName == null ? user.id : user.screenName, "api_key", this.API_KEY });
    
    String json = this.client.getPage("https://api.peerindex.com/1/actor/basic.json", 
      vars, false);
    try
    {
      JSONObject jo = new JSONObject(json);
      return new PeerIndexProfile(jo);
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\ecosystem\PeerIndex.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */