package winterwell.jtwitter.ecosystem;

import java.util.Map;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;
import winterwell.jtwitter.InternalUtils;
import winterwell.jtwitter.Twitter.IHttpClient;
import winterwell.jtwitter.TwitterException.Parsing;
import winterwell.jtwitter.URLConnectionHttpClient;

public class Topsy
{
  public Topsy() {}
  
  public static final class UrlInfo
  {
    public final String title;
    public final int linkCount;
    public final String desc;
    public final String url;
    
    public UrlInfo(JSONObject resp)
      throws JSONException
    {
      this.url = resp.getString("url");
      this.title = resp.getString("title");
      this.linkCount = resp.getInt("trackback_total");
      this.desc = resp.getString("description");
    }
    
    public String toString()
    {
      return this.url + " " + this.linkCount + " " + this.title;
    }
  }
  
  private Twitter.IHttpClient client = new URLConnectionHttpClient();
  private String apikey;
  
  public Topsy(String apiKey)
  {
    this.apikey = apiKey;
  }
  
  public UrlInfo getUrlInfo(String url)
  {
    Map vars = InternalUtils.asMap(new Object[] { "url", url });
    if (this.apikey != null) {
      vars.put("apikey", this.apikey);
    }
    String json = this.client.getPage("http://otter.topsy.com/urlinfo.json", vars, false);
    try
    {
      JSONObject jo = new JSONObject(json);
      JSONObject resp = jo.getJSONObject("response");
      return new UrlInfo(resp);
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\ecosystem\Topsy.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */