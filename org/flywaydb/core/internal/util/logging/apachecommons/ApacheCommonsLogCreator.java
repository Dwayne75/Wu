package org.flywaydb.core.internal.util.logging.apachecommons;

import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogCreator;

public class ApacheCommonsLogCreator
  implements LogCreator
{
  public Log createLogger(Class<?> clazz)
  {
    return new ApacheCommonsLog(LogFactory.getLog(clazz));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\logging\apachecommons\ApacheCommonsLogCreator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */