package com.sun.javaws.util;

import java.security.Policy;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavawsConsoleController14
  extends JavawsConsoleController
{
  private static Logger logger = null;
  
  public void setLogger(Logger paramLogger)
  {
    if (logger == null) {
      logger = paramLogger;
    }
  }
  
  public Logger getLogger()
  {
    return logger;
  }
  
  public boolean isSecurityPolicyReloadSupported()
  {
    return true;
  }
  
  public void reloadSecurityPolicy()
  {
    Policy localPolicy = Policy.getPolicy();
    localPolicy.refresh();
  }
  
  public boolean isLoggingSupported()
  {
    return true;
  }
  
  public boolean toggleLogging()
  {
    if (logger != null)
    {
      Level localLevel = logger.getLevel();
      if (localLevel == Level.OFF) {
        localLevel = Level.ALL;
      } else {
        localLevel = Level.OFF;
      }
      logger.setLevel(localLevel);
      
      return localLevel == Level.ALL;
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\util\JavawsConsoleController14.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */