package winterwell.jtwitter;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TwitterStream
  extends AStream
{
  public static enum KMethod
  {
    filter,  firehose,  links,  retweet,  sample;
  }
  
  public static int MAX_KEYWORDS = 400;
  public static final int MAX_KEYWORD_LENGTH = 60;
  public static final int MAX_USERS = 5000;
  static Map<String, AStream> user2stream = new ConcurrentHashMap();
  private List<Long> follow;
  private List<double[]> locns;
  KMethod method = KMethod.sample;
  private List<String> track;
  
  public TwitterStream(Twitter jtwit)
  {
    super(jtwit);
  }
  
  HttpURLConnection connect2()
    throws Exception
  {
    connect3_rateLimit();
    
    String url = "https://stream.twitter.com/1.1/statuses/" + this.method + ".json";
    Map<String, String> vars = new HashMap();
    if ((this.follow != null) && (this.follow.size() != 0)) {
      vars.put("follow", InternalUtils.join(this.follow, 0, Integer.MAX_VALUE));
    }
    if ((this.track != null) && (this.track.size() != 0)) {
      vars.put("track", InternalUtils.join(this.track, 0, Integer.MAX_VALUE));
    }
    if ((vars.isEmpty()) && (this.method == KMethod.filter)) {
      throw new IllegalStateException("No filters set for " + this);
    }
    vars.put("delimited", "length");
    
    HttpURLConnection con = this.client.post2_connect(url, vars);
    return con;
  }
  
  private void connect3_rateLimit()
  {
    if (this.jtwit.getScreenName() == null) {
      return;
    }
    AStream s = (AStream)user2stream.get(this.jtwit.getScreenName());
    if ((s != null) && (s.isConnected())) {
      throw new TwitterException.TooManyLogins(
        "One account, one stream (running: " + 
        s + 
        "; trying to run" + 
        this + 
        ").\n\tBut streams OR their filter parameters, so one stream can do a lot.");
    }
    if (user2stream.size() > 500) {
      user2stream = new ConcurrentHashMap();
    }
    user2stream.put(this.jtwit.getScreenName(), this);
  }
  
  void fillInOutages2(Twitter jtwit2, AStream.Outage outage)
  {
    if (this.method != KMethod.filter) {
      throw new UnsupportedOperationException();
    }
    Iterator localIterator1;
    Iterator localIterator2;
    if (this.track != null) {
      for (localIterator1 = this.track.iterator(); localIterator1.hasNext(); localIterator2.hasNext())
      {
        String keyword = (String)localIterator1.next();
        List<Status> msgs = this.jtwit.search(keyword);
        localIterator2 = msgs.iterator(); continue;Status status = (Status)localIterator2.next();
        if (!this.tweets.contains(status)) {
          this.tweets.add(status);
        }
      }
    }
    if (this.follow != null) {
      for (localIterator1 = this.follow.iterator(); localIterator1.hasNext(); localIterator2.hasNext())
      {
        Long user = (Long)localIterator1.next();
        List<Status> msgs = this.jtwit.getUserTimeline(user);
        localIterator2 = msgs.iterator(); continue;Status status = (Status)localIterator2.next();
        if (!this.tweets.contains(status)) {
          this.tweets.add(status);
        }
      }
    }
    if ((this.locns != null) && (!this.locns.isEmpty())) {
      throw new UnsupportedOperationException("TODO");
    }
  }
  
  public List<String> getTrackKeywords()
  {
    return this.track;
  }
  
  public void setFollowUsers(List<Long> userIds)
    throws IllegalArgumentException
  {
    this.method = KMethod.filter;
    if ((userIds != null) && (userIds.size() > 5000)) {
      throw new IllegalArgumentException("Track upto 5000 users - not " + userIds.size());
    }
    this.follow = userIds;
  }
  
  public List<Long> getFollowUsers()
  {
    return this.follow;
  }
  
  @Deprecated
  public void setLocation(List<double[]> boundingBoxes)
  {
    this.method = KMethod.filter;
    this.locns = boundingBoxes;
    throw new RuntimeException("TODO! Not implemented yet (sorry)");
  }
  
  void setMethod(KMethod method)
  {
    this.method = method;
  }
  
  public void setTrackKeywords(List<String> keywords)
  {
    if (keywords.size() > MAX_KEYWORDS) {
      throw new IllegalArgumentException("Too many tracked terms: " + keywords.size() + " (" + MAX_KEYWORDS + " limit)");
    }
    for (String kw : keywords) {
      if (kw.length() > 60) {
        throw new IllegalArgumentException("Track term too long: " + kw + " (60 char limit)");
      }
    }
    this.track = keywords;
    this.method = KMethod.filter;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder("TwitterStream");
    sb.append("[" + this.method);
    if (this.track != null) {
      sb.append(" track:" + InternalUtils.join(this.track, 0, 5));
    }
    if ((this.follow != null) && (this.follow.size() > 0)) {
      sb.append(" follow:" + InternalUtils.join(this.follow, 0, 5));
    }
    if (this.locns != null) {
      sb.append(" in:" + InternalUtils.join(this.locns, 0, 5));
    }
    sb.append(" by:" + this.jtwit.getScreenNameIfKnown());
    sb.append("]");
    return sb.toString();
  }
  
  public void setListenersOnly(boolean listenersOnly)
  {
    this.listenersOnly = listenersOnly;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\TwitterStream.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */