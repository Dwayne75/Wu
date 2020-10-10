package com.sun.jnlp;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.LaunchErrorDialog;
import com.sun.javaws.LaunchSelection;
import com.sun.javaws.LocalApplicationProperties;
import com.sun.javaws.Main;
import com.sun.javaws.ui.DownloadWindow;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Date;
import javax.jnlp.ExtensionInstallerService;
import javax.swing.JFrame;

public final class ExtensionInstallerServiceImpl
  implements ExtensionInstallerService
{
  private LocalApplicationProperties _lap;
  private DownloadWindow _window;
  private String _target;
  private String _installPath;
  private boolean _failedJREInstall = false;
  static ExtensionInstallerServiceImpl _sharedInstance = null;
  
  private ExtensionInstallerServiceImpl(String paramString, LocalApplicationProperties paramLocalApplicationProperties, DownloadWindow paramDownloadWindow)
  {
    this._lap = paramLocalApplicationProperties;
    this._window = paramDownloadWindow;
    this._installPath = paramString;
  }
  
  public static synchronized ExtensionInstallerServiceImpl getInstance()
  {
    return _sharedInstance;
  }
  
  public static synchronized void initialize(String paramString, LocalApplicationProperties paramLocalApplicationProperties, DownloadWindow paramDownloadWindow)
  {
    if (_sharedInstance == null) {
      _sharedInstance = new ExtensionInstallerServiceImpl(paramString, paramLocalApplicationProperties, paramDownloadWindow);
    }
  }
  
  public String getInstallPath()
  {
    return this._installPath;
  }
  
  public String getExtensionVersion()
  {
    return this._lap.getVersionId();
  }
  
  public URL getExtensionLocation()
  {
    return this._lap.getLocation();
  }
  
  public String getInstalledJRE(URL paramURL, String paramString)
  {
    JREInfo localJREInfo = LaunchSelection.selectJRE(paramURL, paramString);
    return localJREInfo != null ? localJREInfo.getPath() : null;
  }
  
  public void setHeading(String paramString)
  {
    this._window.setStatus(paramString);
  }
  
  public void setStatus(String paramString)
  {
    this._window.setProgressText(paramString);
  }
  
  public void updateProgress(int paramInt)
  {
    this._window.setProgressBarValue(paramInt);
  }
  
  public void hideProgressBar()
  {
    this._window.setProgressBarVisible(false);
  }
  
  public void hideStatusWindow()
  {
    this._window.getFrame().setVisible(false);
  }
  
  public void setJREInfo(String paramString1, String paramString2)
  {
    int i = JNLPClassLoader.getInstance().getDefaultSecurityModel();
    if ((i != 1) && (i != 2)) {
      throw new SecurityException("Unsigned extension installer attempting to call setJREInfo.");
    }
    Trace.println("setJREInfo: " + paramString2, TraceLevel.EXTENSIONS);
    if ((paramString2 != null) && (new File(paramString2).exists()))
    {
      JREInfo.addJRE(new JREInfo(paramString1, getExtensionVersion(), getExtensionLocation().toString(), paramString2, Config.getOSName(), Config.getOSArch(), true, false));
    }
    else
    {
      Trace.println("jre install failed: jrePath invalid", TraceLevel.EXTENSIONS);
      
      this._failedJREInstall = true;
    }
  }
  
  public void setNativeLibraryInfo(String paramString)
  {
    Trace.println("setNativeLibInfo: " + paramString, TraceLevel.EXTENSIONS);
    
    this._lap.setNativeLibDirectory(paramString);
  }
  
  public void installFailed()
  {
    Trace.println("installFailed", TraceLevel.EXTENSIONS);
    
    Main.systemExit(1);
  }
  
  public void installSucceeded(boolean paramBoolean)
  {
    if (this._failedJREInstall) {
      return;
    }
    Trace.println("installSucceded", TraceLevel.EXTENSIONS);
    
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        Config.store();
        return null;
      }
    });
    this._lap.setInstallDirectory(this._installPath);
    this._lap.setLastAccessed(new Date());
    if (paramBoolean) {
      this._lap.setRebootNeeded(true);
    } else {
      this._lap.setLocallyInstalled(true);
    }
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Object run()
          throws IOException
        {
          ExtensionInstallerServiceImpl.this._lap.store();
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      if ((localPrivilegedActionException.getException() instanceof IOException)) {
        LaunchErrorDialog.show(this._window.getFrame(), (IOException)localPrivilegedActionException.getException(), false);
      } else {
        Trace.ignoredException(localPrivilegedActionException.getException());
      }
    }
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        Main.systemExit(0);
        return null;
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\ExtensionInstallerServiceImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */