package winterwell.jtwitter.ecosystem;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import winterwell.jtwitter.InternalUtils;
import winterwell.jtwitter.Status;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.IHttpClient;
import winterwell.jtwitter.TwitterException.TwitLongerException;
import winterwell.jtwitter.URLConnectionHttpClient;

public class TwitLonger
{
  Twitter.IHttpClient http;
  private Twitter jtwit;
  private String twitlongerApiKey;
  private String twitlongerAppName;
  
  public TwitLonger()
  {
    this.http = new URLConnectionHttpClient();
  }
  
  public TwitLonger(Twitter jtwitter, String twitlongerApiKey, String twitlongerAppName)
  {
    this.twitlongerApiKey = twitlongerApiKey;
    this.twitlongerAppName = twitlongerAppName;
    this.http = jtwitter.getHttpClient();
    this.jtwit = jtwitter;
    if ((twitlongerApiKey == null) || (twitlongerAppName == null)) {
      throw new IllegalStateException("Incomplete Twitlonger api details");
    }
  }
  
  public Status updateLongStatus(String message, Number inReplyToStatusId)
  {
    assert (this.twitlongerApiKey != null) : "Wrong constructor used -- you must supply an api-key to post";
    if (message.length() < 141) {
      throw new IllegalArgumentException("Message too short (" + 
        inReplyToStatusId + 
        " chars). Just post a normal Twitter status. ");
    }
    String url = "http://www.twitlonger.com/api_post";
    
    Map<String, String> vars = InternalUtils.asMap(new Object[] { "application", this.twitlongerAppName, "api_key", this.twitlongerApiKey, "username", this.jtwit.getScreenName(), "message", message });
    if ((inReplyToStatusId != null) && (inReplyToStatusId.doubleValue() != 0.0D)) {
      vars.put("in_reply", inReplyToStatusId.toString());
    }
    String response = this.http.post(url, vars, false);
    Matcher m = contentTag.matcher(response);
    boolean ok = m.find();
    if (!ok) {
      throw new TwitterException.TwitLongerException(
        "TwitLonger call failed", response);
    }
    String shortMsg = m.group(1).trim();
    
    Status s = this.jtwit.updateStatus(shortMsg, inReplyToStatusId);
    
    m = idTag.matcher(response);
    ok = m.find();
    if (!ok) {
      return s;
    }
    String id = m.group(1);
    try
    {
      url = "http://www.twitlonger.com/api_set_id";
      vars.remove("message");
      vars.remove("in_reply");
      vars.remove("username");
      vars.put("message_id", id);
      vars.put("twitter_id", s.getId());
      this.http.post(url, vars, false);
    }
    catch (Exception localException) {}
    return s;
  }
  
  static final Pattern contentTag = Pattern.compile(
    "<content>(.+?)<\\/content>", 32);
  static final Pattern idTag = Pattern.compile("<id>(.+?)<\\/id>", 
    32);
  
  public String getLongStatus(Status truncatedStatus)
  {
    int i = truncatedStatus.text.indexOf("http://tl.gd/");
    if (i == -1) {
      return truncatedStatus.text;
    }
    String id = truncatedStatus.text.substring(i + 13).trim();
    String response = this.http.getPage("http://www.twitlonger.com/api_read/" + 
      id, null, false);
    Matcher m = contentTag.matcher(response);
    boolean ok = m.find();
    if (!ok) {
      throw new TwitterException.TwitLongerException(
        "TwitLonger call failed", response);
    }
    String longMsg = m.group(1).trim();
    return longMsg;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\ecosystem\TwitLonger.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */