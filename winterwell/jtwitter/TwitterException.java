package winterwell.jtwitter;

import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import winterwell.json.JSONException;

public class TwitterException
  extends RuntimeException
{
  private static final long serialVersionUID = 1L;
  
  public static class AccessLevel
    extends TwitterException.E401
  {
    private static final long serialVersionUID = 1L;
    
    public AccessLevel(String msg)
    {
      super();
    }
  }
  
  public static class BadParameter
    extends TwitterException.E403
  {
    private static final long serialVersionUID = 1L;
    
    public BadParameter(String msg)
    {
      super();
    }
  }
  
  public static class E401
    extends TwitterException.E40X
  {
    private static final long serialVersionUID = 1L;
    
    public E401(String string)
    {
      super();
    }
  }
  
  public static class E403
    extends TwitterException.E40X
  {
    private static final long serialVersionUID = 1L;
    
    public E403(String string)
    {
      super();
    }
  }
  
  public static class E404
    extends TwitterException.E40X
  {
    private static final long serialVersionUID = 1L;
    
    public E404(String string)
    {
      super();
    }
  }
  
  public static class E406
    extends TwitterException.E40X
  {
    private static final long serialVersionUID = 1L;
    
    public E406(String string)
    {
      super();
    }
  }
  
  public static class E40X
    extends TwitterException
  {
    private static final long serialVersionUID = 1L;
    
    public E40X(String string)
    {
      super();
    }
  }
  
  public static class E413
    extends TwitterException.E40X
  {
    private static final long serialVersionUID = 1L;
    
    public E413(String string)
    {
      super();
    }
  }
  
  public static class E416
    extends TwitterException.E40X
  {
    private static final long serialVersionUID = 1L;
    
    public E416(String string)
    {
      super();
    }
  }
  
  public static class E50X
    extends TwitterException
  {
    private static final long serialVersionUID = 1L;
    
    public E50X(String string)
    {
      super();
    }
    
    static String msg(String msg)
    {
      if (msg == null) {
        return null;
      }
      msg = InternalUtils.TAG_REGEX.matcher(msg).replaceAll("");
      msg = msg.replaceAll("\\s+", " ");
      if (msg.length() > 280) {
        msg = msg.substring(0, 280) + "...";
      }
      return msg;
    }
  }
  
  public static class FollowerLimit
    extends TwitterException.E403
  {
    private static final long serialVersionUID = 1L;
    
    public FollowerLimit(String msg)
    {
      super();
    }
  }
  
  public static class IO
    extends TwitterException
  {
    private static final long serialVersionUID = 1L;
    
    public IO(IOException e)
    {
      super();
    }
    
    public IOException getCause()
    {
      return (IOException)super.getCause();
    }
  }
  
  public static class Parsing
    extends TwitterException
  {
    private static final long serialVersionUID = 1L;
    
    private static String clip(String json, int len)
    {
      return 
        json.substring(0, len) + "...";
    }
    
    public Parsing(String json, JSONException e)
    {
      super(e);
    }
    
    private static String causeLine(JSONException e)
    {
      if (e == null) {
        return "";
      }
      StackTraceElement[] st = e.getStackTrace();
      StackTraceElement[] arrayOfStackTraceElement1;
      int j = (arrayOfStackTraceElement1 = st).length;
      for (int i = 0; i < j; i++)
      {
        StackTraceElement ste = arrayOfStackTraceElement1[i];
        if (!ste.getClassName().contains("JSON")) {
          return " caused by " + ste;
        }
      }
      return "";
    }
    
    public Parsing(String date, ParseException e)
    {
      super(e);
    }
  }
  
  public static class RateLimit
    extends TwitterException
  {
    private static final long serialVersionUID = 1L;
    
    public RateLimit(String string)
    {
      super();
    }
  }
  
  public static class Repetition
    extends TwitterException.E403
  {
    private static final long serialVersionUID = 1L;
    
    public Repetition(String tweet)
    {
      super();
    }
  }
  
  public static class SuspendedUser
    extends TwitterException.E403
  {
    private static final long serialVersionUID = 1L;
    
    SuspendedUser(String msg)
    {
      super();
    }
  }
  
  public static class Timeout
    extends TwitterException.E50X
  {
    private static final long serialVersionUID = 1L;
    
    public Timeout(String string)
    {
      super();
    }
  }
  
  public static class TooManyLogins
    extends TwitterException.E40X
  {
    private static final long serialVersionUID = 1L;
    
    public TooManyLogins(String string)
    {
      super();
    }
  }
  
  public static class TooRecent
    extends TwitterException.E403
  {
    private static final long serialVersionUID = 1L;
    
    TooRecent(String msg)
    {
      super();
    }
  }
  
  public static class TwitLongerException
    extends TwitterException
  {
    private static final long serialVersionUID = 1L;
    
    public TwitLongerException(String string, String details)
    {
      super(details);
    }
  }
  
  public static class Unexplained
    extends TwitterException
  {
    private static final long serialVersionUID = 1L;
    
    public Unexplained(String msg)
    {
      super();
    }
  }
  
  public static class UpdateToOAuth
    extends TwitterException.E401
  {
    private static final long serialVersionUID = 1L;
    
    public UpdateToOAuth()
    {
      super();
    }
  }
  
  private String additionalInfo = "";
  
  TwitterException(Exception e)
  {
    super(e);
    
    assert (!(e instanceof TwitterException)) : e;
  }
  
  public TwitterException(String string)
  {
    super(string);
  }
  
  TwitterException(String msg, Exception e)
  {
    super(msg, e);
    
    assert (!(e instanceof TwitterException)) : e;
  }
  
  public TwitterException(String string, String additionalInfo)
  {
    this(string);
    setAdditionalInfo(additionalInfo);
  }
  
  public String getAdditionalInfo()
  {
    return this.additionalInfo;
  }
  
  public void setAdditionalInfo(String additionalInfo)
  {
    this.additionalInfo = additionalInfo;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\TwitterException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */