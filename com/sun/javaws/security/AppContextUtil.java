package com.sun.javaws.security;

import sun.awt.AppContext;
import sun.awt.SunToolkit;

public class AppContextUtil
{
  private static AppContext _mainAppContext = null;
  private static AppContext _securityAppContext = null;
  
  public static void createSecurityAppContext()
  {
    if (_mainAppContext == null) {
      _mainAppContext = AppContext.getAppContext();
    }
    if (_securityAppContext == null)
    {
      SunToolkit.createNewAppContext();
      _securityAppContext = AppContext.getAppContext();
    }
  }
  
  public static boolean isSecurityAppContext()
  {
    return AppContext.getAppContext() == _securityAppContext;
  }
  
  public static boolean isApplicationAppContext()
  {
    return AppContext.getAppContext() == _mainAppContext;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\security\AppContextUtil.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */