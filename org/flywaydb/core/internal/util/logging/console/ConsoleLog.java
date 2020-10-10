package org.flywaydb.core.internal.util.logging.console;

import java.io.PrintStream;
import org.flywaydb.core.internal.util.logging.Log;

public class ConsoleLog
  implements Log
{
  private final Level level;
  
  public static enum Level
  {
    DEBUG,  INFO,  WARN;
    
    private Level() {}
  }
  
  public ConsoleLog(Level level)
  {
    this.level = level;
  }
  
  public void debug(String message)
  {
    if (this.level == Level.DEBUG) {
      System.out.println("DEBUG: " + message);
    }
  }
  
  public void info(String message)
  {
    if (this.level.compareTo(Level.INFO) <= 0) {
      System.out.println(message);
    }
  }
  
  public void warn(String message)
  {
    System.out.println("WARNING: " + message);
  }
  
  public void error(String message)
  {
    System.err.println("ERROR: " + message);
  }
  
  public void error(String message, Exception e)
  {
    System.err.println("ERROR: " + message);
    e.printStackTrace(System.err);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\logging\console\ConsoleLog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */