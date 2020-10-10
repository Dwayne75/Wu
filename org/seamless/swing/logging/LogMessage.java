package org.seamless.swing.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class LogMessage
{
  private Level level;
  private Long createdOn = Long.valueOf(new Date().getTime());
  private String thread = Thread.currentThread().getName();
  private String source;
  private String message;
  
  public LogMessage(String message)
  {
    this(Level.INFO, message);
  }
  
  public LogMessage(String source, String message)
  {
    this(Level.INFO, source, message);
  }
  
  public LogMessage(Level level, String message)
  {
    this(level, null, message);
  }
  
  public LogMessage(Level level, String source, String message)
  {
    this.level = level;
    this.source = source;
    this.message = message;
  }
  
  public Level getLevel()
  {
    return this.level;
  }
  
  public Long getCreatedOn()
  {
    return this.createdOn;
  }
  
  public String getThread()
  {
    return this.thread;
  }
  
  public String getSource()
  {
    return this.source;
  }
  
  public String getMessage()
  {
    return this.message;
  }
  
  public String toString()
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
    return getLevel() + " - " + dateFormat.format(new Date(getCreatedOn().longValue())) + " - " + getThread() + " : " + getSource() + " : " + getMessage();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\logging\LogMessage.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */