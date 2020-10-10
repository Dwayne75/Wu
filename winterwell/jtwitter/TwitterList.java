package winterwell.jtwitter;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import winterwell.json.JSONArray;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public class TwitterList
  extends AbstractList<User>
{
  private boolean _private;
  
  public static TwitterList get(String ownerScreenName, String slug, Twitter jtwit)
  {
    return new TwitterList(ownerScreenName, slug, jtwit);
  }
  
  public static TwitterList get(Number id, Twitter jtwit)
  {
    return new TwitterList(id, jtwit);
  }
  
  private long cursor = -1L;
  private String description;
  private final Twitter.IHttpClient http;
  private Number id;
  private final Twitter jtwit;
  private int memberCount = -1;
  private String name;
  private User owner;
  private String slug;
  private int subscriberCount;
  private final List<User> users = new ArrayList();
  
  TwitterList(JSONObject json, Twitter jtwit)
    throws JSONException
  {
    this.jtwit = jtwit;
    this.http = jtwit.getHttpClient();
    init2(json);
  }
  
  @Deprecated
  public TwitterList(String ownerScreenName, String slug, Twitter jtwit)
  {
    assert ((ownerScreenName != null) && (slug != null) && (jtwit != null));
    this.jtwit = jtwit;
    this.owner = new User(ownerScreenName);
    this.name = slug;
    this.slug = slug;
    this.http = jtwit.getHttpClient();
    init();
  }
  
  public TwitterList(String listName, Twitter jtwit, boolean isPublic, String description)
  {
    assert ((listName != null) && (jtwit != null));
    this.jtwit = jtwit;
    String ownerScreenName = jtwit.getScreenName();
    assert (ownerScreenName != null);
    this.name = listName;
    this.slug = listName;
    this.http = jtwit.getHttpClient();
    
    String url = jtwit.TWITTER_URL + "/lists/create.json";
    
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "name", listName, "mode", isPublic ? "public" : "private", "description", description });
    String json = this.http.post(url, vars, true);
    try
    {
      JSONObject jobj = new JSONObject(json);
      init2(jobj);
    }
    catch (JSONException e)
    {
      throw new TwitterException("Could not parse response: " + e);
    }
  }
  
  public TwitterList(Number id, Twitter jtwit)
  {
    assert ((id != null) && (jtwit != null));
    this.jtwit = jtwit;
    this.id = id;
    this.http = jtwit.getHttpClient();
    init();
  }
  
  public boolean add(User user)
  {
    if (this.users.contains(user)) {
      return false;
    }
    String url = this.jtwit.TWITTER_URL + "/lists/members/create.json";
    Map map = getListVars();
    map.put("screen_name", user.screenName);
    String json = this.http.post(url, map, true);
    try
    {
      JSONObject jobj = new JSONObject(json);
      this.memberCount = jobj.getInt("member_count");
      
      this.users.add(user);
      return true;
    }
    catch (JSONException e)
    {
      throw new TwitterException("Could not parse response: " + e);
    }
  }
  
  public boolean addAll(Collection<? extends User> newUsers)
  {
    List newUsersList = new ArrayList(newUsers);
    newUsersList.removeAll(this.users);
    if (newUsersList.size() == 0) {
      return false;
    }
    String url = this.jtwit.TWITTER_URL + "/lists/members/create_all.json";
    Map map = getListVars();
    int batchSize = 100;
    for (int i = 0; i < this.users.size(); i += batchSize)
    {
      int last = i + batchSize;
      String names = InternalUtils.join(newUsersList, i, last);
      map.put("screen_name", names);
      String json = this.http.post(url, map, true);
      try
      {
        JSONObject jobj = new JSONObject(json);
        this.memberCount = jobj.getInt("member_count");
      }
      catch (JSONException e)
      {
        throw new TwitterException("Could not parse response: " + e);
      }
    }
    return true;
  }
  
  public void delete()
  {
    String URL = this.jtwit.TWITTER_URL + "/lists/destroy.json";
    this.http.post(URL, getListVars(), true);
  }
  
  public User get(int index)
  {
    String url = this.jtwit.TWITTER_URL + "/lists/members.json";
    Map<String, String> vars = getListVars();
    while ((this.users.size() < index + 1) && (this.cursor != 0L))
    {
      vars.put("cursor", Long.toString(this.cursor));
      String json = this.http.getPage(url, vars, true);
      try
      {
        JSONObject jobj = new JSONObject(json);
        JSONArray jarr = (JSONArray)jobj.get("users");
        List<User> users1page = User.getUsers(jarr.toString());
        this.users.addAll(users1page);
        this.cursor = new Long(jobj.getString("next_cursor")).longValue();
      }
      catch (JSONException e)
      {
        throw new TwitterException("Could not parse user list" + e);
      }
    }
    return (User)this.users.get(index);
  }
  
  public String getDescription()
  {
    init();
    return this.description;
  }
  
  private Map<String, String> getListVars()
  {
    Map vars = new HashMap();
    if (this.id != null)
    {
      vars.put("list_id", this.id);
      return vars;
    }
    vars.put("owner_screen_name", this.owner.screenName);
    vars.put("slug", this.slug);
    return vars;
  }
  
  public Number getId()
  {
    return this.id;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public User getOwner()
  {
    return this.owner;
  }
  
  public List<Status> getStatuses()
    throws TwitterException
  {
    Map vars = getListVars();
    
    this.jtwit.addStandardishParameters(vars);
    
    String url = this.jtwit.TWITTER_URL + "/lists/statuses.json";
    return this.jtwit.getStatuses(url, vars, true);
  }
  
  public int getSubscriberCount()
  {
    init();
    return this.subscriberCount;
  }
  
  public List<User> getSubscribers()
  {
    String url = this.jtwit.TWITTER_URL + "/lists/subscribers.json";
    Map<String, String> vars = getListVars();
    String json = this.http.getPage(url, vars, true);
    try
    {
      JSONObject jobj = new JSONObject(json);
      JSONArray jsonUsers = jobj.getJSONArray("users");
      return User.getUsers2(jsonUsers);
    }
    catch (JSONException e)
    {
      throw new TwitterException("Could not parse response: " + e);
    }
  }
  
  private void init()
  {
    if (this.memberCount != -1) {
      return;
    }
    String url = this.jtwit.TWITTER_URL + "/lists/show.json";
    Map<String, String> vars = getListVars();
    String json = this.http.getPage(url, vars, true);
    try
    {
      JSONObject jobj = new JSONObject(json);
      init2(jobj);
    }
    catch (JSONException e)
    {
      throw new TwitterException("Could not parse response: " + e);
    }
  }
  
  private void init2(JSONObject jobj)
    throws JSONException
  {
    this.memberCount = jobj.getInt("member_count");
    this.subscriberCount = jobj.getInt("subscriber_count");
    this.name = jobj.getString("name");
    this.slug = jobj.getString("slug");
    this.id = Long.valueOf(jobj.getLong("id"));
    this._private = "private".equals(jobj.optString("mode"));
    this.description = jobj.optString("description");
    JSONObject user = jobj.getJSONObject("user");
    this.owner = new User(user, null);
  }
  
  public boolean isPrivate()
  {
    init();
    return this._private;
  }
  
  public boolean remove(Object o)
  {
    try
    {
      User user = (User)o;
      String url = this.jtwit.TWITTER_URL + "/lists/members/destroy.json";
      Map map = getListVars();
      map.put("screen_name", user.screenName);
      String json = this.http.post(url, map, true);
      
      JSONObject jobj = new JSONObject(json);
      this.memberCount = jobj.getInt("member_count");
      
      this.users.remove(user);
      return true;
    }
    catch (JSONException e)
    {
      throw new TwitterException("Could not parse response: " + e);
    }
  }
  
  public void setDescription(String description)
  {
    String url = this.jtwit.TWITTER_URL + "/lists/update.json";
    Map<String, String> vars = getListVars();
    vars.put("description", description);
    String json = this.http.getPage(url, vars, true);
    try
    {
      JSONObject jobj = new JSONObject(json);
      init2(jobj);
    }
    catch (JSONException e)
    {
      throw new TwitterException("Could not parse response: " + e);
    }
  }
  
  public void setPrivate(boolean isPrivate)
  {
    String url = this.jtwit.TWITTER_URL + "/lists/update.json";
    Map<String, String> vars = getListVars();
    vars.put("mode", isPrivate ? "private" : "public");
    String json = this.http.getPage(url, vars, true);
    try
    {
      JSONObject jobj = new JSONObject(json);
      init2(jobj);
    }
    catch (JSONException e)
    {
      throw new TwitterException("Could not parse response: " + e);
    }
  }
  
  public int size()
  {
    init();
    return this.memberCount;
  }
  
  public String toString()
  {
    return getClass().getSimpleName() + "[" + this.owner + "." + this.name + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\TwitterList.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */