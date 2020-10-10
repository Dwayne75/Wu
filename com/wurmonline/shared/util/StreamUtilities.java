package com.wurmonline.shared.util;

import java.io.InputStream;
import java.io.OutputStream;

public final class StreamUtilities
{
  public static void closeInputStreamIgnoreExceptions(InputStream aInputStream)
  {
    if (aInputStream != null) {
      try
      {
        aInputStream.close();
      }
      catch (Exception localException) {}
    }
  }
  
  public static void closeOutputStreamIgnoreExceptions(OutputStream aOutputStream)
  {
    if (aOutputStream != null) {
      try
      {
        aOutputStream.close();
      }
      catch (Exception localException) {}
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\util\StreamUtilities.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */