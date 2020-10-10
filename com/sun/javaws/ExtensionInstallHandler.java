package com.sun.javaws;

import java.awt.Window;

public abstract class ExtensionInstallHandler
{
  private static ExtensionInstallHandler _installHandler;
  
  public static synchronized ExtensionInstallHandler getInstance()
  {
    if (_installHandler == null) {
      _installHandler = ExtensionInstallHandlerFactory.newInstance();
    }
    return _installHandler;
  }
  
  public abstract boolean doPreRebootActions(Window paramWindow);
  
  public abstract boolean doReboot();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\ExtensionInstallHandler.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */