package winterwell.jtwitter;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import winterwell.json.JSONArray;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public final class User
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  public final Date createdAt;
  public final String description;
  public final int favoritesCount;
  private final Boolean followedByYou;
  public int followersCount;
  private final Boolean followingYou;
  public final boolean followRequestSent;
  public final int friendsCount;
  public final Long id;
  String lang;
  public final int listedCount;
  public final String location;
  public final String name;
  public final boolean notifications;
  private Place place;
  public final String profileBackgroundColor;
  public final URI profileBackgroundImageUrl;
  public final boolean profileBackgroundTile;
  public URI profileImageUrl;
  public final String profileLinkColor;
  public final String profileSidebarBorderColor;
  public final String profileSidebarFillColor;
  public final String profileTextColor;
  public final boolean protectedUser;
  public final String screenName;
  public final Status status;
  public final int statusesCount;
  public final String timezone;
  public final double timezoneOffSet;
  public final boolean verified;
  public final URI website;
  
  static List<User> getUsers(String json)
    throws TwitterException
  {
    if (json.trim().equals("")) {
      return Collections.emptyList();
    }
    try
    {
      JSONArray arr = new JSONArray(json);
      return getUsers2(arr);
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  static List<User> getUsers2(JSONArray arr)
    throws JSONException
  {
    List<User> users = new ArrayList();
    for (int i = 0; i < arr.length(); i++)
    {
      JSONObject obj = arr.getJSONObject(i);
      User u = new User(obj, null);
      users.add(u);
    }
    return users;
  }
  
  public String getLang()
  {
    return this.lang;
  }
  
  User(JSONObject obj, Status status)
    throws TwitterException
  {
    try
    {
      this.id = Long.valueOf(obj.getLong("id"));
      this.name = InternalUtils.unencode(InternalUtils.jsonGet("name", obj));
      String sn = InternalUtils.jsonGet("screen_name", obj);
      this.screenName = (Twitter.CASE_SENSITIVE_SCREENNAMES ? sn : sn.toLowerCase());
      
      Object _locn = Status.jsonGetLocn(obj);
      this.location = (_locn == null ? null : _locn.toString());
      if ((_locn instanceof Place)) {
        this.place = ((Place)_locn);
      }
      this.lang = InternalUtils.jsonGet("lang", obj);
      
      this.description = InternalUtils.unencode(InternalUtils.jsonGet(
        "description", obj));
      String img = InternalUtils.jsonGet("profile_image_url", obj);
      this.profileImageUrl = (img == null ? null : InternalUtils.URI(img));
      String url = InternalUtils.jsonGet("url", obj);
      this.website = (url == null ? null : InternalUtils.URI(url));
      this.protectedUser = obj.optBoolean("protected");
      this.followersCount = obj.optInt("followers_count");
      this.profileBackgroundColor = InternalUtils.jsonGet(
        "profile_background_color", obj);
      this.profileLinkColor = InternalUtils.jsonGet("profile_link_color", obj);
      this.profileTextColor = InternalUtils.jsonGet("profile_text_color", obj);
      this.profileSidebarFillColor = InternalUtils.jsonGet(
        "profile_sidebar_fill_color", obj);
      this.profileSidebarBorderColor = InternalUtils.jsonGet(
        "profile_sidebar_border_color", obj);
      this.friendsCount = obj.optInt("friends_count");
      
      String c = InternalUtils.jsonGet("created_at", obj);
      this.createdAt = (c == null ? null : InternalUtils.parseDate(c));
      
      this.favoritesCount = obj.optInt("favourites_count");
      String utcOffSet = InternalUtils.jsonGet("utc_offset", obj);
      this.timezoneOffSet = (utcOffSet == null ? 0.0D : 
        Double.parseDouble(utcOffSet));
      this.timezone = InternalUtils.jsonGet("time_zone", obj);
      img = InternalUtils.jsonGet("profile_background_image_url", obj);
      this.profileBackgroundImageUrl = (img == null ? null : 
        InternalUtils.URI(img));
      this.profileBackgroundTile = obj.optBoolean("profile_background_tile");
      this.statusesCount = obj.optInt("statuses_count");
      this.notifications = obj.optBoolean("notifications");
      this.verified = obj.optBoolean("verified");
      
      Object _cons = obj.opt("connections");
      if ((_cons instanceof JSONArray))
      {
        JSONArray cons = (JSONArray)_cons;
        boolean _following = false;boolean _followedBy = false;boolean _followRequested = false;
        int i = 0;
        for (int n = cons.length(); i < n; i++)
        {
          String ci = cons.getString(i);
          if ("following".equals(ci)) {
            _following = true;
          } else if ("followed_by".equals(ci)) {
            _followedBy = true;
          } else if ("following_requested".equals(ci)) {
            _followRequested = true;
          }
        }
        this.followedByYou = Boolean.valueOf(_following);
        this.followingYou = Boolean.valueOf(_followedBy);
        this.followRequestSent = _followRequested;
      }
      else
      {
        this.followedByYou = InternalUtils.getOptBoolean(obj, "following");
        
        this.followingYou = InternalUtils.getOptBoolean(obj, "followed_by");
        this.followRequestSent = obj.optBoolean("follow_request_sent");
      }
      this.listedCount = obj.optInt("listed_count", -1);
      if (status == null)
      {
        JSONObject s = obj.optJSONObject("status");
        this.status = (s == null ? null : new Status(s, this));
      }
      else
      {
        this.status = status;
      }
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(String.valueOf(obj), e);
    }
    catch (NullPointerException e)
    {
      throw new TwitterException(e + " from <" + obj + ">, <" + status + 
        ">\n\t" + e.getStackTrace()[0] + "\n\t" + 
        e.getStackTrace()[1]);
    }
  }
  
  public User(String screenName)
  {
    this(screenName, null);
  }
  
  User(String screenName, Number id)
  {
    this.id = (id == null ? null : Long.valueOf(id.longValue()));
    this.name = null;
    if ((screenName != null) && (!Twitter.CASE_SENSITIVE_SCREENNAMES)) {
      screenName = screenName.toLowerCase();
    }
    this.screenName = screenName;
    this.status = null;
    this.location = null;
    this.description = null;
    this.profileImageUrl = null;
    this.website = null;
    this.protectedUser = false;
    this.followersCount = 0;
    this.profileBackgroundColor = null;
    this.profileLinkColor = null;
    this.profileTextColor = null;
    this.profileSidebarFillColor = null;
    this.profileSidebarBorderColor = null;
    this.friendsCount = 0;
    this.createdAt = null;
    this.favoritesCount = 0;
    this.timezoneOffSet = -1.0D;
    this.timezone = null;
    this.profileBackgroundImageUrl = null;
    this.profileBackgroundTile = false;
    this.statusesCount = 0;
    this.notifications = false;
    this.verified = false;
    this.followedByYou = null;
    this.followingYou = null;
    this.followRequestSent = false;
    this.listedCount = -1;
  }
  
  public boolean equals(Object other)
  {
    if (this == other) {
      return true;
    }
    if (other.getClass() != User.class) {
      return false;
    }
    User ou = (User)other;
    if ((this.screenName != null) && (ou.screenName != null)) {
      return this.screenName.equals(ou.screenName);
    }
    if ((this.id != null) && (ou.id != null)) {
      return this.id == ou.id;
    }
    return false;
  }
  
  public Date getCreatedAt()
  {
    return this.createdAt;
  }
  
  public String getDescription()
  {
    return this.description;
  }
  
  public int getFavoritesCount()
  {
    return this.favoritesCount;
  }
  
  public int getFollowersCount()
  {
    return this.followersCount;
  }
  
  public int getFriendsCount()
  {
    return this.friendsCount;
  }
  
  public Long getId()
  {
    return this.id;
  }
  
  public String getLocation()
  {
    return this.location;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public Place getPlace()
  {
    return this.place;
  }
  
  public String getProfileBackgroundColor()
  {
    return this.profileBackgroundColor;
  }
  
  public URI getProfileBackgroundImageUrl()
  {
    return this.profileBackgroundImageUrl;
  }
  
  public URI getProfileImageUrl()
  {
    return this.profileImageUrl;
  }
  
  public String getProfileLinkColor()
  {
    return this.profileLinkColor;
  }
  
  public String getProfileSidebarBorderColor()
  {
    return this.profileSidebarBorderColor;
  }
  
  public String getProfileSidebarFillColor()
  {
    return this.profileSidebarFillColor;
  }
  
  public String getProfileTextColor()
  {
    return this.profileTextColor;
  }
  
  public boolean getProtectedUser()
  {
    return this.protectedUser;
  }
  
  public String getScreenName()
  {
    return this.screenName;
  }
  
  public Status getStatus()
  {
    return this.status;
  }
  
  public int getStatusesCount()
  {
    return this.statusesCount;
  }
  
  public String getTimezone()
  {
    return this.timezone;
  }
  
  public double getTimezoneOffSet()
  {
    return this.timezoneOffSet;
  }
  
  public URI getWebsite()
  {
    return this.website;
  }
  
  public int hashCode()
  {
    return this.screenName.hashCode();
  }
  
  public boolean isDummyObject()
  {
    return this.name == null;
  }
  
  public Boolean isFollowedByYou()
  {
    return this.followedByYou;
  }
  
  public Boolean isFollowingYou()
  {
    return this.followingYou;
  }
  
  public boolean isNotifications()
  {
    return this.notifications;
  }
  
  public boolean isProfileBackgroundTile()
  {
    return this.profileBackgroundTile;
  }
  
  public boolean isProtectedUser()
  {
    return this.protectedUser;
  }
  
  public boolean isVerified()
  {
    return this.verified;
  }
  
  public String toString()
  {
    return this.screenName;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\User.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */