package org.flywaydb.core.internal.util.logging;

public abstract interface Log
{
  public abstract void debug(String paramString);
  
  public abstract void info(String paramString);
  
  public abstract void warn(String paramString);
  
  public abstract void error(String paramString);
  
  public abstract void error(String paramString, Exception paramException);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\logging\Log.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */