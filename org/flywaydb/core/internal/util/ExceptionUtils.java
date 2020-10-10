package org.flywaydb.core.internal.util;

public class ExceptionUtils
{
  public static Throwable getRootCause(Throwable throwable)
  {
    if (throwable == null) {
      return null;
    }
    Throwable cause = throwable;
    Throwable rootCause;
    while ((rootCause = cause.getCause()) != null) {
      cause = rootCause;
    }
    return cause;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\ExceptionUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */