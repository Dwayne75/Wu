package winterwell.jtwitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import winterwell.json.JSONArray;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public class Twitter_Users
{
  private final Twitter.IHttpClient http;
  private final Twitter jtwit;
  
  Twitter_Users(Twitter jtwit)
  {
    this.jtwit = jtwit;
    this.http = jtwit.getHttpClient();
  }
  
  public User block(String screenName)
  {
    HashMap vars = new HashMap();
    vars.put("screen_name", screenName);
    
    String json = this.http.post(this.jtwit.TWITTER_URL + "/blocks/create.json", 
      vars, true);
    return InternalUtils.user(json);
  }
  
  List<User> bulkShow2(String apiMethod, Class stringOrNumber, Collection screenNamesOrIds)
  {
    boolean auth = InternalUtils.authoriseIn11(this.jtwit);
    int batchSize = 100;
    ArrayList<User> users = new ArrayList(screenNamesOrIds.size());
    List _screenNamesOrIds = (screenNamesOrIds instanceof List) ? (List)screenNamesOrIds : 
      new ArrayList(screenNamesOrIds);
    for (int i = 0; i < _screenNamesOrIds.size(); i += batchSize)
    {
      int last = i + batchSize;
      String names = InternalUtils.join(_screenNamesOrIds, i, last);
      String var = stringOrNumber == String.class ? "screen_name" : 
        "user_id";
      Map<String, String> vars = InternalUtils.asMap(new Object[] { var, names });
      try
      {
        String json = this.http.getPage(this.jtwit.TWITTER_URL + apiMethod, vars, auth);
        List<User> usersi = User.getUsers(json);
        users.addAll(usersi);
      }
      catch (TwitterException.E404 localE404) {}catch (TwitterException e)
      {
        if (users.size() == 0) {
          throw e;
        }
        e.printStackTrace();
        break;
      }
    }
    return users;
  }
  
  public User follow(String username)
    throws TwitterException
  {
    if (username == null) {
      throw new NullPointerException();
    }
    if (username.equals(this.jtwit.getScreenName())) {
      throw new IllegalArgumentException("follow yourself makes no sense");
    }
    String page = null;
    try
    {
      Map<String, String> vars = InternalUtils.asMap(new Object[] { "screen_name", username });
      page = this.http.post(this.jtwit.TWITTER_URL + "/friendships/create.json", 
        vars, true);
      
      return new User(new JSONObject(page), null);
    }
    catch (TwitterException.SuspendedUser e)
    {
      throw e;
    }
    catch (TwitterException.Repetition e)
    {
      return null;
    }
    catch (TwitterException.E403 e)
    {
      try
      {
        if (isFollowing(username)) {
          return null;
        }
      }
      catch (TwitterException localTwitterException) {}
      throw e;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(page, e);
    }
  }
  
  public User follow(User user)
  {
    return follow(user.screenName);
  }
  
  public List<Number> getBlockedIds()
  {
    String json = this.http.getPage(this.jtwit.TWITTER_URL + 
      "/blocks/ids.json", null, true);
    try
    {
      JSONArray arr = json.startsWith("[") ? new JSONArray(json) : 
        new JSONObject(json).getJSONArray("ids");
      List<Number> ids = new ArrayList(arr.length());
      int i = 0;
      for (int n = arr.length(); i < n; i++) {
        ids.add(Long.valueOf(arr.getLong(i)));
      }
      return ids;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  public List<Number> getFollowerIDs()
    throws TwitterException
  {
    return getUserIDs(this.jtwit.TWITTER_URL + "/followers/ids.json", null, null);
  }
  
  public List<Number> getFollowerIDs(String screenName)
    throws TwitterException
  {
    return getUserIDs(this.jtwit.TWITTER_URL + "/followers/ids.json", screenName, null);
  }
  
  public List<Number> getFollowerIDs(long userId)
    throws TwitterException
  {
    return getUserIDs(this.jtwit.TWITTER_URL + "/followers/ids.json", null, Long.valueOf(userId));
  }
  
  @Deprecated
  public List<User> getFollowers()
    throws TwitterException
  {
    List<Number> ids = getFollowerIDs();
    return getTweeps2(ids);
  }
  
  /**
   * @deprecated
   */
  public List<User> getFollowers(String username)
    throws TwitterException
  {
    List<Number> ids = getFollowerIDs(username);
    return getTweeps2(ids);
  }
  
  public List<Number> getFriendIDs()
    throws TwitterException
  {
    return getUserIDs(this.jtwit.TWITTER_URL + "/friends/ids.json", null, null);
  }
  
  public List<Number> getFriendIDs(String screenName)
    throws TwitterException
  {
    return getUserIDs(this.jtwit.TWITTER_URL + "/friends/ids.json", screenName, null);
  }
  
  public List<Number> getFriendIDs(long userId)
    throws TwitterException
  {
    return getUserIDs(this.jtwit.TWITTER_URL + "/friends/ids.json", null, Long.valueOf(userId));
  }
  
  @Deprecated
  public List<User> getFriends()
    throws TwitterException
  {
    List<Number> ids = getFriendIDs();
    return getTweeps2(ids);
  }
  
  /**
   * @deprecated
   */
  public List<User> getFriends(String username)
    throws TwitterException
  {
    List<Number> ids = getFriendIDs(username);
    return getTweeps2(ids);
  }
  
  /**
   * @deprecated
   */
  private List<User> getTweeps2(List<Number> ids)
  {
    if (ids.size() > 100) {
      ids = ids.subList(0, 100);
    }
    List<User> users = showById(ids);
    return users;
  }
  
  public List<User> getRelationshipInfo(List<String> screenNames)
  {
    if (screenNames.size() == 0) {
      return Collections.EMPTY_LIST;
    }
    List<User> users = bulkShow2("/friendships/lookup.json", String.class, 
      screenNames);
    return users;
  }
  
  public List<User> getRelationshipInfoById(List<? extends Number> userIDs)
  {
    if (userIDs.size() == 0) {
      return Collections.EMPTY_LIST;
    }
    List<User> users = bulkShow2("/friendships/lookup.json", Number.class, 
      userIDs);
    return users;
  }
  
  public User getUser(long userId)
  {
    return show(Long.valueOf(userId));
  }
  
  public User getUser(String screenName)
  {
    return show(screenName);
  }
  
  private List<Number> getUserIDs(String url, String screenName, Long userId)
  {
    Long cursor = Long.valueOf(-1L);
    List<Number> ids = new ArrayList();
    if ((screenName != null) && (userId != null)) {
      throw new IllegalArgumentException("cannot use both screen_name and user_id when fetching user_ids");
    }
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "screen_name", screenName, "user_id", userId });
    while (!this.jtwit.enoughResults(ids))
    {
      vars.put("cursor", String.valueOf(cursor));
      String json = this.http.getPage(url, vars, this.http.canAuthenticate());
      try
      {
        JSONArray jarr;
        if (json.charAt(0) == '[')
        {
          JSONArray jarr = new JSONArray(json);
          cursor = Long.valueOf(0L);
        }
        else
        {
          JSONObject jobj = new JSONObject(json);
          jarr = (JSONArray)jobj.get("ids");
          cursor = new Long(jobj.getString("next_cursor"));
        }
        for (int i = 0; i < jarr.length(); i++) {
          ids.add(Long.valueOf(jarr.getLong(i)));
        }
        if ((jarr.length() == 0) || (cursor.longValue() == 0L)) {
          break;
        }
      }
      catch (JSONException e)
      {
        throw new TwitterException.Parsing(json, e);
      }
    }
    return ids;
  }
  
  private List<User> getUsers(String url, String screenName)
  {
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "screen_name", screenName });
    List<User> users = new ArrayList();
    Long cursor = Long.valueOf(-1L);
    while ((cursor.longValue() != 0L) && (!this.jtwit.enoughResults(users)))
    {
      vars.put("cursor", cursor.toString());
      try
      {
        JSONObject jobj = new JSONObject(this.http.getPage(url, vars, 
          this.http.canAuthenticate()));
        users.addAll(User.getUsers(jobj.getString("users")));
        cursor = new Long(jobj.getString("next_cursor"));
      }
      catch (JSONException e)
      {
        throw new TwitterException.Parsing(null, e);
      }
    }
    return users;
  }
  
  public boolean isBlocked(Long userId)
  {
    try
    {
      HashMap vars = new HashMap();
      vars.put("user_id", Long.toString(userId.longValue()));
      
      String json = this.http.getPage(this.jtwit.TWITTER_URL + 
        "/blocks/exists.json", vars, true);
      return true;
    }
    catch (TwitterException.E404 e) {}
    return false;
  }
  
  public boolean isBlocked(String screenName)
  {
    try
    {
      HashMap vars = new HashMap();
      vars.put("screen_name", screenName);
      
      String json = this.http.getPage(this.jtwit.TWITTER_URL + 
        "/blocks/exists.json", vars, true);
      return true;
    }
    catch (TwitterException.E404 e) {}
    return false;
  }
  
  public boolean isFollower(String userB)
  {
    return isFollower(userB, this.jtwit.getScreenName());
  }
  
  public boolean isFollower(String followerScreenName, String followedScreenName)
  {
    assert ((followerScreenName != null) && (followedScreenName != null));
    try
    {
      Map vars = InternalUtils.asMap(new Object[] { "source_screen_name", followerScreenName, "target_screen_name", followedScreenName });
      String page = this.http.getPage(this.jtwit.TWITTER_URL + 
        "/friendships/show.json", vars, this.http.canAuthenticate());
      JSONObject jo = new JSONObject(page);
      JSONObject trgt = jo.getJSONObject("relationship").getJSONObject("target");
      return trgt.getBoolean("followed_by");
    }
    catch (TwitterException.E403 e)
    {
      if ((e instanceof TwitterException.SuspendedUser)) {
        throw e;
      }
      String whoFirst = followedScreenName.equals(this.jtwit.getScreenName()) ? followerScreenName : 
        followedScreenName;
      try
      {
        show(whoFirst);
        String whoSecond = whoFirst.equals(followedScreenName) ? followerScreenName : 
          followedScreenName;
        if (whoSecond.equals(this.jtwit.getScreenName())) {
          throw e;
        }
        show(whoSecond);
      }
      catch (TwitterException.RateLimit localRateLimit) {}
      throw e;
    }
    catch (TwitterException e)
    {
      if (e.getMessage() != null) {
        if (e.getMessage().contains("Two user ids or screen_names must be supplied")) {
          throw new TwitterException("WTF? inputs: follower=" + 
            followerScreenName + ", followed=" + 
            followedScreenName + ", call-by=" + 
            this.jtwit.getScreenName() + "; " + e.getMessage());
        }
      }
      throw e;
    }
  }
  
  public boolean isFollowing(String userB)
  {
    return isFollower(this.jtwit.getScreenName(), userB);
  }
  
  public boolean isFollowing(User user)
  {
    return isFollowing(user.screenName);
  }
  
  /**
   * @deprecated
   */
  public User leaveNotifications(String screenName)
  {
    return setNotifications(screenName, Boolean.valueOf(false), null);
  }
  
  public User setNotifications(String screenName, Boolean device, Boolean retweets)
  {
    if ((device == null) && (retweets == null)) {
      return null;
    }
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "screen_name", screenName, "device", device, "retweets", retweets });
    String page = this.http.post(this.jtwit.TWITTER_URL + 
      "/friendships/update.json", vars, true);
    try
    {
      JSONObject jo = new JSONObject(page).getJSONObject("relationship").getJSONObject("target");
      return new User(jo, null);
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(page, e);
    }
  }
  
  public User notify(String username)
  {
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "screen_name", username });
    String page = this.http.getPage(this.jtwit.TWITTER_URL + 
      "/notifications/follow.json", vars, true);
    try
    {
      return new User(new JSONObject(page), null);
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(page, e);
    }
  }
  
  public User reportSpammer(String screenName)
  {
    HashMap vars = new HashMap();
    vars.put("screen_name", screenName);
    
    String json = this.http.post(this.jtwit.TWITTER_URL + "/report_spam.json", vars, 
      true);
    return InternalUtils.user(json);
  }
  
  public List<User> searchUsers(String searchTerm)
  {
    return searchUsers(searchTerm, 0);
  }
  
  public List<User> searchUsers(String searchTerm, int page)
  {
    assert (searchTerm != null);
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "q", searchTerm });
    if (page > 1) {
      vars.put("page", Integer.toString(page));
    }
    if ((this.jtwit.count != null) && (this.jtwit.count.intValue() < 20)) {
      vars.put("per_page", String.valueOf(this.jtwit.count));
    }
    String json = this.http.getPage(this.jtwit.TWITTER_URL + "/users/search.json", 
      vars, true);
    List<User> users = User.getUsers(json);
    return users;
  }
  
  public List<User> show(Collection<String> screenNames)
  {
    if (screenNames.size() == 0) {
      return Collections.EMPTY_LIST;
    }
    return bulkShow2("/users/lookup.json", String.class, screenNames);
  }
  
  public User show(Number userId)
  {
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "user_id", userId.toString() });
    String json = this.http.getPage(this.jtwit.TWITTER_URL + "/users/show.json", 
      vars, this.http.canAuthenticate());
    try
    {
      return new User(new JSONObject(json), null);
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  public User show(String screenName)
    throws TwitterException, TwitterException.SuspendedUser
  {
    Map vars = InternalUtils.asMap(new Object[] { "screen_name", screenName });
    
    String json = "";
    try
    {
      json = this.http.getPage(this.jtwit.TWITTER_URL + "/users/show.json", 
        vars, this.http.canAuthenticate());
    }
    catch (Exception e)
    {
      throw new TwitterException.E404("User " + screenName + 
        " does not seem to exist, their user account may have been removed from the service");
    }
    if (json.length() == 0) {
      throw new TwitterException.E404(screenName + 
        " does not seem to exist");
    }
    try
    {
      return new User(new JSONObject(json), null);
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  public List<User> showById(Collection<? extends Number> userIds)
  {
    if (userIds.size() == 0) {
      return Collections.EMPTY_LIST;
    }
    return bulkShow2("/users/lookup.json", Number.class, userIds);
  }
  
  public User stopFollowing(String username)
  {
    try
    {
      Map<String, String> vars = InternalUtils.asMap(new Object[] { "screen_name", username });
      page = this.jtwit.http.post(this.jtwit.TWITTER_URL + 
        "/friendships/destroy.json", vars, true);
    }
    catch (TwitterException e)
    {
      String page;
      if ((e.getMessage() != null) && 
        (e.getMessage().contains("not friends"))) {
        return null;
      }
      throw e;
    }
    try
    {
      return new User(new JSONObject(page), null);
    }
    catch (JSONException e)
    {
      String page;
      throw new TwitterException.Parsing(page, e);
    }
  }
  
  public User stopFollowing(User user)
  {
    return stopFollowing(user.screenName);
  }
  
  public User unblock(String screenName)
  {
    HashMap vars = new HashMap();
    vars.put("screen_name", screenName);
    
    String json = this.http.post(this.jtwit.TWITTER_URL + "/blocks/destroy.json", 
      vars, true);
    return InternalUtils.user(json);
  }
  
  public boolean userExists(String screenName)
  {
    try
    {
      show(screenName);
    }
    catch (TwitterException.SuspendedUser e)
    {
      return false;
    }
    catch (TwitterException.E404 e)
    {
      return false;
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\Twitter_Users.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */