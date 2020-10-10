package com.wurmonline.shared.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;

public final class IoUtilities
{
  public static void closeClosable(Closeable closableObject)
  {
    if (closableObject != null) {
      try
      {
        closableObject.close();
      }
      catch (IOException localIOException) {}
    }
  }
  
  public static void closeHttpURLConnection(HttpURLConnection httpURLConnection)
  {
    if (httpURLConnection != null) {
      try
      {
        httpURLConnection.disconnect();
      }
      catch (Exception localException) {}
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\util\IoUtilities.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */