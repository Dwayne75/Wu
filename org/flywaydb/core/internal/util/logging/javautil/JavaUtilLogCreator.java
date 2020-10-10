package org.flywaydb.core.internal.util.logging.javautil;

import java.util.logging.Logger;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogCreator;

public class JavaUtilLogCreator
  implements LogCreator
{
  public Log createLogger(Class<?> clazz)
  {
    return new JavaUtilLog(Logger.getLogger(clazz.getName()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\logging\javautil\JavaUtilLogCreator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */