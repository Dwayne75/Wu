package com.sun.jnlp;

import com.sun.javaws.LaunchDownload.DownloadProgress;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.cache.DiskCacheEntry;
import com.sun.javaws.cache.DownloadProtocol;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.ui.DownloadWindow;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.jnlp.DownloadService;
import javax.jnlp.DownloadServiceListener;
import javax.swing.JFrame;

public final class DownloadServiceImpl
  implements DownloadService
{
  private static DownloadServiceImpl _sharedInstance = null;
  private DownloadServiceListener _defaultProgress = null;
  
  public static synchronized DownloadServiceImpl getInstance()
  {
    initialize();
    return _sharedInstance;
  }
  
  public static synchronized void initialize()
  {
    if (_sharedInstance == null) {
      _sharedInstance = new DownloadServiceImpl();
    }
  }
  
  public DownloadServiceListener getDefaultProgressWindow()
  {
    if (this._defaultProgress == null) {
      this._defaultProgress = ((DownloadServiceListener)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          return new DownloadServiceImpl.DefaultProgressImpl(DownloadServiceImpl.this, new DownloadWindow(JNLPClassLoader.getInstance().getLaunchDesc(), false));
        }
      }));
    }
    return this._defaultProgress;
  }
  
  private class DefaultProgressImpl
    implements DownloadServiceListener
  {
    private DownloadWindow _dw = null;
    
    DefaultProgressImpl(DownloadWindow paramDownloadWindow)
    {
      AccessController.doPrivileged(new PrivilegedAction()
      {
        private final DownloadServiceImpl val$this$0;
        private final DownloadWindow val$dw;
        
        public Object run()
        {
          DownloadServiceImpl.DefaultProgressImpl.this._dw = this.val$dw;
          DownloadServiceImpl.DefaultProgressImpl.this._dw.buildIntroScreen();
          DownloadServiceImpl.DefaultProgressImpl.this._dw.showLoadingProgressScreen();
          return null;
        }
      });
    }
    
    public void progress(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
    {
      ensureVisible();
      if (paramLong1 == 0L) {
        this._dw.resetDownloadTimer();
      }
      this._dw.progress(paramURL, paramString, paramLong1, paramLong2, paramInt);
      if (paramInt >= 100) {
        hideFrame();
      }
      if (this._dw.isCanceled())
      {
        hideFrame();
        throw new RuntimeException("canceled by user");
      }
    }
    
    public void validating(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
    {
      ensureVisible();
      this._dw.validating(paramURL, paramString, paramLong1, paramLong2, paramInt);
      if ((paramLong1 >= paramLong2) && ((paramInt < 0) || (paramInt >= 99))) {
        hideFrame();
      }
    }
    
    public void upgradingArchive(URL paramURL, String paramString, int paramInt1, int paramInt2)
    {
      ensureVisible();
      this._dw.patching(paramURL, paramString, paramInt1, paramInt2);
      if (paramInt2 >= 100) {
        hideFrame();
      }
    }
    
    public void downloadFailed(URL paramURL, String paramString)
    {
      hideFrame();
    }
    
    private void ensureVisible()
    {
      if (!this._dw.getFrame().isVisible())
      {
        this._dw.getFrame().setVisible(true);
        this._dw.getFrame().toFront();
      }
    }
    
    private synchronized void hideFrame()
    {
      this._dw.resetCancled();
      this._dw.getFrame().hide();
    }
  }
  
  public boolean isResourceCached(URL paramURL, String paramString)
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      private final URL val$ref;
      private final String val$version;
      
      public Object run()
      {
        if ((DownloadProtocol.isInCache(this.val$ref, this.val$version, 0)) || (DownloadProtocol.isInCache(this.val$ref, this.val$version, 1))) {
          return Boolean.TRUE;
        }
        return Boolean.FALSE;
      }
    });
    return localBoolean.booleanValue();
  }
  
  public boolean isPartCached(String paramString)
  {
    return isPartCached(new String[] { paramString });
  }
  
  public boolean isPartCached(String[] paramArrayOfString)
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      private final String[] val$parts;
      
      public Object run()
      {
        LaunchDesc localLaunchDesc = JNLPClassLoader.getInstance().getLaunchDesc();
        ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
        if (localResourcesDesc == null) {
          return Boolean.FALSE;
        }
        JARDesc[] arrayOfJARDesc = localResourcesDesc.getPartJars(this.val$parts);
        return new Boolean(DownloadServiceImpl.this.isJARInCache(arrayOfJARDesc, true));
      }
    });
    return localBoolean.booleanValue();
  }
  
  public boolean isExtensionPartCached(URL paramURL, String paramString1, String paramString2)
  {
    return isExtensionPartCached(paramURL, paramString1, new String[] { paramString2 });
  }
  
  public boolean isExtensionPartCached(URL paramURL, String paramString, String[] paramArrayOfString)
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      private final URL val$ref;
      private final String val$version;
      private final String[] val$parts;
      
      public Object run()
      {
        LaunchDesc localLaunchDesc = JNLPClassLoader.getInstance().getLaunchDesc();
        ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
        if (localResourcesDesc == null) {
          return Boolean.FALSE;
        }
        JARDesc[] arrayOfJARDesc = localResourcesDesc.getExtensionPart(this.val$ref, this.val$version, this.val$parts);
        return new Boolean(DownloadServiceImpl.this.isJARInCache(arrayOfJARDesc, true));
      }
    });
    return localBoolean.booleanValue();
  }
  
  public void loadResource(URL paramURL, String paramString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException
  {
    if (isResourceCached(paramURL, paramString)) {
      return;
    }
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$ref;
        private final String val$version;
        private final DownloadServiceListener val$progress;
        
        public Object run()
          throws IOException
        {
          try
          {
            JNLPClassLoader.getInstance().downloadResource(this.val$ref, this.val$version, new DownloadServiceImpl.ProgressHelper(DownloadServiceImpl.this, this.val$progress), true);
          }
          catch (JNLPException localJNLPException)
          {
            throw new IOException(localJNLPException.getMessage());
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }
  
  public void loadPart(String paramString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException
  {
    loadPart(new String[] { paramString }, paramDownloadServiceListener);
  }
  
  public void loadPart(String[] paramArrayOfString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException
  {
    if (isPartCached(paramArrayOfString)) {
      return;
    }
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final String[] val$parts;
        private final DownloadServiceListener val$progress;
        
        public Object run()
          throws IOException
        {
          try
          {
            JNLPClassLoader.getInstance().downloadParts(this.val$parts, new DownloadServiceImpl.ProgressHelper(DownloadServiceImpl.this, this.val$progress), true);
          }
          catch (JNLPException localJNLPException)
          {
            throw new IOException(localJNLPException.getMessage());
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }
  
  public void loadExtensionPart(URL paramURL, String paramString1, String paramString2, DownloadServiceListener paramDownloadServiceListener)
    throws IOException
  {
    loadExtensionPart(paramURL, paramString1, new String[] { paramString2 }, paramDownloadServiceListener);
  }
  
  public void loadExtensionPart(URL paramURL, String paramString, String[] paramArrayOfString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$ref;
        private final String val$version;
        private final String[] val$parts;
        private final DownloadServiceListener val$progress;
        
        public Object run()
          throws IOException
        {
          try
          {
            JNLPClassLoader.getInstance().downloadExtensionParts(this.val$ref, this.val$version, this.val$parts, new DownloadServiceImpl.ProgressHelper(DownloadServiceImpl.this, this.val$progress), true);
          }
          catch (JNLPException localJNLPException)
          {
            throw new IOException(localJNLPException.getMessage());
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }
  
  public void removeResource(URL paramURL, String paramString)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$ref;
        private final String val$version;
        
        public Object run()
          throws IOException
        {
          LaunchDesc localLaunchDesc = JNLPClassLoader.getInstance().getLaunchDesc();
          ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
          if (localResourcesDesc == null) {
            return null;
          }
          JARDesc[] arrayOfJARDesc = localResourcesDesc.getResource(this.val$ref, this.val$version);
          DownloadServiceImpl.this.removeJARFromCache(arrayOfJARDesc);
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }
  
  public void removePart(String paramString)
    throws IOException
  {
    removePart(new String[] { paramString });
  }
  
  public void removePart(String[] paramArrayOfString)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final String[] val$parts;
        
        public Object run()
          throws IOException
        {
          LaunchDesc localLaunchDesc = JNLPClassLoader.getInstance().getLaunchDesc();
          ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
          if (localResourcesDesc == null) {
            return null;
          }
          JARDesc[] arrayOfJARDesc = localResourcesDesc.getPartJars(this.val$parts);
          DownloadServiceImpl.this.removeJARFromCache(arrayOfJARDesc);
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }
  
  public void removeExtensionPart(URL paramURL, String paramString1, String paramString2)
    throws IOException
  {
    removeExtensionPart(paramURL, paramString1, new String[] { paramString2 });
  }
  
  public void removeExtensionPart(URL paramURL, String paramString, String[] paramArrayOfString)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$ref;
        private final String val$version;
        private final String[] val$parts;
        
        public Object run()
          throws IOException
        {
          LaunchDesc localLaunchDesc = JNLPClassLoader.getInstance().getLaunchDesc();
          ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
          if (localResourcesDesc == null) {
            return null;
          }
          JARDesc[] arrayOfJARDesc = localResourcesDesc.getExtensionPart(this.val$ref, this.val$version, this.val$parts);
          DownloadServiceImpl.this.removeJARFromCache(arrayOfJARDesc);
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }
  
  private void removeJARFromCache(JARDesc[] paramArrayOfJARDesc)
    throws IOException
  {
    if (paramArrayOfJARDesc == null) {
      return;
    }
    if (paramArrayOfJARDesc.length == 0) {
      return;
    }
    DiskCacheEntry localDiskCacheEntry = null;
    for (int i = 0; i < paramArrayOfJARDesc.length; i++)
    {
      int j = paramArrayOfJARDesc[i].isNativeLib() ? 1 : 0;
      try
      {
        localDiskCacheEntry = DownloadProtocol.getResource(paramArrayOfJARDesc[i].getLocation(), paramArrayOfJARDesc[i].getVersion(), j, true, null);
      }
      catch (JNLPException localJNLPException)
      {
        throw new IOException(localJNLPException.getMessage());
      }
      if (localDiskCacheEntry != null) {
        Cache.removeEntry(localDiskCacheEntry);
      }
    }
  }
  
  private boolean isJARInCache(JARDesc[] paramArrayOfJARDesc, boolean paramBoolean)
  {
    if (paramArrayOfJARDesc == null) {
      return false;
    }
    if (paramArrayOfJARDesc.length == 0) {
      return false;
    }
    boolean bool = true;
    for (int i = 0; i < paramArrayOfJARDesc.length; i++) {
      if (paramArrayOfJARDesc[i].isNativeLib())
      {
        if (DownloadProtocol.isInCache(paramArrayOfJARDesc[i].getLocation(), paramArrayOfJARDesc[i].getVersion(), 1))
        {
          if (!paramBoolean) {
            return true;
          }
        }
        else {
          bool = false;
        }
      }
      else if (DownloadProtocol.isInCache(paramArrayOfJARDesc[i].getLocation(), paramArrayOfJARDesc[i].getVersion(), 0))
      {
        if (!paramBoolean) {
          return true;
        }
      }
      else {
        bool = false;
      }
    }
    return bool;
  }
  
  private class ProgressHelper
    implements LaunchDownload.DownloadProgress
  {
    DownloadServiceListener _dsp = null;
    
    public ProgressHelper(DownloadServiceListener paramDownloadServiceListener)
    {
      this._dsp = paramDownloadServiceListener;
      
      this._dsp.progress(null, null, 0L, 0L, -1);
    }
    
    public void extensionDownload(String paramString, int paramInt) {}
    
    public void jreDownload(String paramString, URL paramURL) {}
    
    public void progress(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
    {
      if (this._dsp != null) {
        this._dsp.progress(paramURL, paramString, paramLong1, paramLong2, paramInt);
      }
    }
    
    public void validating(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
    {
      if (this._dsp != null) {
        this._dsp.validating(paramURL, paramString, paramLong1, paramLong2, paramInt);
      }
    }
    
    public void patching(URL paramURL, String paramString, int paramInt1, int paramInt2)
    {
      if (this._dsp != null) {
        this._dsp.upgradingArchive(paramURL, paramString, paramInt1, paramInt2);
      }
    }
    
    public void downloadFailed(URL paramURL, String paramString)
    {
      if (this._dsp != null) {
        this._dsp.downloadFailed(paramURL, paramString);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\DownloadServiceImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */