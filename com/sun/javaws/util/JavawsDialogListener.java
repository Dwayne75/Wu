package com.sun.javaws.util;

import com.sun.deploy.util.DialogListener;
import com.sun.javaws.SplashScreen;
import java.security.AccessController;
import java.security.PrivilegedAction;

public final class JavawsDialogListener
  implements DialogListener
{
  public void beforeShow()
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        SplashScreen.hide();
        return null;
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\util\JavawsDialogListener.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */