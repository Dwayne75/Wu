package org.seamless.swing.logging;

import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public abstract class LoggingHandler
  extends Handler
{
  public int sourcePathElements = 3;
  
  public LoggingHandler() {}
  
  public LoggingHandler(int sourcePathElements)
  {
    this.sourcePathElements = sourcePathElements;
  }
  
  public void publish(LogRecord logRecord)
  {
    LogMessage logMessage = new LogMessage(logRecord.getLevel(), getSource(logRecord), logRecord.getMessage());
    
    log(logMessage);
  }
  
  public void flush() {}
  
  public void close()
    throws SecurityException
  {}
  
  protected String getSource(LogRecord record)
  {
    StringBuilder sb = new StringBuilder(180);
    String[] split = record.getSourceClassName().split("\\.");
    if (split.length > this.sourcePathElements) {
      split = (String[])Arrays.copyOfRange(split, split.length - this.sourcePathElements, split.length);
    }
    for (String s : split) {
      sb.append(s).append(".");
    }
    sb.append(record.getSourceMethodName());
    return sb.toString();
  }
  
  protected abstract void log(LogMessage paramLogMessage);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\logging\LoggingHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */