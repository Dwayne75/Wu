package com.sun.jnlp;

import java.awt.AWTPermission;
import java.io.FilePermission;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Permission;

public final class CheckServicePermission
{
  private static boolean checkPermission(Permission paramPermission)
  {
    try
    {
      AccessController.checkPermission(paramPermission);
      return true;
    }
    catch (AccessControlException localAccessControlException) {}
    return false;
  }
  
  static boolean hasFileAccessPermissions()
  {
    return checkPermission(new FilePermission("*", "read,write"));
  }
  
  static boolean hasPrintAccessPermissions()
  {
    return checkPermission(new RuntimePermission("queuePrintJob"));
  }
  
  static boolean hasClipboardPermissions()
  {
    return checkPermission(new AWTPermission("accessClipboard"));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\CheckServicePermission.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */