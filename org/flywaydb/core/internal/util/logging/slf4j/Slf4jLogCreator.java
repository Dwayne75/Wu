package org.flywaydb.core.internal.util.logging.slf4j;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogCreator;
import org.slf4j.LoggerFactory;

public class Slf4jLogCreator
  implements LogCreator
{
  public Log createLogger(Class<?> clazz)
  {
    return new Slf4jLog(LoggerFactory.getLogger(clazz));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\logging\slf4j\Slf4jLogCreator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */