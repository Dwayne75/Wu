package com.sun.jnlp;

import com.sun.deploy.si.DeploySIListener;
import com.sun.deploy.si.SingleInstanceImpl;
import com.sun.deploy.si.SingleInstanceManager;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.Globals;
import com.sun.javaws.Main;
import com.sun.javaws.jnl.ApplicationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.XMLFormat;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;

public final class SingleInstanceServiceImpl
  extends SingleInstanceImpl
  implements SingleInstanceService
{
  private static SingleInstanceServiceImpl _sharedInstance = null;
  
  public static synchronized SingleInstanceServiceImpl getInstance()
  {
    if (_sharedInstance == null) {
      _sharedInstance = new SingleInstanceServiceImpl();
    }
    return _sharedInstance;
  }
  
  public void addSingleInstanceListener(SingleInstanceListener paramSingleInstanceListener)
  {
    if (paramSingleInstanceListener == null) {
      return;
    }
    LaunchDesc localLaunchDesc = JNLPClassLoader.getInstance().getLaunchDesc();
    URL localURL = localLaunchDesc.getCanonicalHome();
    String str = localURL.toString();
    
    AccessController.doPrivileged(new PrivilegedAction()
    {
      private final String val$jnlpUrlString;
      private final LaunchDesc val$ld;
      
      public Object run()
      {
        if (SingleInstanceManager.isServerRunning(this.val$jnlpUrlString))
        {
          String[] arrayOfString = Globals.getApplicationArgs();
          if (arrayOfString != null) {
            this.val$ld.getApplicationDescriptor().setArguments(arrayOfString);
          }
          if (SingleInstanceManager.connectToServer(this.val$ld.toString())) {
            Main.systemExit(0);
          }
        }
        return null;
      }
    });
    super.addSingleInstanceListener(new TransferListener(paramSingleInstanceListener), str);
  }
  
  public void removeSingleInstanceListener(SingleInstanceListener paramSingleInstanceListener)
  {
    super.removeSingleInstanceListener(new TransferListener(paramSingleInstanceListener));
  }
  
  public boolean isSame(String paramString1, String paramString2)
  {
    LaunchDesc localLaunchDesc = null;
    try
    {
      localLaunchDesc = XMLFormat.parse(paramString1.getBytes());
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    if (localLaunchDesc != null)
    {
      URL localURL = localLaunchDesc.getCanonicalHome();
      Trace.println("GOT: " + localURL.toString(), TraceLevel.BASIC);
      if (paramString2.equals(localURL.toString())) {
        return true;
      }
    }
    return false;
  }
  
  public String[] getArguments(String paramString1, String paramString2)
  {
    LaunchDesc localLaunchDesc = null;
    try
    {
      localLaunchDesc = XMLFormat.parse(paramString1.getBytes());
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    if (localLaunchDesc != null) {
      return localLaunchDesc.getApplicationDescriptor().getArguments();
    }
    return new String[0];
  }
  
  private class TransferListener
    implements DeploySIListener
  {
    SingleInstanceListener _sil;
    
    public TransferListener(SingleInstanceListener paramSingleInstanceListener)
    {
      this._sil = paramSingleInstanceListener;
    }
    
    public void newActivation(String[] paramArrayOfString)
    {
      this._sil.newActivation(paramArrayOfString);
    }
    
    public Object getSingleInstanceListener()
    {
      return this._sil;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\SingleInstanceServiceImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */