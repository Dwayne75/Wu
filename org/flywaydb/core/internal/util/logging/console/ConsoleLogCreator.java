package org.flywaydb.core.internal.util.logging.console;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogCreator;

public class ConsoleLogCreator
  implements LogCreator
{
  private final ConsoleLog.Level level;
  
  public ConsoleLogCreator(ConsoleLog.Level level)
  {
    this.level = level;
  }
  
  public Log createLogger(Class<?> clazz)
  {
    return new ConsoleLog(this.level);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\logging\console\ConsoleLogCreator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */