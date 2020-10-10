package org.flywaydb.core.internal.util.logging.javautil;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.flywaydb.core.internal.util.logging.Log;

public class JavaUtilLog
  implements Log
{
  private final Logger logger;
  
  public JavaUtilLog(Logger logger)
  {
    this.logger = logger;
  }
  
  public void debug(String message)
  {
    log(Level.FINE, message, null);
  }
  
  public void info(String message)
  {
    log(Level.INFO, message, null);
  }
  
  public void warn(String message)
  {
    log(Level.WARNING, message, null);
  }
  
  public void error(String message)
  {
    log(Level.SEVERE, message, null);
  }
  
  public void error(String message, Exception e)
  {
    log(Level.SEVERE, message, e);
  }
  
  private void log(Level level, String message, Exception e)
  {
    LogRecord record = new LogRecord(level, message);
    record.setLoggerName(this.logger.getName());
    record.setThrown(e);
    record.setSourceClassName(this.logger.getName());
    record.setSourceMethodName(getMethodName());
    this.logger.log(record);
  }
  
  private String getMethodName()
  {
    StackTraceElement[] steArray = new Throwable().getStackTrace();
    for (StackTraceElement stackTraceElement : steArray) {
      if (this.logger.getName().equals(stackTraceElement.getClassName())) {
        return stackTraceElement.getMethodName();
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\logging\javautil\JavaUtilLog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */