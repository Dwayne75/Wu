package org.flywaydb.core.internal.util.logging.slf4j;

import org.flywaydb.core.internal.util.logging.Log;
import org.slf4j.Logger;

public class Slf4jLog
  implements Log
{
  private final Logger logger;
  
  public Slf4jLog(Logger logger)
  {
    this.logger = logger;
  }
  
  public void debug(String message)
  {
    this.logger.debug(message);
  }
  
  public void info(String message)
  {
    this.logger.info(message);
  }
  
  public void warn(String message)
  {
    this.logger.warn(message);
  }
  
  public void error(String message)
  {
    this.logger.error(message);
  }
  
  public void error(String message, Exception e)
  {
    this.logger.error(message, e);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\logging\slf4j\Slf4jLog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */