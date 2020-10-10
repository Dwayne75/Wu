package org.controlsfx.tools;

import java.security.AccessController;
import java.security.PrivilegedAction;

public enum Platform
{
  WINDOWS("windows"),  OSX("mac"),  UNIX("unix"),  UNKNOWN("");
  
  private static Platform current = getCurrentPlatform();
  private String platformId;
  
  private Platform(String platformId)
  {
    this.platformId = platformId;
  }
  
  public String getPlatformId()
  {
    return this.platformId;
  }
  
  public static Platform getCurrent()
  {
    return current;
  }
  
  private static Platform getCurrentPlatform()
  {
    String osName = System.getProperty("os.name");
    if (osName.startsWith("Windows")) {
      return WINDOWS;
    }
    if (osName.startsWith("Mac")) {
      return OSX;
    }
    if (osName.startsWith("SunOS")) {
      return UNIX;
    }
    if (osName.startsWith("Linux"))
    {
      String javafxPlatform = (String)AccessController.doPrivileged(new PrivilegedAction()
      {
        public String run()
        {
          return System.getProperty("javafx.platform");
        }
      });
      if ((!"android".equals(javafxPlatform)) && (!"Dalvik".equals(System.getProperty("java.vm.name")))) {
        return UNIX;
      }
    }
    return UNKNOWN;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\tools\Platform.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */