package winterwell.jtwitter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import winterwell.json.JSONArray;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public final class Message
  implements Twitter.ITweet
{
  private static final long serialVersionUID = 1L;
  private final Date createdAt;
  private EnumMap<Twitter.KEntityType, List<Twitter.TweetEntity>> entities;
  public final Number id;
  public Number inReplyToMessageId;
  private String location;
  private Place place;
  private final User recipient;
  private final User sender;
  public final String text;
  
  public String getDisplayText()
  {
    return Status.getDisplayText2(this);
  }
  
  static List<Message> getMessages(String json)
    throws TwitterException
  {
    if (json.trim().equals("")) {
      return Collections.emptyList();
    }
    try
    {
      List<Message> msgs = new ArrayList();
      JSONArray arr = new JSONArray(json);
      for (int i = 0; i < arr.length(); i++)
      {
        JSONObject obj = arr.getJSONObject(i);
        Message u = new Message(obj);
        msgs.add(u);
      }
      return msgs;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  /**
   * @deprecated
   */
  Message(User dummyUser, Number id)
  {
    this.sender = dummyUser;
    this.id = id;
    this.recipient = null;
    this.createdAt = null;
    this.text = null;
  }
  
  Message(JSONObject obj)
    throws JSONException, TwitterException
  {
    this.id = Long.valueOf(obj.getLong("id"));
    String _text = obj.getString("text");
    this.text = InternalUtils.unencode(_text);
    String c = InternalUtils.jsonGet("created_at", obj);
    this.createdAt = InternalUtils.parseDate(c);
    this.sender = new User(obj.getJSONObject("sender"), null);
    
    Object recip = obj.opt("recipient");
    if ((recip instanceof JSONObject)) {
      this.recipient = new User((JSONObject)recip, null);
    } else {
      this.recipient = null;
    }
    JSONObject jsonEntities = obj.optJSONObject("entities");
    if (jsonEntities != null)
    {
      this.entities = new EnumMap(
        Twitter.KEntityType.class);
      Twitter.KEntityType[] arrayOfKEntityType;
      int j = (arrayOfKEntityType = Twitter.KEntityType.values()).length;
      for (int i = 0; i < j; i++)
      {
        Twitter.KEntityType type = arrayOfKEntityType[i];
        List<Twitter.TweetEntity> es = Twitter.TweetEntity.parse(this, _text, type, 
          jsonEntities);
        this.entities.put(type, es);
      }
    }
    Object _locn = Status.jsonGetLocn(obj);
    this.location = (_locn == null ? null : _locn.toString());
    if ((_locn instanceof Place)) {
      this.place = ((Place)_locn);
    }
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
    Message other = (Message)obj;
    return this.id.equals(other.id);
  }
  
  public Date getCreatedAt()
  {
    return this.createdAt;
  }
  
  public BigInteger getId()
  {
    if ((this.id instanceof Long)) {
      return BigInteger.valueOf(this.id.longValue());
    }
    return (BigInteger)this.id;
  }
  
  public String getLocation()
  {
    return this.location;
  }
  
  public List<String> getMentions()
  {
    return Collections.singletonList(this.recipient.screenName);
  }
  
  public Place getPlace()
  {
    return this.place;
  }
  
  public User getRecipient()
  {
    return this.recipient;
  }
  
  public User getSender()
  {
    return this.sender;
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
    return getSender();
  }
  
  public int hashCode()
  {
    return this.id.hashCode();
  }
  
  public String toString()
  {
    return this.text;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\Message.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */