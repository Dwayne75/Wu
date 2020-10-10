package com.sun.javaws;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.DialogFactory;
import com.sun.deploy.util.WinRegistry;
import java.awt.Window;

public class WinExtensionInstallHandler
  extends ExtensionInstallHandler
{
  private static final String KEY_RUNONCE = "Software\\Microsoft\\Windows\\CurrentVersion\\RunOnce";
  
  static
  {
    NativeLibrary.getInstance().load();
  }
  
  public boolean doPreRebootActions(Window paramWindow)
  {
    int[] arrayOfInt = { 1 };
    paramWindow.setVisible(true);
    paramWindow.requestFocus();
    arrayOfInt[0] = DialogFactory.showConfirmDialog(paramWindow, ResourceManager.getString("extensionInstall.rebootMessage"), ResourceManager.getString("extensionInstall.rebootTitle"));
    
    paramWindow.setVisible(false);
    
    return arrayOfInt[0] == 0;
  }
  
  public boolean doReboot()
  {
    return WinRegistry.doReboot();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\WinExtensionInstallHandler.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */