package winterwell.jtwitter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import winterwell.json.JSONArray;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public class Twitter_Account
{
  public static enum KAccessLevel
  {
    NONE,  READ_ONLY,  READ_WRITE,  READ_WRITE_DM;
  }
  
  public static class Search
  {
    private Date createdAt;
    private Long id;
    private String query;
    
    public Search(Long id, Date createdAt, String query)
    {
      this.id = id;
      this.createdAt = createdAt;
      this.query = query;
    }
    
    public Date getCreatedAt()
    {
      return this.createdAt;
    }
    
    public Long getId()
    {
      return this.id;
    }
    
    public String getText()
    {
      return this.query;
    }
  }
  
  public static String COLOR_BG = "profile_background_color";
  public static String COLOR_LINK = "profile_link_color";
  public static String COLOR_SIDEBAR_BORDER = "profile_sidebar_border_color";
  public static String COLOR_SIDEBAR_FILL = "profile_sidebar_fill_color";
  public static String COLOR_TEXT = "profile_text_color";
  private KAccessLevel accessLevel;
  final Twitter jtwit;
  
  public Twitter_Account(Twitter jtwit)
  {
    assert (jtwit.getHttpClient().canAuthenticate()) : jtwit;
    this.jtwit = jtwit;
  }
  
  public Map<String, RateLimit> getRateLimits()
  {
    return ((URLConnectionHttpClient)this.jtwit.getHttpClient()).updateRateLimits();
  }
  
  public Search createSavedSearch(String query)
  {
    String url = this.jtwit.TWITTER_URL + "saved_searches/create.json";
    Map vars = InternalUtils.asMap(new Object[] { "query", query });
    String json = this.jtwit.getHttpClient().post(url, vars, true);
    try
    {
      return makeSearch(new JSONObject(json));
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  public Search destroySavedSearch(Long id)
  {
    String url = this.jtwit.TWITTER_URL + "saved_searches/destroy/" + id + 
      ".json";
    String json = this.jtwit.getHttpClient().post(url, null, true);
    try
    {
      return makeSearch(new JSONObject(json));
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  /**
   * @deprecated
   */
  public KAccessLevel getAccessLevel()
  {
    if (this.accessLevel != null) {
      return this.accessLevel;
    }
    try
    {
      verifyCredentials();
      return this.accessLevel;
    }
    catch (TwitterException.E401 e) {}
    return KAccessLevel.NONE;
  }
  
  public List<Search> getSavedSearches()
  {
    String url = this.jtwit.TWITTER_URL + "saved_searches.json";
    String json = this.jtwit.getHttpClient().getPage(url, null, true);
    try
    {
      JSONArray ja = new JSONArray(json);
      List<Search> searches = new ArrayList();
      for (int i = 0; i < ja.length(); i++)
      {
        JSONObject jo = ja.getJSONObject(i);
        Search search = makeSearch(jo);
        searches.add(search);
      }
      return searches;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  private Search makeSearch(JSONObject jo)
    throws JSONException
  {
    Date createdAt = InternalUtils.parseDate(jo
      .getString("created_at"));
    Long id = Long.valueOf(jo.getLong("id"));
    String query = jo.getString("query");
    Search search = new Search(id, createdAt, query);
    return search;
  }
  
  public User setProfile(String name, String url, String location, String description)
  {
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "name", name, "url", url, "location", location, "description", description });
    String apiUrl = this.jtwit.TWITTER_URL + "/account/update_profile.json";
    String json = this.jtwit.getHttpClient().post(apiUrl, vars, true);
    return InternalUtils.user(json);
  }
  
  public User setProfileColors(Map<String, String> colorName2hexCode)
  {
    assert (colorName2hexCode.size() != 0);
    String url = this.jtwit.TWITTER_URL + "/account/update_profile_colors.json";
    String json = this.jtwit.getHttpClient().post(url, colorName2hexCode, true);
    return InternalUtils.user(json);
  }
  
  public String toString()
  {
    return "TwitterAccount[" + this.jtwit.getScreenName() + "]";
  }
  
  public User verifyCredentials()
    throws TwitterException.E401
  {
    String url = this.jtwit.TWITTER_URL + "/account/verify_credentials.json";
    String json = this.jtwit.getHttpClient().getPage(url, null, true);
    
    Twitter.IHttpClient client = this.jtwit.getHttpClient();
    String al = client.getHeader("X-Access-Level");
    if (al != null)
    {
      if ("read".equals(al)) {
        this.accessLevel = KAccessLevel.READ_ONLY;
      }
      if ("read-write".equals(al)) {
        this.accessLevel = KAccessLevel.READ_WRITE;
      }
      if ("read-write-directmessages".equals(al)) {
        this.accessLevel = KAccessLevel.READ_WRITE_DM;
      }
    }
    User self = InternalUtils.user(json);
    
    this.jtwit.self = self;
    return self;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\Twitter_Account.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */