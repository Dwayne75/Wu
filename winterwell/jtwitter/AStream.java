package winterwell.jtwitter;

import java.io.Closeable;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import winterwell.json.JSONArray;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public abstract class AStream
  implements Closeable
{
  public static abstract interface IListen
  {
    public abstract boolean processEvent(TwitterEvent paramTwitterEvent);
    
    public abstract boolean processSystemEvent(Object[] paramArrayOfObject);
    
    public abstract boolean processTweet(Twitter.ITweet paramITweet);
  }
  
  public static final class Outage
    implements Serializable
  {
    private static final long serialVersionUID = 1L;
    public final BigInteger sinceId;
    public final long untilTime;
    
    public Outage(BigInteger sinceId, long untilTime)
    {
      this.sinceId = sinceId;
      this.untilTime = untilTime;
    }
    
    public String toString()
    {
      return "Outage[id:" + this.sinceId + " to time:" + this.untilTime + "]";
    }
  }
  
  public static int MAX_BUFFER = 10000;
  private static final int MAX_WAIT_SECONDS = 900;
  boolean autoReconnect;
  final Twitter.IHttpClient client;
  
  static int forgetIfFull(List incoming)
  {
    if (incoming.size() < MAX_BUFFER) {
      return 0;
    }
    int chop = MAX_BUFFER / 10;
    for (int i = 0; i < chop; i++) {
      incoming.remove(0);
    }
    return chop;
  }
  
  static Object read3_parse(JSONObject jo, Twitter jtwitr)
    throws JSONException
  {
    if (jo.has("text"))
    {
      Status tweet = new Status(jo, null);
      return tweet;
    }
    if (jo.has("direct_message"))
    {
      Message dm = new Message(jo.getJSONObject("direct_message"));
      return dm;
    }
    String eventType = jo.optString("event");
    if (eventType != "")
    {
      TwitterEvent event = new TwitterEvent(jo, jtwitr);
      return event;
    }
    JSONObject del = jo.optJSONObject("delete");
    if (del != null)
    {
      boolean isDM = false;
      JSONObject s = del.optJSONObject("status");
      if (s == null)
      {
        s = del.getJSONObject("direct_message");
        isDM = true;
      }
      BigInteger id = new BigInteger(s.getString("id_str"));
      BigInteger userId = new BigInteger(s.getString("user_id"));
      
      User dummyUser = new User(null, userId);
      Twitter.ITweet deadTweet;
      Twitter.ITweet deadTweet;
      if (isDM) {
        deadTweet = new Message(dummyUser, id);
      } else {
        deadTweet = new Status(dummyUser, null, id, null);
      }
      return new Object[] { "delete", deadTweet, userId };
    }
    JSONObject limit = jo.optJSONObject("limit");
    if (limit != null)
    {
      int cnt = limit.optInt("track");
      if (cnt == 0) {
        System.out.println(jo);
      }
      return new Object[] { "limit", Integer.valueOf(cnt) };
    }
    JSONObject disconnect = jo.optJSONObject("disconnect");
    if (disconnect != null) {
      return new Object[] { "disconnect", disconnect };
    }
    System.out.println(jo);
    return new Object[] { "unknown", jo };
  }
  
  List<TwitterEvent> events = new ArrayList();
  boolean fillInFollows = true;
  private int forgotten;
  List<Long> friends;
  final Twitter jtwit;
  private BigInteger lastId = BigInteger.ZERO;
  final List<IListen> listeners = new ArrayList(0);
  final List<Outage> outages = Collections.synchronizedList(new ArrayList());
  int previousCount;
  StreamGobbler readThread;
  InputStream stream;
  List<Object[]> sysEvents = new ArrayList();
  List<Twitter.ITweet> tweets = new ArrayList();
  boolean listenersOnly;
  
  public AStream(Twitter jtwit)
  {
    this.client = jtwit.getHttpClient();
    this.jtwit = jtwit;
    
    this.client.setTimeout(91000);
  }
  
  public void addListener(IListen listener)
  {
    synchronized (this.listeners)
    {
      this.listeners.remove(listener);
      
      this.listeners.add(0, listener);
    }
  }
  
  public void addOutage(Outage outage)
  {
    for (int i = 0; i < this.outages.size(); i++)
    {
      Outage o = (Outage)this.outages.get(i);
      if (o.sinceId.compareTo(outage.sinceId) > 0)
      {
        this.outages.add(i, outage);
        return;
      }
    }
    this.outages.add(outage);
  }
  
  public void clear()
  {
    this.outages.clear();
    popEvents();
    popSystemEvents();
    popTweets();
  }
  
  public synchronized void close()
  {
    if ((this.readThread != null) && (Thread.currentThread() != this.readThread))
    {
      this.readThread.pleaseStop();
      if (this.readThread.isAlive())
      {
        try
        {
          Thread.sleep(100L);
        }
        catch (InterruptedException localInterruptedException) {}
        this.readThread.interrupt();
      }
      this.readThread = null;
    }
    InternalUtils.close(this.stream);
    this.stream = null;
  }
  
  public synchronized void connect()
    throws TwitterException
  {
    if (isConnected()) {
      return;
    }
    close();
    
    assert ((this.readThread == null) || (this.readThread.stream == this)) : this;
    
    HttpURLConnection con = null;
    try
    {
      con = connect2();
      this.stream = con.getInputStream();
      if (this.readThread == null)
      {
        this.readThread = new StreamGobbler(this);
        this.readThread.setName("Gobble:" + toString());
        this.readThread.start();
      }
      else
      {
        assert (Thread.currentThread() == this.readThread) : this;
        assert (this.readThread.stream == this) : this.readThread;
      }
      if (isConnected()) {
        return;
      }
      Thread.sleep(10L);
      if (!isConnected()) {
        throw new TwitterException(this.readThread.ex);
      }
    }
    catch (Exception e)
    {
      if ((e instanceof TwitterException)) {
        throw ((TwitterException)e);
      }
      throw new TwitterException(e);
    }
  }
  
  abstract HttpURLConnection connect2()
    throws Exception;
  
  public final Exception fillInOutages()
    throws UnsupportedOperationException
  {
    if (this.outages.size() == 0) {
      return null;
    }
    Outage[] outs = (Outage[])this.outages.toArray(new Outage[0]);
    
    Twitter jtwit2 = new Twitter(this.jtwit);
    Exception ex = null;
    Outage[] arrayOfOutage1;
    int j = (arrayOfOutage1 = outs).length;
    for (int i = 0; i < j; i++)
    {
      Outage outage = arrayOfOutage1[i];
      if (System.currentTimeMillis() - outage.untilTime >= 60000L)
      {
        boolean ok = this.outages.remove(outage);
        if (ok) {
          try
          {
            jtwit2.setSinceId(outage.sinceId);
            jtwit2.setUntilDate(new Date(outage.untilTime));
            jtwit2.setMaxResults(100000);
            
            fillInOutages2(jtwit2, outage);
          }
          catch (Throwable e)
          {
            this.outages.add(outage);
            if ((e instanceof Exception)) {
              ex = (Exception)e;
            }
          }
        }
      }
    }
    return ex;
  }
  
  abstract void fillInOutages2(Twitter paramTwitter, Outage paramOutage);
  
  protected void finalize()
    throws Throwable
  {
    close();
  }
  
  public final List<TwitterEvent> getEvents()
  {
    read();
    return this.events;
  }
  
  public final int getForgotten()
  {
    return this.forgotten;
  }
  
  public final List<Outage> getOutages()
  {
    return this.outages;
  }
  
  public final List<Object[]> getSystemEvents()
  {
    read();
    return this.sysEvents;
  }
  
  public final List<Twitter.ITweet> getTweets()
  {
    read();
    
    return this.tweets;
  }
  
  public final boolean isAlive()
  {
    if (isConnected()) {
      return true;
    }
    if (!this.autoReconnect) {
      return false;
    }
    return (this.readThread != null) && (this.readThread.isAlive()) && 
      (!this.readThread.stopFlag);
  }
  
  public final boolean isConnected()
  {
    if ((this.readThread != null) && (this.readThread.isAlive()) && 
      (this.readThread.ex == null)) {
      if (!this.readThread.stopFlag) {
        return true;
      }
    }
    return false;
  }
  
  public final List<TwitterEvent> popEvents()
  {
    List evs = getEvents();
    this.events = new ArrayList();
    return evs;
  }
  
  public final List<Object[]> popSystemEvents()
  {
    List<Object[]> evs = getSystemEvents();
    this.sysEvents = new ArrayList();
    return evs;
  }
  
  public final List<Twitter.ITweet> popTweets()
  {
    List<Twitter.ITweet> ts = getTweets();
    
    this.tweets = new ArrayList();
    return ts;
  }
  
  private final void read()
  {
    if (this.readThread != null)
    {
      String[] jsons = this.readThread.popJsons();
      String[] arrayOfString1;
      int j = (arrayOfString1 = jsons).length;
      for (int i = 0; i < j; i++)
      {
        String json = arrayOfString1[i];
        try
        {
          read2(json);
        }
        catch (JSONException e)
        {
          throw new TwitterException.Parsing(json, e);
        }
      }
    }
    if (isConnected()) {
      return;
    }
    if ((this.readThread != null) && (this.readThread.stopFlag)) {
      return;
    }
    Exception ex = this.readThread == null ? null : this.readThread.ex;
    
    close();
    if (!this.autoReconnect)
    {
      if ((ex instanceof TwitterException)) {
        throw ((TwitterException)ex);
      }
      throw new TwitterException(ex);
    }
    reconnect();
  }
  
  private void read2(String json)
    throws JSONException
  {
    JSONObject jobj = new JSONObject(json);
    
    JSONArray _friends = jobj.optJSONArray("friends");
    if (_friends != null)
    {
      read3_friends(_friends);
      return;
    }
    Object object = read3_parse(jobj, this.jtwit);
    if ((object instanceof Twitter.ITweet))
    {
      Twitter.ITweet tweet = (Twitter.ITweet)object;
      if (this.tweets.contains(tweet)) {
        return;
      }
      this.tweets.add(tweet);
      if ((tweet instanceof Status))
      {
        BigInteger id = ((Status)tweet).id;
        if (id.compareTo(this.lastId) > 0) {
          this.lastId = id;
        }
      }
      this.forgotten += forgetIfFull(this.tweets);
      return;
    }
    if ((object instanceof TwitterEvent))
    {
      TwitterEvent event = (TwitterEvent)object;
      this.events.add(event);
      this.forgotten += forgetIfFull(this.events);
      return;
    }
    if ((object instanceof Object[]))
    {
      Object[] sysEvent = (Object[])object;
      if ("delete".equals(sysEvent[0]))
      {
        Twitter.ITweet deadTweet = (Twitter.ITweet)sysEvent[1];
        
        boolean pruned = this.tweets.remove(deadTweet);
        if (!pruned) {}
      }
      else if ("limit".equals(sysEvent[0]))
      {
        Integer cnt = (Integer)sysEvent[1];
        this.forgotten += cnt.intValue();
      }
      this.sysEvents.add(sysEvent);
      this.forgotten += forgetIfFull(this.sysEvents);
      return;
    }
    System.out.println(jobj);
  }
  
  private void read3_friends(JSONArray _friends)
    throws JSONException
  {
    List<Long> oldFriends = this.friends;
    this.friends = new ArrayList(_friends.length());
    int i = 0;
    for (int n = _friends.length(); i < n; i++)
    {
      long fi = _friends.getLong(i);
      this.friends.add(Long.valueOf(fi));
    }
    if ((oldFriends == null) || (!this.fillInFollows)) {
      return;
    }
    HashSet<Long> friends2 = new HashSet(this.friends);
    friends2.removeAll(oldFriends);
    if (friends2.size() == 0) {
      return;
    }
    Twitter_Users tu = new Twitter_Users(this.jtwit);
    List<User> newFriends = tu.showById(friends2);
    User you = this.jtwit.getSelf();
    for (User nf : newFriends)
    {
      TwitterEvent e = new TwitterEvent(new Date(), you, 
        "follow", nf, null);
      this.events.add(e);
    }
    this.forgotten += forgetIfFull(this.events);
  }
  
  synchronized void reconnect()
  {
    long now = System.currentTimeMillis();
    reconnect2();
    long dt = System.currentTimeMillis() - now;
    addSysEvent(new Object[] { "reconnect", Long.valueOf(dt) });
    if (this.lastId != BigInteger.ZERO)
    {
      this.outages.add(new Outage(this.lastId, System.currentTimeMillis()));
      if (this.outages.size() > 100000)
      {
        for (int i = 0; i < 1000; i++) {
          this.outages.remove(0);
        }
        this.forgotten += 10000;
      }
    }
  }
  
  void addSysEvent(Object[] sysEvent)
  {
    this.sysEvents.add(sysEvent);
    if (this.listeners.size() == 0) {
      return;
    }
    synchronized (this.listeners)
    {
      try
      {
        for (IListen listener : this.listeners)
        {
          boolean carryOn = listener.processSystemEvent(sysEvent);
          if (!carryOn) {
            break;
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  private void reconnect2()
  {
    try
    {
      connect();
      return;
    }
    catch (TwitterException.E40X e)
    {
      throw e;
    }
    catch (Exception e)
    {
      System.out.println(e);
      
      int wait = 20 + new Random().nextInt(40);
      int waited = 0;
      while (waited < 900) {
        try
        {
          Thread.sleep(wait * 1000);
          waited += wait;
          if (wait < 300) {
            wait *= 2;
          }
          connect();
          
          return;
        }
        catch (TwitterException.E40X e)
        {
          throw e;
        }
        catch (Exception e)
        {
          System.out.println(e);
        }
      }
      throw new TwitterException.E50X("Could not connect to streaming server");
    }
  }
  
  synchronized void reconnectFromGobblerThread()
  {
    assert ((Thread.currentThread() == this.readThread) || (this.readThread == null)) : this;
    if (isConnected()) {
      return;
    }
    reconnect();
  }
  
  /* Error */
  public boolean removeListener(IListen listener)
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 221	winterwell/jtwitter/AStream:listeners	Ljava/util/List;
    //   4: dup
    //   5: astore_2
    //   6: monitorenter
    //   7: aload_0
    //   8: getfield 221	winterwell/jtwitter/AStream:listeners	Ljava/util/List;
    //   11: aload_1
    //   12: invokeinterface 253 2 0
    //   17: aload_2
    //   18: monitorexit
    //   19: ireturn
    //   20: aload_2
    //   21: monitorexit
    //   22: athrow
    // Line number table:
    //   Java source line #783	-> byte code offset #0
    //   Java source line #784	-> byte code offset #7
    //   Java source line #783	-> byte code offset #20
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	23	0	this	AStream
    //   0	23	1	listener	IListen
    //   5	16	2	Ljava/lang/Object;	Object
    // Exception table:
    //   from	to	target	type
    //   7	19	20	finally
    //   20	22	20	finally
  }
  
  public void setAutoReconnect(boolean yes)
  {
    this.autoReconnect = yes;
  }
  
  @Deprecated
  public void setPreviousCount(int previousCount)
  {
    this.previousCount = previousCount;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\AStream.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */