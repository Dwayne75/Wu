package winterwell.jtwitter;

import java.util.Date;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public class TwitterEvent
{
  public final Date createdAt;
  public final User source;
  public final User target;
  private Object targetObject;
  public final String type;
  
  TwitterEvent(Date createdAt, User source, String type, User target, Object targetObject)
  {
    this.createdAt = createdAt;
    this.source = source;
    this.type = type;
    this.target = target;
    this.targetObject = targetObject;
  }
  
  public TwitterEvent(JSONObject jo, Twitter jtwit)
    throws JSONException
  {
    this.type = jo.getString("event");
    this.target = new User(jo.getJSONObject("target"), null);
    this.source = new User(jo.getJSONObject("source"), null);
    this.createdAt = InternalUtils.parseDate(jo.getString("created_at"));
    
    JSONObject to = jo.optJSONObject("target_object");
    if (to == null) {
      return;
    }
    if (to.has("member_count"))
    {
      this.targetObject = new TwitterList(to, jtwit);
      return;
    }
    try
    {
      this.targetObject = new Status(to, null);
    }
    catch (Exception ex)
    {
      this.targetObject = to;
    }
  }
  
  public Date getCreatedAt()
  {
    return this.createdAt;
  }
  
  public User getSource()
  {
    return this.source;
  }
  
  public User getTarget()
  {
    return this.target;
  }
  
  public Object getTargetObject()
  {
    return this.targetObject;
  }
  
  public String getType()
  {
    return this.type;
  }
  
  public boolean is(String type)
  {
    return this.type.equals(type);
  }
  
  public String toString()
  {
    return this.source + " " + this.type + " " + this.target + " " + getTargetObject();
  }
  
  public static abstract interface Type
  {
    public static final String ADDED_TO_LIST = "list_member_added";
    public static final String FAVORITE = "favorite";
    public static final String FOLLOW = "follow";
    public static final String LIST_CREATED = "list_created";
    public static final String REMOVED_FROM_LIST = "list_member_removed";
    public static final String UNFAVORITE = "unfavorite";
    public static final String USER_UPDATE = "user_update";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\TwitterEvent.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */