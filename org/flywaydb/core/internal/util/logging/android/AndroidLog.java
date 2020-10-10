package org.flywaydb.core.internal.util.logging.android;

public class AndroidLog
  implements org.flywaydb.core.internal.util.logging.Log
{
  private static final String TAG = "Flyway";
  
  public void debug(String message)
  {
    android.util.Log.d("Flyway", message);
  }
  
  public void info(String message)
  {
    android.util.Log.i("Flyway", message);
  }
  
  public void warn(String message)
  {
    android.util.Log.w("Flyway", message);
  }
  
  public void error(String message)
  {
    android.util.Log.e("Flyway", message);
  }
  
  public void error(String message, Exception e)
  {
    android.util.Log.e("Flyway", message, e);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\logging\android\AndroidLog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */