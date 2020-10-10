package org.flywaydb.core.internal.util.logging.android;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogCreator;

public class AndroidLogCreator
  implements LogCreator
{
  public Log createLogger(Class<?> clazz)
  {
    return new AndroidLog();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\logging\android\AndroidLogCreator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */