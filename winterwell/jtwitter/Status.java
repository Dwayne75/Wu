package winterwell.jtwitter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import winterwell.json.JSONArray;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public final class Status
  implements Twitter.ITweet
{
  static final Pattern AT_YOU_SIR = Pattern.compile("@(\\w+)");
  private static final String FAKE = "fake";
  private static final long serialVersionUID = 1L;
  public final Date createdAt;
  private EnumMap<Twitter.KEntityType, List<Twitter.TweetEntity>> entities;
  private boolean favorited;
  public final BigInteger id;
  public final BigInteger inReplyToStatusId;
  private String location;
  private Status original;
  private Place place;
  public final int retweetCount;
  boolean sensitive;
  public final String source;
  public final String text;
  public final User user;
  private String lang;
  
  static List<Status> getStatuses(String json)
    throws TwitterException
  {
    if (json.trim().equals("")) {
      return Collections.emptyList();
    }
    try
    {
      List<Status> tweets = new ArrayList();
      JSONArray arr = new JSONArray(json);
      for (int i = 0; i < arr.length(); i++)
      {
        Object ai = arr.get(i);
        if (!JSONObject.NULL.equals(ai))
        {
          JSONObject obj = (JSONObject)ai;
          Status tweet = new Status(obj, null);
          tweets.add(tweet);
        }
      }
      return tweets;
    }
    catch (JSONException e)
    {
      if (json.startsWith("<")) {
        throw new TwitterException.E50X(InternalUtils.stripTags(json));
      }
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  static List<Status> getStatusesFromSearch(Twitter tw, String json)
  {
    try
    {
      JSONObject searchResults = new JSONObject(json);
      List<Status> users = new ArrayList();
      JSONArray arr = searchResults.getJSONArray("statuses");
      for (int i = 0; i < arr.length(); i++)
      {
        JSONObject obj = arr.getJSONObject(i);
        
        Status s = new Status(obj, null);
        users.add(s);
      }
      return users;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  static Object jsonGetLocn(JSONObject object)
    throws JSONException
  {
    String _location = InternalUtils.jsonGet("location", object);
    if ((_location != null) && (_location.length() == 0)) {
      _location = null;
    }
    JSONObject _place = object.optJSONObject("place");
    if (_location != null)
    {
      Matcher m = InternalUtils.latLongLocn.matcher(_location);
      if (m.matches()) {
        _location = m.group(2) + "," + m.group(3);
      }
      return _location;
    }
    if (_place != null)
    {
      Place place = new Place(_place);
      return place;
    }
    JSONObject geo = object.optJSONObject("geo");
    if ((geo != null) && (geo != JSONObject.NULL))
    {
      JSONArray latLong = geo.getJSONArray("coordinates");
      _location = latLong.get(0) + "," + latLong.get(1);
    }
    return _location;
  }
  
  public String getLang()
  {
    return this.lang;
  }
  
  Status(JSONObject object, User user)
    throws TwitterException
  {
    try
    {
      String _id = object.optString("id_str");
      this.id = new BigInteger(_id == "" ? object.get("id").toString() : _id);
      
      JSONObject retweeted = object.optJSONObject("retweeted_status");
      if (retweeted != null) {
        this.original = new Status(retweeted, null);
      }
      String _rawtext = InternalUtils.jsonGet("text", object);
      String _text = _rawtext;
      
      boolean truncated = object.optBoolean("truncated");
      if ((!truncated) && (this.original != null)) {
        truncated = (_text.endsWith("…")) || (_text.endsWith("..."));
      }
      String rtStart = null;
      if ((truncated) && (this.original != null) && (_text.startsWith("RT ")))
      {
        rtStart = "RT @" + this.original.getUser() + ": ";
        _text = rtStart + this.original.getText();
      }
      else
      {
        _text = InternalUtils.unencode(_text);
      }
      this.text = _text;
      
      String c = InternalUtils.jsonGet("created_at", object);
      this.createdAt = InternalUtils.parseDate(c);
      
      String src = InternalUtils.jsonGet("source", object);
      this.source = ((src != null) && (src.contains("&lt;")) ? InternalUtils.unencode(src) : src);
      
      String irt = InternalUtils.jsonGet("in_reply_to_status_id", object);
      if ((irt == null) || (irt.length() == 0)) {
        this.inReplyToStatusId = (this.original == null ? null : this.original.getId());
      } else {
        this.inReplyToStatusId = new BigInteger(irt);
      }
      this.favorited = object.optBoolean("favorited");
      if (user != null)
      {
        this.user = user;
      }
      else
      {
        JSONObject jsonUser = object.optJSONObject("user");
        if (jsonUser == null)
        {
          this.user = null;
        }
        else if (jsonUser.opt("screen_name") == null)
        {
          String _uid = jsonUser.optString("id_str");
          BigInteger userId = new BigInteger(_uid == "" ? object.get(
            "id").toString() : _uid);
          this.user = new User(null, userId);
        }
        else
        {
          this.user = new User(jsonUser, this);
        }
      }
      Object _locn = jsonGetLocn(object);
      this.location = (_locn == null ? null : _locn.toString());
      if ((_locn instanceof Place)) {
        this.place = ((Place)_locn);
      }
      String _lang = object.optString("lang");
      this.lang = ("und".equals(_lang) ? null : _lang);
      
      this.retweetCount = object.optInt("retweet_count", -1);
      
      JSONObject jsonEntities = object.optJSONObject("entities");
      if (jsonEntities != null)
      {
        this.entities = new EnumMap(
          Twitter.KEntityType.class);
        setupEntities(_rawtext, rtStart, jsonEntities);
      }
      this.sensitive = object.optBoolean("possibly_sensitive");
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(null, e);
    }
  }
  
  private void setupEntities(String _rawtext, String rtStart, JSONObject jsonEntities)
  {
    if (rtStart != null)
    {
      int rt = rtStart.length();
      Twitter.KEntityType[] arrayOfKEntityType2;
      int i = (arrayOfKEntityType2 = Twitter.KEntityType.values()).length;
      for (localKEntityType1 = 0; localKEntityType1 < i; localKEntityType1++)
      {
        type = arrayOfKEntityType2[localKEntityType1];
        List<Twitter.TweetEntity> es = this.original.getTweetEntities(type);
        if (es != null)
        {
          ArrayList rtEs = new ArrayList(es.size());
          for (Twitter.TweetEntity e : es)
          {
            Twitter.TweetEntity rte = new Twitter.TweetEntity(this, e.type, 
            
              Math.min(rt + e.start, this.text.length()), Math.min(rt + e.end, this.text.length()), e.display);
            rtEs.add(rte);
          }
          this.entities.put(type, rtEs);
        }
      }
      return;
    }
    Twitter.KEntityType[] arrayOfKEntityType1;
    Twitter.KEntityType localKEntityType1 = (arrayOfKEntityType1 = Twitter.KEntityType.values()).length;
    for (Twitter.KEntityType type = 0; type < localKEntityType1; type++)
    {
      Twitter.KEntityType type = arrayOfKEntityType1[type];
      Object es = Twitter.TweetEntity.parse(this, _rawtext, type, 
        jsonEntities);
      this.entities.put(type, es);
    }
  }
  
  @Deprecated
  public Status(User user, String text, Number id, Date createdAt)
  {
    this.text = text;
    this.user = user;
    this.createdAt = createdAt;
    this.id = 
      ((id instanceof BigInteger) ? (BigInteger)id : id == null ? null : new BigInteger(
      id.toString()));
    this.inReplyToStatusId = null;
    this.source = "fake";
    this.retweetCount = -1;
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Status other = (Status)obj;
    return this.id.equals(other.id);
  }
  
  public Date getCreatedAt()
  {
    return this.createdAt;
  }
  
  public BigInteger getId()
  {
    return this.id;
  }
  
  public String getLocation()
  {
    return this.location;
  }
  
  public List<String> getMentions()
  {
    Matcher m = AT_YOU_SIR.matcher(this.text);
    List<String> list = new ArrayList(2);
    while (m.find()) {
      if ((m.start() == 0) || 
        (!Character.isLetterOrDigit(this.text.charAt(m.start() - 1))))
      {
        String mention = m.group(1);
        if (!Twitter.CASE_SENSITIVE_SCREENNAMES) {
          mention = mention.toLowerCase();
        }
        list.add(mention);
      }
    }
    return list;
  }
  
  public Status getOriginal()
  {
    return this.original;
  }
  
  public Place getPlace()
  {
    return this.place;
  }
  
  public String getSource()
  {
    return InternalUtils.stripTags(this.source);
  }
  
  public String getText()
  {
    return this.text;
  }
  
  public List<Twitter.TweetEntity> getTweetEntities(Twitter.KEntityType type)
  {
    return this.entities == null ? null : (List)this.entities.get(type);
  }
  
  public User getUser()
  {
    return this.user;
  }
  
  public int hashCode()
  {
    return this.id.hashCode();
  }
  
  public boolean isFavorite()
  {
    return this.favorited;
  }
  
  public boolean isSensitive()
  {
    return this.sensitive;
  }
  
  public String toString()
  {
    return this.text;
  }
  
  public String getDisplayText()
  {
    return getDisplayText2(this);
  }
  
  static String getDisplayText2(Twitter.ITweet tweet)
  {
    List<Twitter.TweetEntity> es = tweet.getTweetEntities(Twitter.KEntityType.urls);
    String _text = tweet.getText();
    if ((es == null) || (es.size() == 0)) {
      return _text;
    }
    StringBuilder sb = new StringBuilder(200);
    int i = 0;
    for (Twitter.TweetEntity entity : es)
    {
      sb.append(_text.substring(i, entity.start));
      sb.append(entity.displayVersion());
      i = entity.end;
    }
    if (i < _text.length()) {
      sb.append(_text.substring(i));
    }
    return sb.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\Status.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */