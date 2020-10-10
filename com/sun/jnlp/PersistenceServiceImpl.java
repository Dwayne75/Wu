package com.sun.jnlp;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.cache.DiskCacheEntry;
import com.sun.javaws.jnl.LaunchDesc;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Vector;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;

public final class PersistenceServiceImpl
  implements PersistenceService
{
  private long _globalLimit = -1L;
  private long _appLimit = -1L;
  private long _size = -1L;
  private static PersistenceServiceImpl _sharedInstance = null;
  private final SmartSecurityDialog _securityDialog = new SmartSecurityDialog();
  
  public static synchronized PersistenceServiceImpl getInstance()
  {
    initialize();
    return _sharedInstance;
  }
  
  public static synchronized void initialize()
  {
    if (_sharedInstance == null) {
      _sharedInstance = new PersistenceServiceImpl();
    }
    if (_sharedInstance != null) {
      _sharedInstance._appLimit = (Config.getIntProperty("deployment.javaws.muffin.max") * 1024L);
    }
  }
  
  long getLength(URL paramURL)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    return Cache.getMuffinSize(paramURL);
  }
  
  long getMaxLength(URL paramURL)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    
    Long localLong = null;
    try
    {
      localLong = (Long)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$url;
        
        public Object run()
          throws IOException
        {
          long[] arrayOfLong = Cache.getMuffinAttributes(this.val$url);
          if (arrayOfLong == null) {
            return new Long(-1L);
          }
          return new Long(arrayOfLong[1]);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
    return localLong.longValue();
  }
  
  long setMaxLength(URL paramURL, long paramLong)
    throws MalformedURLException, IOException
  {
    long l1 = 0L;
    checkAccess(paramURL);
    if ((l1 = checkSetMaxSize(paramURL, paramLong)) < 0L) {
      return -1L;
    }
    long l2 = l1;
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$url;
        private final long val$f_newmaxsize;
        
        public Object run()
          throws MalformedURLException, IOException
        {
          Cache.putMuffinAttributes(this.val$url, PersistenceServiceImpl.this.getTag(this.val$url), this.val$f_newmaxsize);
          
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException)) {
        throw ((IOException)localException);
      }
      if ((localException instanceof MalformedURLException)) {
        throw ((MalformedURLException)localException);
      }
    }
    return l1;
  }
  
  private long checkSetMaxSize(URL paramURL, long paramLong)
    throws IOException
  {
    URL[] arrayOfURL = null;
    try
    {
      arrayOfURL = (URL[])AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$url;
        
        public Object run()
          throws IOException
        {
          return Cache.getAccessibleMuffins(this.val$url);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException1)
    {
      throw ((IOException)localPrivilegedActionException1.getException());
    }
    long l1 = 0L;
    if (arrayOfURL != null) {
      for (int i = 0; i < arrayOfURL.length; i++) {
        if (arrayOfURL[i] != null)
        {
          URL localURL = arrayOfURL[i];
          Long localLong = null;
          try
          {
            localLong = (Long)AccessController.doPrivileged(new PrivilegedExceptionAction()
            {
              private final URL val$friendMuffin;
              
              public Object run()
                throws IOException
              {
                return new Long(Cache.getMuffinSize(this.val$friendMuffin));
              }
            });
          }
          catch (PrivilegedActionException localPrivilegedActionException2)
          {
            throw ((IOException)localPrivilegedActionException2.getException());
          }
          l1 += localLong.longValue();
        }
      }
    }
    long l2 = paramLong + l1;
    if (l2 > this._appLimit) {
      return reconcileMaxSize(paramLong, l1, this._appLimit);
    }
    return paramLong;
  }
  
  private long reconcileMaxSize(long paramLong1, long paramLong2, long paramLong3)
  {
    long l = paramLong1 + paramLong2;
    
    boolean bool = CheckServicePermission.hasFileAccessPermissions();
    if ((bool) || (askUser(l, paramLong3)))
    {
      this._appLimit = l;
      return paramLong1;
    }
    return paramLong3 - paramLong2;
  }
  
  private URL[] getAccessibleMuffins(URL paramURL)
    throws IOException
  {
    return Cache.getAccessibleMuffins(paramURL);
  }
  
  public long create(URL paramURL, long paramLong)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    Long localLong = null;
    
    long l1 = -1L;
    if ((l1 = checkSetMaxSize(paramURL, paramLong)) < 0L) {
      return -1L;
    }
    long l2 = l1;
    try
    {
      localLong = (Long)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$url;
        private final long val$pass_newmaxsize;
        
        public Object run()
          throws MalformedURLException, IOException
        {
          File localFile = Cache.getTempCacheFile(this.val$url, null);
          if (localFile == null) {
            return new Long(-1L);
          }
          Cache.insertMuffinEntry(this.val$url, localFile, 0, this.val$pass_newmaxsize);
          
          return new Long(this.val$pass_newmaxsize);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException)) {
        throw ((IOException)localException);
      }
      if ((localException instanceof MalformedURLException)) {
        throw ((MalformedURLException)localException);
      }
    }
    return localLong.longValue();
  }
  
  public FileContents get(URL paramURL)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    File localFile = Cache.getMuffinFileForURL(paramURL);
    if (localFile == null) {
      throw new FileNotFoundException(paramURL.toString());
    }
    return new FileContentsImpl(localFile, this, paramURL, getMaxLength(paramURL));
  }
  
  public void delete(URL paramURL)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$url;
        
        public Object run()
          throws MalformedURLException, IOException
        {
          DiskCacheEntry localDiskCacheEntry = Cache.getMuffinEntry('P', this.val$url);
          if (localDiskCacheEntry == null) {
            throw new FileNotFoundException(this.val$url.toString());
          }
          Cache.removeMuffinEntry(localDiskCacheEntry);
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException)) {
        throw ((IOException)localException);
      }
      if ((localException instanceof MalformedURLException)) {
        throw ((MalformedURLException)localException);
      }
    }
  }
  
  public String[] getNames(URL paramURL)
    throws MalformedURLException, IOException
  {
    String[] arrayOfString = null;
    URL localURL = URLUtil.asPathURL(paramURL);
    checkAccess(localURL);
    try
    {
      arrayOfString = (String[])AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$pathUrl;
        
        public Object run()
          throws MalformedURLException, IOException
        {
          File localFile1 = Cache.getMuffinFileForURL(this.val$pathUrl);
          if (!localFile1.isDirectory()) {
            localFile1 = localFile1.getParentFile();
          }
          File[] arrayOfFile = localFile1.listFiles();
          Vector localVector = new Vector();
          for (int i = 0; i < arrayOfFile.length; i++) {
            if (Cache.isMainMuffinFile(arrayOfFile[i]))
            {
              DiskCacheEntry localDiskCacheEntry = Cache.getMuffinCacheEntryFromFile(arrayOfFile[i]);
              
              URL localURL = localDiskCacheEntry.getLocation();
              File localFile2 = new File(localURL.getFile());
              localVector.addElement(localFile2.getName());
            }
          }
          return (String[])localVector.toArray(new String[0]);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException)) {
        throw ((IOException)localException);
      }
      if ((localException instanceof MalformedURLException)) {
        throw ((MalformedURLException)localException);
      }
    }
    return arrayOfString;
  }
  
  public int getTag(URL paramURL)
    throws MalformedURLException, IOException
  {
    Integer localInteger = null;
    checkAccess(paramURL);
    try
    {
      localInteger = (Integer)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$url;
        
        public Object run()
          throws MalformedURLException, IOException
        {
          long[] arrayOfLong = Cache.getMuffinAttributes(this.val$url);
          if (arrayOfLong == null) {
            throw new MalformedURLException();
          }
          return new Integer((int)arrayOfLong[0]);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException)) {
        throw ((IOException)localException);
      }
      if ((localException instanceof MalformedURLException)) {
        throw ((MalformedURLException)localException);
      }
    }
    return localInteger.intValue();
  }
  
  public void setTag(URL paramURL, int paramInt)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final URL val$url;
        private final int val$tag;
        
        public Object run()
          throws MalformedURLException, IOException
        {
          Cache.putMuffinAttributes(this.val$url, this.val$tag, PersistenceServiceImpl.this.getMaxLength(this.val$url));
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException)) {
        throw ((IOException)localException);
      }
      if ((localException instanceof MalformedURLException)) {
        throw ((MalformedURLException)localException);
      }
    }
  }
  
  private boolean askUser(long paramLong1, long paramLong2)
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      private final long val$requested;
      private final long val$currentLimit;
      
      public Object run()
      {
        String str = ResourceManager.getString("APIImpl.persistence.message", new Long(this.val$requested), new Long(this.val$currentLimit));
        
        boolean bool = PersistenceServiceImpl.this._securityDialog.showDialog(str);
        if (bool)
        {
          long l = Math.min(2147483647L, (this.val$requested + 1023L) / 1024L);
          
          Config.setIntProperty("deployment.javaws.muffin.max", (int)l);
          
          Config.storeIfDirty();
        }
        return new Boolean(bool);
      }
    });
    return localBoolean.booleanValue();
  }
  
  private void checkAccess(URL paramURL)
    throws MalformedURLException
  {
    LaunchDesc localLaunchDesc = JNLPClassLoader.getInstance().getLaunchDesc();
    if (localLaunchDesc != null)
    {
      URL localURL = localLaunchDesc.getCodebase();
      if (localURL != null)
      {
        if ((paramURL == null) || (!localURL.getHost().equals(paramURL.getHost()))) {
          throwAccessDenied(paramURL);
        }
        String str = paramURL.getFile();
        if (str == null) {
          throwAccessDenied(paramURL);
        }
        int i = str.lastIndexOf('/');
        if (i == -1) {
          return;
        }
        if (!localURL.getFile().startsWith(str.substring(0, i + 1))) {
          throwAccessDenied(paramURL);
        }
      }
    }
  }
  
  private void throwAccessDenied(URL paramURL)
    throws MalformedURLException
  {
    throw new MalformedURLException(ResourceManager.getString("APIImpl.persistence.accessdenied", paramURL.toString()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\PersistenceServiceImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */