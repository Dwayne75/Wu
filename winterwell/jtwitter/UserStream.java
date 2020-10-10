package winterwell.jtwitter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserStream
  extends AStream
{
  boolean withFollowings;
  
  public UserStream(Twitter jtwit)
  {
    super(jtwit);
  }
  
  HttpURLConnection connect2()
    throws IOException
  {
    connect3_rateLimit();
    
    String url = "https://userstream.twitter.com/2/user.json?delimited=length";
    
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "with", this.withFollowings ? "followings" : "user" });
    HttpURLConnection con = this.client.connect(url, vars, true);
    return con;
  }
  
  private void connect3_rateLimit()
  {
    if (this.jtwit.getScreenName() == null) {
      return;
    }
    AStream s = (AStream)user2stream.get(this.jtwit.getScreenName());
    if ((s != null) && (s.isConnected())) {
      throw new TwitterException.TooManyLogins("One account, one UserStream");
    }
    if (user2stream.size() > 500) {
      user2stream = new ConcurrentHashMap();
    }
    user2stream.put(this.jtwit.getScreenName(), this);
  }
  
  static Map<String, AStream> user2stream = new ConcurrentHashMap();
  
  void fillInOutages2(Twitter jtwit2, AStream.Outage outage)
    throws UnsupportedOperationException, TwitterException
  {
    if (this.withFollowings) {
      throw new UnsupportedOperationException("TODO");
    }
    List<Status> mentions = jtwit2.getMentions();
    for (Status status : mentions) {
      if (!this.tweets.contains(status)) {
        this.tweets.add(status);
      }
    }
    List<Status> updates = jtwit2.getUserTimeline(jtwit2.getScreenName());
    for (Status status : updates) {
      if (!this.tweets.contains(status)) {
        this.tweets.add(status);
      }
    }
    Object dms = jtwit2.getDirectMessages();
    for (Twitter.ITweet dm : (List)dms) {
      if (!this.tweets.contains(dm)) {
        this.tweets.add(dm);
      }
    }
  }
  
  public Collection<Long> getFriends()
  {
    return this.friends;
  }
  
  public void setWithFollowings(boolean withFollowings)
  {
    assert (!isConnected());
    this.withFollowings = withFollowings;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder("UserStream");
    sb.append("[" + this.jtwit.getScreenNameIfKnown());
    if (this.withFollowings) {
      sb.append(" +followings");
    }
    sb.append("]");
    return sb.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\UserStream.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */