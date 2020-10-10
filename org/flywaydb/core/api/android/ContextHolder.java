package org.flywaydb.core.api.android;

import android.content.Context;

public class ContextHolder
{
  private static Context context;
  
  public static Context getContext()
  {
    return context;
  }
  
  public static void setContext(Context context)
  {
    context = context;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\android\ContextHolder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */