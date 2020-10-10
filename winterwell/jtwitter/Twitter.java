package winterwell.jtwitter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import winterwell.json.JSONArray;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;
import winterwell.jtwitter.ecosystem.TwitLonger;

public class Twitter
  implements Serializable
{
  public static boolean CASE_SENSITIVE_SCREENNAMES;
  
  public static Map<String, Double> getAPIStatus()
    throws Exception
  {
    HashMap<String, Double> map = new HashMap();
    
    String json = null;
    try
    {
      URLConnectionHttpClient client = new URLConnectionHttpClient();
      json = client.getPage("https://api.io.watchmouse.com/synth/current/39657/folder/7617/?fields=info;cur;24h.uptime", null, false);
      JSONObject jobj = new JSONObject(json);
      JSONArray jarr = jobj.getJSONArray("result");
      for (int i = 0; i < jarr.length(); i++)
      {
        JSONObject jo = jarr.getJSONObject(i);
        String name = jo.getJSONObject("info").getString("name");
        JSONObject h24 = jo.getJSONObject("24h");
        double value = h24.getDouble("uptime");
        map.put(name, Double.valueOf(value));
      }
      return map;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
    catch (Exception e) {}
    return map;
  }
  
  public static abstract interface ICallback
  {
    public abstract boolean process(List<Status> paramList);
  }
  
  public static abstract interface IHttpClient
  {
    public abstract boolean canAuthenticate();
    
    public abstract HttpURLConnection connect(String paramString, Map<String, String> paramMap, boolean paramBoolean)
      throws IOException;
    
    public abstract IHttpClient copy();
    
    public abstract String getHeader(String paramString);
    
    public abstract String getPage(String paramString, Map<String, String> paramMap, boolean paramBoolean)
      throws TwitterException;
    
    /**
     * @deprecated
     */
    public abstract RateLimit getRateLimit(Twitter.KRequestType paramKRequestType);
    
    public abstract Map<String, RateLimit> getRateLimits();
    
    public abstract String post(String paramString, Map<String, String> paramMap, boolean paramBoolean)
      throws TwitterException;
    
    public abstract HttpURLConnection post2_connect(String paramString, Map<String, String> paramMap)
      throws Exception;
    
    public abstract void setTimeout(int paramInt);
    
    public abstract boolean isRetryOnError();
    
    public abstract void setRetryOnError(boolean paramBoolean);
  }
  
  public static abstract interface ITweet
    extends Serializable
  {
    public abstract Date getCreatedAt();
    
    public abstract BigInteger getId();
    
    public abstract String getLocation();
    
    public abstract List<String> getMentions();
    
    public abstract Place getPlace();
    
    public abstract String getText();
    
    public abstract List<Twitter.TweetEntity> getTweetEntities(Twitter.KEntityType paramKEntityType);
    
    public abstract User getUser();
    
    public abstract String getDisplayText();
  }
  
  public static enum KEntityType
  {
    hashtags,  urls,  user_mentions;
  }
  
  /**
   * @deprecated
   */
  public static enum KRequestType
  {
    NORMAL("/statuses/user_timeline"),  SEARCH("/search/tweets"),  SEARCH_USERS(
      "/users/search"),  SHOW_USER("/users/show"),  UPLOAD_MEDIA("Media"),  STREAM_KEYWORD(""),  STREAM_USER("");
    
    final String rateLimit;
    
    private KRequestType(String rateLimit)
    {
      this.rateLimit = rateLimit;
    }
  }
  
  public static final class TweetEntity
    implements Serializable
  {
    private static final long serialVersionUID = 1L;
    final String display;
    public final int end;
    public final int start;
    private final Twitter.ITweet tweet;
    public final Twitter.KEntityType type;
    
    static ArrayList<TweetEntity> parse(Twitter.ITweet tweet, String rawText, Twitter.KEntityType type, JSONObject jsonEntities)
      throws JSONException
    {
      assert ((type != null) && (tweet != null) && (rawText != null) && (jsonEntities != null)) : 
        (tweet + "\t" + rawText + "\t" + type + "\t" + jsonEntities);
      try
      {
        JSONArray arr = jsonEntities.optJSONArray(type.toString());
        if ((arr == null) || (arr.length() == 0)) {
          return null;
        }
        ArrayList<TweetEntity> list = new ArrayList(
          arr.length());
        for (int i = 0; i < arr.length(); i++)
        {
          JSONObject obj = arr.getJSONObject(i);
          TweetEntity te = new TweetEntity(tweet, rawText, type, obj, list);
          list.add(te);
        }
        return list;
      }
      catch (Throwable e) {}
      return null;
    }
    
    TweetEntity(Twitter.ITweet tweet, String rawText, Twitter.KEntityType type, JSONObject obj, ArrayList<TweetEntity> previous)
      throws JSONException
    {
      this.tweet = tweet;
      this.type = type;
      switch (type)
      {
      case urls: 
        Object eu = obj.opt("expanded_url");
        this.display = (JSONObject.NULL.equals(eu) ? null : (String)eu);
        break;
      case user_mentions: 
        this.display = obj.getString("name");
        break;
      default: 
        this.display = null;
      }
      JSONArray indices = obj.getJSONArray("indices");
      int _start = indices.getInt(0);
      int _end = indices.getInt(1);
      assert ((_start >= 0) && (_end >= _start)) : obj;
      
      String text = tweet.getText();
      if (rawText.regionMatches(_start, text, _start, _end - _start))
      {
        this.start = _start;this.end = _end;
        return;
      }
      _end = Math.min(_end, rawText.length());
      _start = Math.min(_start, _end);
      if (_start == _end)
      {
        switch (type)
        {
        case hashtags: 
          break;
        case urls: 
          Matcher m = Regex.VALID_URL.matcher(text);
          if (m.find())
          {
            this.start = m.start();
            this.end = m.end(); return;
          }
          break;
        }
        this.end = Math.min(_end, text.length());
        this.start = Math.min(_start, this.end);
        return;
      }
      String entityText = rawText.substring(_start, _end);
      
      int from = 0;
      for (TweetEntity prev : previous) {
        if (tweet.getText().regionMatches(prev.start, entityText, 0, entityText.length())) {
          from = prev.end;
        }
      }
      int i = text.indexOf(entityText, from);
      if (i == -1)
      {
        entityText = InternalUtils.unencode(entityText);
        i = text.indexOf(entityText);
        if (i == -1) {
          i = _start;
        }
      }
      this.start = i;
      this.end = (this.start + _end - _start);
    }
    
    TweetEntity(Twitter.ITweet tweet, Twitter.KEntityType type, int start, int end, String display)
    {
      this.tweet = tweet;
      this.end = end;
      this.start = start;
      this.type = type;
      this.display = display;
    }
    
    public String displayVersion()
    {
      return this.display == null ? toString() : this.display;
    }
    
    public String toString()
    {
      String text = this.tweet.getText();
      int e = Math.min(this.end, text.length());
      int s = Math.min(this.start, e);
      return text.substring(s, e);
    }
  }
  
  public static boolean CHECK_TWEET_LENGTH = true;
  public static int LINK_LENGTH = 22;
  public static int MEDIA_LENGTH = 23;
  public static long PHOTO_SIZE_LIMIT = 3145728L;
  public static final String SEARCH_MIXED = "mixed";
  public static final String SEARCH_POPULAR = "popular";
  public static final String SEARCH_RECENT = "recent";
  private static final long serialVersionUID = 1L;
  public static final String version = "2.8.7";
  public static final int MAX_CHARS = 140;
  static final String API_VERSION = "1.1";
  static final String DEFAULT_TWITTER_URL = "https://api.twitter.com/1.1";
  public static boolean WORRIED_ABOUT_TWITTER = false;
  Integer count;
  private String geocode;
  final IHttpClient http;
  
  public static User getUser(String screenName, List<User> users)
  {
    assert ((screenName != null) && (users != null));
    for (User user : users) {
      if (screenName.equals(user.screenName)) {
        return user;
      }
    }
    return null;
  }
  
  public static void main(String[] args)
  {
    if (args.length == 3)
    {
      Twitter tw = new Twitter(args[0], args[1]);
      
      Status s = tw.setStatus(args[2]);
      System.out.println(s);
      return;
    }
    System.out.println("Java interface for Twitter");
    System.out.println("--------------------------");
    System.out.println("Version 2.8.7");
    System.out.println("Released under LGPL by Winterwell Associates Ltd.");
    System.out
      .println("See source code, JavaDoc, or http://winterwell.com for details on how to use.");
  }
  
  boolean includeRTs = true;
  private String lang;
  private int maxResults = -1;
  private double[] myLatLong;
  private String name;
  private String resultType;
  User self;
  private Date sinceDate;
  private Number sinceId;
  private String sourceApp = "jtwitterlib";
  boolean tweetEntities = true;
  @Deprecated
  private transient String twitlongerApiKey;
  @Deprecated
  private transient String twitlongerAppName;
  String TWITTER_URL = "https://api.twitter.com/1.1";
  private Date untilDate;
  private BigInteger untilId;
  private Long placeId;
  
  public void setMyPlace(Long placeId)
  {
    this.placeId = placeId;
  }
  
  /**
   * @deprecated
   */
  public Twitter()
  {
    this(null, new URLConnectionHttpClient());
  }
  
  public Twitter(String name, IHttpClient client)
  {
    this.name = name;
    this.http = client;
    assert (client != null);
  }
  
  @Deprecated
  public Twitter(String screenName, String password)
  {
    this(screenName, new URLConnectionHttpClient(screenName, password));
  }
  
  public Twitter(Twitter jtwit)
  {
    this(jtwit.getScreenName(), jtwit.http.copy());
  }
  
  public Twitter_Account account()
  {
    return new Twitter_Account(this);
  }
  
  public Twitter_Analytics analytics()
  {
    return new Twitter_Analytics(this.http);
  }
  
  Map<String, String> addStandardishParameters(Map<String, String> vars)
  {
    if (this.sinceId != null) {
      vars.put("since_id", this.sinceId.toString());
    }
    if (this.untilId != null) {
      vars.put("max_id", this.untilId.toString());
    }
    if (this.count != null) {
      vars.put("count", this.count.toString());
    }
    if (this.tweetEntities) {
      vars.put("include_entities", "1");
    } else {
      vars.put("include_entities", "0");
    }
    if (!this.includeRTs) {
      vars.put("include_rts", "0");
    }
    return vars;
  }
  
  @Deprecated
  public User befriend(String username)
    throws TwitterException
  {
    return follow(username);
  }
  
  @Deprecated
  public User breakFriendship(String username)
  {
    return stopFollowing(username);
  }
  
  /**
   * @deprecated
   */
  public List<User> bulkShow(List<String> screenNames)
  {
    return users().show(screenNames);
  }
  
  /**
   * @deprecated
   */
  public List<User> bulkShowById(List<? extends Number> userIds)
  {
    return users().showById(userIds);
  }
  
  private <T extends ITweet> List<T> dateFilter(List<T> list)
  {
    if ((this.sinceDate == null) && (this.untilDate == null)) {
      return list;
    }
    ArrayList<T> filtered = new ArrayList(list.size());
    for (T message : list) {
      if (message.getCreatedAt() == null) {
        filtered.add(message);
      } else if ((this.untilDate == null) || (!this.untilDate.before(message.getCreatedAt()))) {
        if ((this.sinceDate == null) || (!this.sinceDate.after(message.getCreatedAt()))) {
          filtered.add(message);
        }
      }
    }
    return filtered;
  }
  
  public void destroy(ITweet tweet)
    throws TwitterException
  {
    if ((tweet instanceof Status)) {
      destroyStatus(tweet.getId());
    } else {
      destroyMessage((Message)tweet);
    }
  }
  
  private void destroyMessage(Message dm)
  {
    String page = post(this.TWITTER_URL + "/direct_messages/destroy/" + dm.id + 
      ".json", null, true);
    assert (page != null);
  }
  
  public void destroyMessage(Number id)
  {
    String page = post(this.TWITTER_URL + "/direct_messages/destroy/" + id + 
      ".json", null, true);
    assert (page != null);
  }
  
  public void destroyStatus(Number id)
    throws TwitterException
  {
    String page = post(this.TWITTER_URL + "/statuses/destroy/" + id + ".json", 
      null, true);
    
    flush();
    assert (page != null);
  }
  
  @Deprecated
  public void destroyStatus(Status status)
    throws TwitterException
  {
    destroyStatus(status.getId());
  }
  
  boolean enoughResults(List list)
  {
    return (this.maxResults != -1) && (list.size() >= this.maxResults);
  }
  
  void flush()
  {
    this.http.getPage("https://twitter.com/" + this.name, null, true);
  }
  
  @Deprecated
  public User follow(String username)
    throws TwitterException
  {
    return users().follow(username);
  }
  
  public String toString()
  {
    return "Twitter[" + this.name + "]";
  }
  
  @Deprecated
  public User follow(User user)
  {
    return follow(user.screenName);
  }
  
  public Twitter_Geo geo()
  {
    return new Twitter_Geo(this);
  }
  
  public List<Message> getDirectMessages()
  {
    return getMessages(this.TWITTER_URL + "/direct_messages.json", 
      standardishParameters());
  }
  
  public List<Message> getDirectMessagesSent()
  {
    return getMessages(this.TWITTER_URL + "/direct_messages/sent.json", 
      standardishParameters());
  }
  
  public List<Status> getFavorites()
  {
    return getFavorites(null);
  }
  
  public List<Status> getFavorites(String screenName)
  {
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "screen_name", screenName });
    return getStatuses(this.TWITTER_URL + "/favorites/list.json", 
      addStandardishParameters(vars), this.http.canAuthenticate());
  }
  
  @Deprecated
  public List<Number> getFollowerIDs()
    throws TwitterException
  {
    return users().getFollowerIDs();
  }
  
  @Deprecated
  public List<Number> getFollowerIDs(String screenName)
    throws TwitterException
  {
    return users().getFollowerIDs(screenName);
  }
  
  @Deprecated
  public List<User> getFollowers()
    throws TwitterException
  {
    return users().getFollowers();
  }
  
  @Deprecated
  public List<User> getFollowers(String username)
    throws TwitterException
  {
    return users().getFollowers(username);
  }
  
  @Deprecated
  public List<Number> getFriendIDs()
    throws TwitterException
  {
    return users().getFriendIDs();
  }
  
  @Deprecated
  public List<Number> getFriendIDs(String screenName)
    throws TwitterException
  {
    return users().getFriendIDs(screenName);
  }
  
  @Deprecated
  public List<User> getFriends()
    throws TwitterException
  {
    return users().getFriends();
  }
  
  @Deprecated
  public List<User> getFriends(String username)
    throws TwitterException
  {
    return users().getFriends(username);
  }
  
  @Deprecated
  public List<Status> getFriendsTimeline()
    throws TwitterException
  {
    return getHomeTimeline();
  }
  
  public List<Status> getHomeTimeline()
    throws TwitterException
  {
    assert (this.http.canAuthenticate());
    return getStatuses(this.TWITTER_URL + "/statuses/home_timeline.json", 
      standardishParameters(), true);
  }
  
  public IHttpClient getHttpClient()
  {
    return this.http;
  }
  
  public List<TwitterList> getLists()
  {
    return getLists(this.name);
  }
  
  public List<TwitterList> getListsAll(User user)
  {
    assert ((user != null) || (this.http.canAuthenticate())) : "No authenticating user";
    try
    {
      String url = this.TWITTER_URL + "/lists/all.json";
      Map<String, String> vars = user.screenName == null ? 
        InternalUtils.asMap(new Object[] {"user_id", user.id }) : 
        InternalUtils.asMap(new Object[] {"screen_name", user.screenName });
      String listsJson = this.http.getPage(url, vars, this.http.canAuthenticate());
      JSONObject wrapper = new JSONObject(listsJson);
      JSONArray jarr = (JSONArray)wrapper.get("lists");
      List<TwitterList> lists = new ArrayList();
      for (int i = 0; i < jarr.length(); i++)
      {
        JSONObject li = jarr.getJSONObject(i);
        TwitterList twList = new TwitterList(li, this);
        lists.add(twList);
      }
      return lists;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(null, e);
    }
  }
  
  public List<TwitterList> getLists(String screenName)
  {
    assert (screenName != null);
    try
    {
      String url = this.TWITTER_URL + "/lists/list.json";
      
      Map<String, String> vars = InternalUtils.asMap(new Object[] {"screen_name", screenName });
      String listsJson = this.http.getPage(url, vars, true);
      
      JSONArray jarr = new JSONArray(listsJson);
      List<TwitterList> lists = new ArrayList();
      for (int i = 0; i < jarr.length(); i++)
      {
        JSONObject li = jarr.getJSONObject(i);
        TwitterList twList = new TwitterList(li, this);
        lists.add(twList);
      }
      return lists;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(null, e);
    }
  }
  
  public List<TwitterList> getListsContaining(String screenName, boolean filterToOwned)
  {
    assert (screenName != null);
    try
    {
      String url = this.TWITTER_URL + "/lists/memberships.json";
      
      Map<String, String> vars = InternalUtils.asMap(new Object[] { "screen_name", screenName });
      if (filterToOwned)
      {
        assert (this.http.canAuthenticate());
        vars.put("filter_to_owned_lists", "1");
      }
      String listsJson = this.http.getPage(url, vars, this.http.canAuthenticate());
      JSONObject wrapper = new JSONObject(listsJson);
      JSONArray jarr = (JSONArray)wrapper.get("lists");
      List<TwitterList> lists = new ArrayList();
      for (int i = 0; i < jarr.length(); i++)
      {
        JSONObject li = jarr.getJSONObject(i);
        TwitterList twList = new TwitterList(li, this);
        lists.add(twList);
      }
      return lists;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(null, e);
    }
  }
  
  public List<TwitterList> getListsContainingMe()
  {
    return getListsContaining(this.name, false);
  }
  
  /**
   * @deprecated
   */
  public String getLongStatus(Status truncatedStatus)
  {
    TwitLonger tl = new TwitLonger();
    return tl.getLongStatus(truncatedStatus);
  }
  
  public int getMaxResults()
  {
    return this.maxResults;
  }
  
  public List<Status> getMentions()
  {
    return getStatuses(this.TWITTER_URL + "/statuses/mentions_timeline.json", 
      standardishParameters(), true);
  }
  
  private List<Message> getMessages(String url, Map<String, String> var)
  {
    if (this.maxResults < 1)
    {
      List<Message> msgs = Message.getMessages(this.http.getPage(url, var, 
        true));
      msgs = dateFilter(msgs);
      return msgs;
    }
    BigInteger maxId = this.untilId;
    List<Message> msgs = new ArrayList();
    while (msgs.size() <= this.maxResults)
    {
      String p = this.http.getPage(url, var, true);
      List<Message> nextpage = Message.getMessages(p);
      
      maxId = InternalUtils.getMinId(maxId, nextpage);
      
      nextpage = dateFilter(nextpage);
      msgs.addAll(nextpage);
      if (nextpage.size() < 20) {
        break;
      }
      var.put("max_id", maxId.toString());
    }
    return msgs;
  }
  
  /**
   * @deprecated
   */
  public RateLimit getRateLimit(KRequestType reqType)
  {
    return this.http.getRateLimit(reqType);
  }
  
  /**
   * @deprecated
   */
  public int getRateLimitStatus()
  {
    RateLimit rl = (RateLimit)((URLConnectionHttpClient)this.http).updateRateLimits().get(KRequestType.NORMAL.rateLimit);
    return rl == null ? 90 : rl.getRemaining();
  }
  
  /**
   * @deprecated
   */
  public List<Status> getReplies()
    throws TwitterException
  {
    return getMentions();
  }
  
  public List<User> getRetweeters(Status tweet)
  {
    String url = this.TWITTER_URL + "/statuses/retweets/" + tweet.id + 
      ".json";
    Map<String, String> vars = addStandardishParameters(new HashMap());
    String json = this.http.getPage(url, vars, this.http.canAuthenticate());
    List<Status> ss = Status.getStatuses(json);
    List<User> users = new ArrayList(ss.size());
    for (Status status : ss) {
      users.add(status.getUser());
    }
    return users;
  }
  
  public List<Status> getRetweets(Status tweet)
  {
    String url = this.TWITTER_URL + "/statuses/retweets/" + tweet.id + ".json";
    Map<String, String> vars = addStandardishParameters(new HashMap());
    String json = this.http.getPage(url, vars, true);
    List<Status> newStyle = Status.getStatuses(json);
    try
    {
      StringBuilder sq = new StringBuilder();
      sq.append("\"RT @" + tweet.getUser().getScreenName() + ": ");
      if (sq.length() + tweet.text.length() + 1 > 140)
      {
        int i = tweet.text.lastIndexOf(' ', 140 - sq.length() - 1);
        String words = tweet.text.substring(0, i);
        sq.append(words);
      }
      else
      {
        sq.append(tweet.text);
      }
      sq.append('"');
      List<Status> oldStyle = search(sq.toString());
      
      newStyle.addAll(oldStyle);
      Collections.sort(newStyle, InternalUtils.NEWEST_FIRST);
      return newStyle;
    }
    catch (TwitterException e) {}
    return newStyle;
  }
  
  /**
   * @deprecated
   */
  public List<Status> getRetweetsByMe()
  {
    List<Status> myTweets = getUserTimeline();
    List<Status> retweets = new ArrayList();
    for (Status status : myTweets) {
      if ((status.getOriginal() != null) && (status.getText().startsWith("RT"))) {
        retweets.add(status);
      }
    }
    return retweets;
  }
  
  public List<Status> getRetweetsOfMe()
  {
    String url = this.TWITTER_URL + "/statuses/retweets_of_me.json";
    Map<String, String> vars = addStandardishParameters(new HashMap());
    String json = this.http.getPage(url, vars, true);
    return Status.getStatuses(json);
  }
  
  public String getScreenName()
  {
    if (this.name != null) {
      return this.name;
    }
    getSelf();
    return this.name;
  }
  
  public String getScreenNameIfKnown()
  {
    return this.name;
  }
  
  private Map<String, String> getSearchParams(String searchTerm, Integer rpp)
  {
    Map vars = InternalUtils.asMap(new Object[] { "count", rpp, "q", searchTerm });
    if (this.sinceId != null) {
      vars.put("since_id", this.sinceId.toString());
    }
    if (this.untilId != null) {
      vars.put("max_id", this.untilId.toString());
    }
    if (this.untilDate != null) {
      vars.put("until", InternalUtils.df.format(this.untilDate));
    }
    if (this.lang != null) {
      vars.put("lang", this.lang);
    }
    if (this.geocode != null) {
      vars.put("geocode", this.geocode);
    }
    if (this.resultType != null) {
      vars.put("result_type", this.resultType);
    }
    addStandardishParameters(vars);
    return vars;
  }
  
  public User getSelf()
  {
    if (this.self != null) {
      return this.self;
    }
    if (!this.http.canAuthenticate())
    {
      if (this.name != null)
      {
        this.self = new User(this.name);
        return this.self;
      }
      return null;
    }
    account().verifyCredentials();
    this.name = this.self.getScreenName();
    return this.self;
  }
  
  public Status getStatus()
    throws TwitterException
  {
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "count", Integer.valueOf(6) });
    String json = this.http.getPage(
      this.TWITTER_URL + "/statuses/user_timeline.json", vars, true);
    List<Status> statuses = Status.getStatuses(json);
    if (statuses.size() == 0) {
      return null;
    }
    return (Status)statuses.get(0);
  }
  
  public Status getStatus(Number id)
    throws TwitterException
  {
    boolean auth = InternalUtils.authoriseIn11(this);
    Map vars = this.tweetEntities ? InternalUtils.asMap(new Object[] { "include_entities", "1" }) : 
      null;
    String json = this.http.getPage(this.TWITTER_URL + "/statuses/show/" + id + 
      ".json", vars, auth);
    try
    {
      return new Status(new JSONObject(json), null);
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  public Status getStatus(String username)
    throws TwitterException
  {
    assert (username != null);
    
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "id", username, "count", Integer.valueOf(6) });
    String json = this.http.getPage(
      this.TWITTER_URL + "/statuses/user_timeline.json", vars, this.http.canAuthenticate());
    List<Status> statuses = Status.getStatuses(json);
    if (statuses.size() == 0) {
      return null;
    }
    return (Status)statuses.get(0);
  }
  
  List<Status> getStatuses(String url, Map<String, String> var, boolean authenticate)
  {
    if (this.maxResults < 1)
    {
      try
      {
        msgs = Status.getStatuses(this.http.getPage(url, var, 
          authenticate));
      }
      catch (TwitterException.Parsing pex)
      {
        List<Status> msgs;
        List<Status> msgs;
        if (this.http.isRetryOnError())
        {
          InternalUtils.sleep(250L);
          String json = this.http.getPage(url, var, authenticate);
          msgs = Status.getStatuses(json);
        }
        else
        {
          throw pex;
        }
      }
      List<Status> msgs = dateFilter(msgs);
      return msgs;
    }
    BigInteger maxId = this.untilId;
    List<Status> msgs = new ArrayList();
    while (msgs.size() <= this.maxResults)
    {
      try
      {
        String json = this.http.getPage(url, var, authenticate);
        nextpage = Status.getStatuses(json);
      }
      catch (TwitterException.Parsing pex)
      {
        List<Status> nextpage;
        List<Status> nextpage;
        if (this.http.isRetryOnError())
        {
          InternalUtils.sleep(250L);
          String json = this.http.getPage(url, var, authenticate);
          nextpage = Status.getStatuses(json);
        }
        else
        {
          throw pex;
        }
      }
      List<Status> nextpage;
      if (nextpage.size() == 0) {
        break;
      }
      maxId = InternalUtils.getMinId(maxId, nextpage);
      
      msgs.addAll(dateFilter(nextpage));
      var.put("max_id", maxId.toString());
    }
    return msgs;
  }
  
  public List<String> getTrends()
  {
    return getTrends(Integer.valueOf(1));
  }
  
  public List<String> getTrends(Number woeid)
  {
    String jsonTrends = this.http.getPage(this.TWITTER_URL + "/trends/place.json", 
      InternalUtils.asMap(new Object[] {"id", woeid }), true);
    try
    {
      JSONArray jarr = new JSONArray(jsonTrends);
      JSONObject json1 = jarr.getJSONObject(0);
      JSONArray json2 = json1.getJSONArray("trends");
      List<String> trends = new ArrayList();
      for (int i = 0; i < json2.length(); i++)
      {
        JSONObject ti = json2.getJSONObject(i);
        String t = ti.getString("name");
        trends.add(t);
      }
      return trends;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(jsonTrends, e);
    }
  }
  
  public Date getUntilDate()
  {
    return this.untilDate;
  }
  
  @Deprecated
  public User getUser(long userId)
  {
    return show(Long.valueOf(userId));
  }
  
  @Deprecated
  public User getUser(String screenName)
  {
    return show(screenName);
  }
  
  public List<Status> getUserTimeline()
    throws TwitterException
  {
    return getStatuses(this.TWITTER_URL + "/statuses/user_timeline.json", 
      standardishParameters(), true);
  }
  
  public List<Status> getUserTimeline(Long userId)
    throws TwitterException
  {
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "user_id", userId });
    addStandardishParameters(vars);
    
    boolean authenticate = this.http.canAuthenticate();
    try
    {
      return getStatuses(this.TWITTER_URL + "/statuses/user_timeline.json", 
        vars, authenticate);
    }
    catch (TwitterException.E401 e)
    {
      throw e;
    }
  }
  
  public List<Status> getUserTimeline(String screenName)
    throws TwitterException
  {
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "screen_name", screenName });
    addStandardishParameters(vars);
    
    boolean authenticate = this.http.canAuthenticate();
    try
    {
      return getStatuses(this.TWITTER_URL + "/statuses/user_timeline.json", 
        vars, authenticate);
    }
    catch (TwitterException.E404 e)
    {
      throw new TwitterException.E404("Twitter does not return any information for " + screenName + 
        ". They may have been deleted long ago.");
    }
    catch (TwitterException.E401 e)
    {
      isSuspended(screenName);
      throw e;
    }
  }
  
  /**
   * @deprecated
   */
  public List<Status> getUserTimelineWithRetweets(String screenName)
    throws TwitterException
  {
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "screen_name", screenName, "include_rts", "1" });
    addStandardishParameters(vars);
    
    boolean authenticate = this.http.canAuthenticate();
    try
    {
      return getStatuses(this.TWITTER_URL + "/statuses/user_timeline.json", 
        vars, authenticate);
    }
    catch (TwitterException.E401 e)
    {
      isSuspended(screenName);
      throw e;
    }
  }
  
  @Deprecated
  public boolean isFollower(String userB)
  {
    return isFollower(userB, this.name);
  }
  
  /**
   * @deprecated
   */
  public boolean isFollower(String followerScreenName, String followedScreenName)
  {
    return users().isFollower(followerScreenName, followedScreenName);
  }
  
  @Deprecated
  public boolean isFollowing(String userB)
  {
    return isFollower(this.name, userB);
  }
  
  @Deprecated
  public boolean isFollowing(User user)
  {
    return isFollowing(user.screenName);
  }
  
  /**
   * @deprecated
   */
  public boolean isRateLimited(KRequestType reqType, int minCalls)
  {
    RateLimit rl = getRateLimit(reqType);
    if (rl == null) {
      return false;
    }
    if (rl.getRemaining() >= minCalls) {
      return false;
    }
    if (rl.isOutOfDate()) {
      return false;
    }
    return true;
  }
  
  private void isSuspended(String screenName)
    throws TwitterException.SuspendedUser
  {
    show(screenName);
  }
  
  /**
   * @deprecated
   */
  public boolean isTwitlongerSetup()
  {
    return (this.twitlongerApiKey != null) && (this.twitlongerAppName != null);
  }
  
  public boolean isValidLogin()
  {
    if (!this.http.canAuthenticate()) {
      return false;
    }
    try
    {
      Twitter_Account ta = new Twitter_Account(this);
      User u = ta.verifyCredentials();
      return true;
    }
    catch (TwitterException.E403 e)
    {
      return false;
    }
    catch (TwitterException.E401 e)
    {
      return false;
    }
    catch (TwitterException e)
    {
      throw e;
    }
  }
  
  private String post(String uri, Map<String, String> vars, boolean authenticate)
    throws TwitterException
  {
    String page = this.http.post(uri, vars, authenticate);
    return page;
  }
  
  public void reportSpam(String screenName)
  {
    this.http.getPage(this.TWITTER_URL + "/version/report_spam.json", 
      InternalUtils.asMap(new Object[] {"screen_name", screenName }), true);
  }
  
  public Status retweet(Status tweet)
  {
    try
    {
      String result = post(
        this.TWITTER_URL + "/statuses/retweet/" + tweet.getId() + 
        ".json", null, true);
      return new Status(new JSONObject(result), null);
    }
    catch (TwitterException.E403 e)
    {
      List<Status> rts = getRetweetsByMe();
      for (Status rt : rts) {
        if (tweet.equals(rt.getOriginal())) {
          throw new TwitterException.Repetition(rt.getText());
        }
      }
      throw e;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(null, e);
    }
  }
  
  public List<Status> search(String searchTerm)
  {
    return search(searchTerm, null, 100);
  }
  
  public List<Status> search(String searchTerm, ICallback callback, int rpp)
  {
    if ((rpp > 100) && (this.maxResults < rpp)) {
      throw new IllegalArgumentException(
        "You need to switch on paging to fetch more than 100 search results. First call setMaxResults() to raise the limit above " + 
        rpp);
    }
    if (searchTerm.length() > 1000) {
      throw new TwitterException.E406("Search query too long: " + searchTerm);
    }
    Map vars;
    Map vars;
    if ((this.maxResults < 100) && (this.maxResults > 0)) {
      vars = getSearchParams(searchTerm, Integer.valueOf(this.maxResults));
    } else {
      vars = getSearchParams(searchTerm, Integer.valueOf(rpp));
    }
    List<Status> allResults = new ArrayList(Math.max(this.maxResults, 
      rpp));
    String url = this.TWITTER_URL + "/search/tweets.json";
    BigInteger maxId = this.untilId;
    List<Status> stati;
    int numResults;
    do
    {
      vars.put("max_id", maxId);
      try
      {
        String json = this.http.getPage(url, vars, true);
        stati = Status.getStatusesFromSearch(this, json);
      }
      catch (TwitterException.Parsing pex)
      {
        List<Status> stati;
        List<Status> stati;
        if (this.http.isRetryOnError())
        {
          InternalUtils.sleep(250L);
          String json = this.http.getPage(url, vars, true);
          stati = Status.getStatusesFromSearch(this, json);
        }
        else
        {
          throw pex;
        }
      }
      catch (TwitterException.E403 ex)
      {
        if ((ex.getMessage() != null) && (ex.getMessage().startsWith("code 195:"))) {
          throw new TwitterException.E406("Search too long/complex: " + ex.getMessage());
        }
        throw ex;
      }
      numResults = stati.size();
      
      maxId = InternalUtils.getMinId(maxId, stati);
      
      stati = dateFilter(stati);
      allResults.addAll(stati);
    } while (((callback == null) || 
    
      (!callback.process(stati))) && 
      
      ((rpp != 100) || (numResults >= 70)) && (numResults >= rpp) && (
      
      allResults.size() < this.maxResults));
    return allResults;
  }
  
  @Deprecated
  public List<User> searchUsers(String searchTerm)
  {
    return users().searchUsers(searchTerm);
  }
  
  public Message sendMessage(String recipient, String text)
    throws TwitterException
  {
    assert ((recipient != null) && (text != null)) : (recipient + " " + text);
    assert (!text.startsWith("d " + recipient)) : (recipient + " " + text);
    assert (!recipient.startsWith("@")) : (recipient + " " + text);
    if (text.length() > 140) {
      throw new IllegalArgumentException("Message is too long.");
    }
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "user", recipient, "text", text });
    if (this.tweetEntities) {
      vars.put("include_entities", "1");
    }
    String result = null;
    try
    {
      result = post(this.TWITTER_URL + "/direct_messages/new.json", vars, true);
      
      return new Message(new JSONObject(result));
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(result, e);
    }
    catch (TwitterException.E404 e)
    {
      throw new TwitterException.E404(e.getMessage() + " with recipient=" + 
        recipient + ", text=" + text);
    }
  }
  
  public void setAPIRootUrl(String url)
  {
    assert ((url.startsWith("http://")) || (url.startsWith("https://"))) : url;
    assert (!url.endsWith("/")) : ("Please remove the trailing / from " + url);
    this.TWITTER_URL = url;
  }
  
  public void setCount(Integer count)
  {
    this.count = count;
  }
  
  public Status setFavorite(Status status, boolean isFavorite)
  {
    try
    {
      String uri = 
        this.TWITTER_URL + "/favorites/destroy.json";
      String json = this.http.post(uri, InternalUtils.asMap(new Object[] { "id", status.id }), true);
      return new Status(new JSONObject(json), null);
    }
    catch (TwitterException.E403 e)
    {
      if ((e.getMessage() != null) && 
        (e.getMessage().contains("already favorited"))) {
        return null;
      }
      throw e;
    }
  }
  
  public void setIncludeRTs(boolean includeRTs)
  {
    this.includeRTs = includeRTs;
  }
  
  public void setIncludeTweetEntities(boolean tweetEntities)
  {
    this.tweetEntities = tweetEntities;
  }
  
  public void setLanguage(String language)
  {
    this.lang = language;
  }
  
  public void setMaxResults(int maxResults)
  {
    assert (maxResults != 0);
    this.maxResults = maxResults;
  }
  
  public void setMyLocation(double[] latitudeLongitude)
  {
    this.myLatLong = latitudeLongitude;
    if (this.myLatLong == null) {
      return;
    }
    if (Math.abs(this.myLatLong[0]) > 90.0D) {
      throw new IllegalArgumentException(this.myLatLong[0] + 
        " is not within +/- 90");
    }
    if (Math.abs(this.myLatLong[1]) > 180.0D) {
      throw new IllegalArgumentException(this.myLatLong[1] + 
        " is not within +/- 180");
    }
  }
  
  public void setSearchLocation(double latitude, double longitude, String radius)
  {
    assert ((radius.endsWith("mi")) || (radius.endsWith("km"))) : radius;
    this.geocode = ((float)latitude + "," + (float)longitude + "," + radius);
  }
  
  public String getSearchLocation()
  {
    return this.geocode;
  }
  
  public void setSearchResultType(String resultType)
  {
    this.resultType = resultType;
  }
  
  @Deprecated
  public void setSinceDate(Date sinceDate)
  {
    this.sinceDate = sinceDate;
  }
  
  public void setSinceId(Number statusId)
  {
    this.sinceId = statusId;
  }
  
  public void setSource(String sourceApp)
  {
    this.sourceApp = sourceApp;
  }
  
  public Status setStatus(String statusText)
    throws TwitterException
  {
    return updateStatus(statusText);
  }
  
  @Deprecated
  public void setUntilDate(Date untilDate)
  {
    this.untilDate = untilDate;
  }
  
  public void setUntilId(Number untilId)
  {
    if (untilId == null)
    {
      this.untilId = null;
      return;
    }
    if ((untilId instanceof BigInteger))
    {
      this.untilId = ((BigInteger)untilId);
      return;
    }
    this.untilId = BigInteger.valueOf(untilId.longValue());
  }
  
  public BigInteger getUntilId()
  {
    return this.untilId;
  }
  
  public Number getSinceId()
  {
    return this.sinceId;
  }
  
  /**
   * @deprecated
   */
  public void setupTwitlonger(String twitlongerAppName, String twitlongerApiKey)
  {
    this.twitlongerAppName = twitlongerAppName;
    this.twitlongerApiKey = twitlongerApiKey;
  }
  
  @Deprecated
  public User show(Number userId)
  {
    return users().show(userId);
  }
  
  @Deprecated
  public User show(String screenName)
    throws TwitterException, TwitterException.SuspendedUser
  {
    return users().show(screenName);
  }
  
  public List<String> splitMessage(String longStatus)
  {
    if (longStatus.length() <= 140) {
      return Collections.singletonList(longStatus);
    }
    List<String> sections = new ArrayList(4);
    StringBuilder tweet = new StringBuilder(140);
    String[] words = longStatus.split("\\s+");
    String[] arrayOfString1;
    int j = (arrayOfString1 = words).length;
    for (int i = 0; i < j; i++)
    {
      String w = arrayOfString1[i];
      if (tweet.length() + w.length() + 1 > 140)
      {
        tweet.append("...");
        sections.add(tweet.toString());
        tweet = new StringBuilder(140);
        tweet.append(w);
      }
      else
      {
        if (tweet.length() != 0) {
          tweet.append(" ");
        }
        tweet.append(w);
      }
    }
    if (tweet.length() != 0) {
      sections.add(tweet.toString());
    }
    return sections;
  }
  
  private Map<String, String> standardishParameters()
  {
    return addStandardishParameters(new HashMap());
  }
  
  @Deprecated
  public User stopFollowing(String username)
  {
    return users().stopFollowing(username);
  }
  
  @Deprecated
  public User stopFollowing(User user)
  {
    return stopFollowing(user.screenName);
  }
  
  public boolean updateConfiguration()
  {
    String json = this.http.getPage(this.TWITTER_URL + "/help/configuration.json", 
      null, true);
    boolean change = false;
    try
    {
      JSONObject jo = new JSONObject(json);
      
      int len = jo.getInt("short_url_length");
      if (len != LINK_LENGTH) {
        change = true;
      }
      LINK_LENGTH = len;
      
      int len = jo.getInt("characters_reserved_per_media");
      if (len != MEDIA_LENGTH) {
        change = true;
      }
      MEDIA_LENGTH = len;
      
      long lmt = jo.getLong("photo_size_limit");
      if (lmt != PHOTO_SIZE_LIMIT) {
        change = true;
      }
      PHOTO_SIZE_LIMIT = lmt;
      
      return change;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  /**
   * @deprecated
   */
  public Status updateLongStatus(String message, Number inReplyToStatusId)
  {
    TwitLonger tl = new TwitLonger(this, this.twitlongerApiKey, this.twitlongerAppName);
    return tl.updateLongStatus(message, inReplyToStatusId);
  }
  
  public Status updateStatus(String statusText)
  {
    return updateStatus(statusText, null);
  }
  
  public static int countCharacters(String statusText)
  {
    int shortLength = statusText.length();
    Matcher m = Regex.VALID_URL.matcher(statusText);
    while (m.find())
    {
      shortLength += LINK_LENGTH - m.group().length();
      if (m.group().startsWith("https")) {
        shortLength++;
      }
    }
    return shortLength;
  }
  
  public Status updateStatus(String statusText, Number inReplyToStatusId)
    throws TwitterException
  {
    Map<String, String> vars = updateStatus2_vars(statusText, inReplyToStatusId, false);
    String result = this.http.post(this.TWITTER_URL + "/statuses/update.json", vars, 
      true);
    try
    {
      Status s = new Status(new JSONObject(result), null);
      return updateStatus2_safetyCheck(statusText, s);
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(result, e);
    }
  }
  
  private Map<String, String> updateStatus2_vars(String statusText, Number inReplyToStatusId, boolean withMedia)
  {
    int max = withMedia ? 140 - MEDIA_LENGTH : 140;
    if ((statusText.length() > max) && 
      (this.TWITTER_URL.contains("twitter")) && 
      (CHECK_TWEET_LENGTH))
    {
      int shortLength = countCharacters(statusText);
      if (shortLength > max)
      {
        if (statusText.startsWith("RT")) {
          throw new IllegalArgumentException(
            "Status text must be 140 characters or less -- use Twitter.retweet() to do new-style retweets which can be a bit longer: " + 
            statusText.length() + " " + statusText);
        }
        if (withMedia) {
          throw new IllegalArgumentException(
            "Status-with-media text must be " + max + " characters or less: " + 
            statusText.length() + " " + statusText);
        }
        throw new IllegalArgumentException(
          "Status text must be 140 characters or less: " + 
          statusText.length() + " " + statusText);
      }
    }
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "status", statusText });
    if (this.tweetEntities) {
      vars.put("include_entities", "1");
    }
    if (this.myLatLong != null)
    {
      vars.put("lat", Double.toString(this.myLatLong[0]));
      vars.put("long", Double.toString(this.myLatLong[1]));
    }
    if (this.placeId != null) {
      vars.put("place_id", Long.toString(this.placeId.longValue()));
    }
    if (this.sourceApp != null) {
      vars.put("source", this.sourceApp);
    }
    if (inReplyToStatusId != null)
    {
      double v = inReplyToStatusId.doubleValue();
      assert ((v != 0.0D) && (v != -1.0D));
      vars.put("in_reply_to_status_id", inReplyToStatusId.toString());
    }
    return vars;
  }
  
  private Status updateStatus2_safetyCheck(String statusText, Status s)
  {
    String st = statusText.toLowerCase();
    if ((st.startsWith("dm ")) || (st.startsWith("d "))) {
      return null;
    }
    if (!WORRIED_ABOUT_TWITTER) {
      return s;
    }
    String targetText = statusText.trim();
    String returnedStatusText = s.text.trim();
    
    targetText = InternalUtils.stripUrls(targetText);
    returnedStatusText = InternalUtils.stripUrls(returnedStatusText);
    if (returnedStatusText.equals(targetText)) {
      return s;
    }
    try
    {
      Thread.sleep(500L);
    }
    catch (InterruptedException localInterruptedException) {}
    Status s2 = getStatus();
    if (s2 != null)
    {
      returnedStatusText = InternalUtils.stripUrls(s2.text.trim());
      if (targetText.equals(returnedStatusText)) {
        return s2;
      }
    }
    throw new TwitterException.Unexplained(
      "Unexplained failure for tweet: expected \"" + statusText + 
      "\" but got " + s2);
  }
  
  public Status updateStatusWithMedia(String statusText, BigInteger inReplyToStatusId, File mediaFile)
  {
    if ((mediaFile == null) || (!mediaFile.isFile())) {
      throw new IllegalArgumentException("Invalid file: " + mediaFile);
    }
    Map vars = updateStatus2_vars(statusText, inReplyToStatusId, true);
    vars.put("media[]", mediaFile);
    
    String result = null;
    try
    {
      String url = this.TWITTER_URL + "/statuses/update_with_media.json";
      result = ((OAuthSignpostClient)this.http).postMultipartForm(url, vars);
      return new Status(new JSONObject(result), null);
    }
    catch (TwitterException.E403 e)
    {
      Status s = getStatus();
      if ((s != null) && (s.getText().equals(statusText))) {
        throw new TwitterException.Repetition(s.getText());
      }
      throw e;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(result, e);
    }
  }
  
  public Twitter_Users users()
  {
    return new Twitter_Users(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\Twitter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */