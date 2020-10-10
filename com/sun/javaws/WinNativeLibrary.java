package com.sun.javaws;

import com.sun.deploy.config.Config;
import java.io.File;

public class WinNativeLibrary
  extends NativeLibrary
{
  private static boolean isLoaded = false;
  
  public synchronized void load()
  {
    if (!isLoaded)
    {
      String str = Config.getJavaHome() + File.separator + "bin" + File.separator + "deploy.dll";
      
      System.load(str);
      isLoaded = true;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\WinNativeLibrary.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */