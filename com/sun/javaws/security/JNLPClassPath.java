package com.sun.javaws.security;

import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.Globals;
import com.sun.javaws.cache.DiskCacheEntry;
import com.sun.javaws.cache.DownloadProtocol;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.jnl.ResourcesDesc.PackageInformation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JNLPClassPath
{
  private Stack _pendingJarDescs = new Stack();
  private ArrayList _loaders = new ArrayList();
  private Loader _appletLoader = null;
  private LaunchDesc _launchDesc = null;
  private HashMap _fileToUrls = new HashMap();
  
  public JNLPClassPath(LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    this._launchDesc = paramLaunchDesc;
    if (paramBoolean)
    {
      localObject = URLUtil.getBase(paramLaunchDesc.getCanonicalHome());
      Trace.println("Classpath: " + localObject, TraceLevel.BASIC);
      if ("file".equals(((URL)localObject).getProtocol())) {
        this._appletLoader = new FileDirectoryLoader((URL)localObject);
      } else {
        this._appletLoader = new URLDirectoryLoader((URL)localObject);
      }
    }
    Object localObject = paramLaunchDesc.getResources();
    if (localObject != null)
    {
      JARDesc[] arrayOfJARDesc = ((ResourcesDesc)localObject).getEagerOrAllJarDescs(true);
      for (int i = arrayOfJARDesc.length - 1; i >= 0; i--) {
        if (arrayOfJARDesc[i].isJavaFile())
        {
          Trace.println("Classpath: " + arrayOfJARDesc[i].getLocation() + ":" + arrayOfJARDesc[i].getVersion(), TraceLevel.BASIC);
          this._pendingJarDescs.add(arrayOfJARDesc[i]);
        }
      }
    }
  }
  
  public synchronized JARDesc getJarDescFromFileURL(URL paramURL)
  {
    return (JARDesc)this._fileToUrls.get(paramURL.toString());
  }
  
  private void loadAllResources()
  {
    try
    {
      JARDesc localJARDesc = getNextPendingJarDesc();
      while (localJARDesc != null)
      {
        createLoader(localJARDesc);
        localJARDesc = getNextPendingJarDesc();
      }
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    if (this._appletLoader != null) {
      synchronized (this._loaders)
      {
        this._loaders.add(this._appletLoader);
      }
    }
  }
  
  private synchronized JARDesc getNextPendingJarDesc()
  {
    return this._pendingJarDescs.isEmpty() ? null : (JARDesc)this._pendingJarDescs.pop();
  }
  
  private synchronized JARDesc getIfPendingJarDesc(JARDesc paramJARDesc)
  {
    if (this._pendingJarDescs.contains(paramJARDesc))
    {
      this._pendingJarDescs.remove(paramJARDesc);
      return paramJARDesc;
    }
    return null;
  }
  
  private Loader createLoader(JARDesc paramJARDesc)
    throws IOException
  {
    try
    {
      (Loader)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final JARDesc val$jd;
        
        public Object run()
          throws IOException
        {
          return JNLPClassPath.this.createLoaderHelper(this.val$jd);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Trace.println("Failed to create loader for: " + paramJARDesc + " (" + localPrivilegedActionException.getException() + ")", TraceLevel.BASIC);
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }
  
  private Loader createLoaderHelper(JARDesc paramJARDesc)
    throws IOException
  {
    URL localURL1 = paramJARDesc.getLocation();
    String str1 = paramJARDesc.getVersion();
    try
    {
      DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getResource(localURL1, str1, 0, true, null);
      if ((localDiskCacheEntry == null) || (!localDiskCacheEntry.getFile().exists())) {
        throw new IOException("Resource not found: " + paramJARDesc.getLocation() + ":" + paramJARDesc.getVersion());
      }
      String str2 = URLUtil.getEncodedPath(localDiskCacheEntry.getFile());
      URL localURL2 = new URL("file", "", str2);
      
      Trace.println("Creating loader for: " + localURL2, TraceLevel.BASIC);
      JarLoader localJarLoader = new JarLoader(localURL2);
      synchronized (this)
      {
        this._loaders.add(localJarLoader);
        
        this._fileToUrls.put(localURL2.toString(), paramJARDesc);
      }
      return localJarLoader;
    }
    catch (JNLPException localJNLPException)
    {
      Trace.println("Failed to download: " + localJNLPException + " (" + localJNLPException + ")", TraceLevel.BASIC);
      Trace.ignoredException(localJNLPException);
      throw new IOException(localJNLPException.getMessage());
    }
  }
  
  private Resource findNamedResource(String paramString, boolean paramBoolean)
    throws IOException
  {
    Resource localResource = findNamedResourceInLoaders(paramString, paramBoolean);
    if (localResource != null) {
      return localResource;
    }
    synchronized (this)
    {
      if (this._pendingJarDescs.isEmpty()) {
        return null;
      }
    }
    ??? = this._launchDesc.getResources().getPackageInformation(paramString);
    JARDesc localJARDesc;
    if (??? != null)
    {
      JARDesc[] arrayOfJARDesc = ((ResourcesDesc.PackageInformation)???).getLaunchDesc().getResources().getPart(((ResourcesDesc.PackageInformation)???).getPart());
      for (int i = 0; i < arrayOfJARDesc.length; i++)
      {
        localJARDesc = getIfPendingJarDesc(arrayOfJARDesc[i]);
        if (localJARDesc != null) {
          createLoader(localJARDesc);
        }
      }
      localResource = findNamedResourceInLoaders(paramString, paramBoolean);
      if (localResource != null) {
        return localResource;
      }
    }
    synchronized (this)
    {
      ListIterator localListIterator = this._pendingJarDescs.listIterator(this._pendingJarDescs.size());
      while (localListIterator.hasPrevious())
      {
        localJARDesc = (JARDesc)localListIterator.previous();
        if (!this._launchDesc.getResources().isPackagePart(localJARDesc.getPartName()))
        {
          localListIterator.remove();
          Loader localLoader = createLoader(localJARDesc);
          localResource = localLoader.getResource(paramString, paramBoolean);
          if (localResource != null) {
            return localResource;
          }
        }
      }
    }
    if (this._appletLoader != null) {
      localResource = this._appletLoader.getResource(paramString, paramBoolean);
    }
    return localResource;
  }
  
  private Resource findNamedResourceInLoaders(String paramString, boolean paramBoolean)
    throws IOException
  {
    Object localObject1 = 0;
    synchronized (this)
    {
      localObject1 = this._loaders.size();
    }
    for (??? = 0; ??? < localObject1; ???++)
    {
      Loader localLoader = null;
      synchronized (this)
      {
        localLoader = (Loader)this._loaders.get(???);
      }
      ??? = localLoader.getResource(paramString, paramBoolean);
      if (??? != null) {
        return (Resource)???;
      }
    }
    return null;
  }
  
  public Resource getResource(String paramString, boolean paramBoolean)
  {
    Trace.println("getResource: " + paramString + " (check: " + paramBoolean + ")", TraceLevel.BASIC);
    try
    {
      return findNamedResource(paramString, paramBoolean);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    return null;
  }
  
  public Resource getResource(String paramString)
  {
    return getResource(paramString, true);
  }
  
  public Enumeration getResources(String paramString, boolean paramBoolean)
  {
    loadAllResources();
    Object localObject1;
    synchronized (this)
    {
      localObject1 = this._loaders.size();
    }
    ??? = localObject1;
    
    new Enumeration()
    {
      private int index;
      private Resource res;
      private final int val$size;
      private final String val$name;
      private final boolean val$check;
      
      private boolean next()
      {
        if (this.res != null) {
          return true;
        }
        while (this.index < this.val$size)
        {
          JNLPClassPath.Loader localLoader = (JNLPClassPath.Loader)JNLPClassPath.this._loaders.get(this.index++);
          this.res = localLoader.getResource(this.val$name, this.val$check);
          if (this.res != null) {
            return true;
          }
        }
        return false;
      }
      
      public boolean hasMoreElements()
      {
        return next();
      }
      
      public Object nextElement()
      {
        if (!next()) {
          throw new NoSuchElementException();
        }
        Resource localResource = this.res;
        this.res = null;
        return localResource;
      }
    };
  }
  
  public Enumeration getResources(String paramString)
  {
    return getResources(paramString, true);
  }
  
  public URL checkURL(URL paramURL)
  {
    try
    {
      check(paramURL);
    }
    catch (Exception localException)
    {
      return null;
    }
    return paramURL;
  }
  
  private static void check(URL paramURL)
    throws IOException
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      Permission localPermission = paramURL.openConnection().getPermission();
      if (localPermission != null) {
        localSecurityManager.checkPermission(localPermission);
      }
    }
  }
  
  private static abstract class Loader
  {
    private final URL base;
    
    Loader(URL paramURL)
    {
      this.base = paramURL;
    }
    
    Resource getResource(String paramString)
    {
      return getResource(paramString, true);
    }
    
    abstract Resource getResource(String paramString, boolean paramBoolean);
    
    URL getBaseURL()
    {
      return this.base;
    }
  }
  
  private static class URLDirectoryLoader
    extends JNLPClassPath.Loader
  {
    URLDirectoryLoader(URL paramURL)
    {
      super();
    }
    
    Resource getResource(String paramString, boolean paramBoolean)
    {
      URL localURL;
      try
      {
        localURL = new URL(getBaseURL(), paramString);
      }
      catch (MalformedURLException localMalformedURLException)
      {
        throw new IllegalArgumentException("name");
      }
      URLConnection localURLConnection;
      try
      {
        if (paramBoolean) {
          JNLPClassPath.check(localURL);
        }
        localURLConnection = localURL.openConnection();
        Object localObject;
        if ((localURLConnection instanceof HttpURLConnection))
        {
          localObject = (HttpURLConnection)localURLConnection;
          int i = ((HttpURLConnection)localObject).getResponseCode();
          ((HttpURLConnection)localObject).disconnect();
          if (i >= 400) {
            return null;
          }
        }
        else
        {
          localObject = localURL.openStream();
          ((InputStream)localObject).close();
        }
      }
      catch (Exception localException)
      {
        return null;
      }
      new Resource()
      {
        private final String val$name;
        private final URL val$url;
        private final URLConnection val$uc;
        
        public String getName()
        {
          return this.val$name;
        }
        
        public URL getURL()
        {
          return this.val$url;
        }
        
        public URL getCodeSourceURL()
        {
          return JNLPClassPath.URLDirectoryLoader.this.getBaseURL();
        }
        
        public InputStream getInputStream()
          throws IOException
        {
          return this.val$uc.getInputStream();
        }
        
        public int getContentLength()
          throws IOException
        {
          return this.val$uc.getContentLength();
        }
      };
    }
  }
  
  private static class JarLoader
    extends JNLPClassPath.Loader
  {
    private JarFile jar;
    private URL csu;
    
    JarLoader(URL paramURL)
      throws IOException
    {
      super();
      this.jar = getJarFile(paramURL);
      this.csu = paramURL;
    }
    
    private JarFile getJarFile(URL paramURL)
      throws IOException
    {
      if ("file".equals(paramURL.getProtocol()))
      {
        String str = URLUtil.getPathFromURL(paramURL);
        File localFile = new File(str);
        if (!localFile.exists()) {
          throw new FileNotFoundException(str);
        }
        return new JarFile(str);
      }
      throw new IOException("Must be file URL");
    }
    
    Resource getResource(String paramString, boolean paramBoolean)
    {
      JarEntry localJarEntry = this.jar.getJarEntry(paramString);
      if (localJarEntry != null)
      {
        URL localURL;
        try
        {
          localURL = new URL(getBaseURL(), paramString);
          if (paramBoolean) {
            JNLPClassPath.check(localURL);
          }
        }
        catch (MalformedURLException localMalformedURLException)
        {
          Trace.ignoredException(localMalformedURLException);
          return null;
        }
        catch (IOException localIOException)
        {
          Trace.ignoredException(localIOException);
          return null;
        }
        catch (AccessControlException localAccessControlException)
        {
          Trace.ignoredException(localAccessControlException);
          return null;
        }
        new Resource()
        {
          private final String val$name;
          private final URL val$url;
          private final JarEntry val$entry;
          
          public String getName()
          {
            return this.val$name;
          }
          
          public URL getURL()
          {
            return this.val$url;
          }
          
          public URL getCodeSourceURL()
          {
            return JNLPClassPath.JarLoader.this.csu;
          }
          
          public InputStream getInputStream()
            throws IOException
          {
            return JNLPClassPath.JarLoader.this.jar.getInputStream(this.val$entry);
          }
          
          public int getContentLength()
          {
            return (int)this.val$entry.getSize();
          }
          
          public Manifest getManifest()
            throws IOException
          {
            return JNLPClassPath.JarLoader.this.jar.getManifest();
          }
          
          public Certificate[] getCertificates()
          {
            return this.val$entry.getCertificates();
          }
          
          public CodeSigner[] getCodeSigners()
          {
            if (Globals.isJavaVersionAtLeast15()) {
              return this.val$entry.getCodeSigners();
            }
            return null;
          }
        };
      }
      return null;
    }
  }
  
  private static class FileDirectoryLoader
    extends JNLPClassPath.Loader
  {
    private File dir;
    
    FileDirectoryLoader(URL paramURL)
    {
      super();
      if (!"file".equals(paramURL.getProtocol())) {
        throw new IllegalArgumentException("must be FILE URL");
      }
      this.dir = new File(URLUtil.getPathFromURL(paramURL));
    }
    
    Resource getResource(String paramString, boolean paramBoolean)
    {
      try
      {
        URL localURL = new URL(getBaseURL(), paramString);
        if (!localURL.getFile().startsWith(getBaseURL().getFile())) {
          return null;
        }
        if (paramBoolean) {
          JNLPClassPath.check(localURL);
        }
        File localFile = new File(this.dir, paramString.replace('/', File.separatorChar));
        if (localFile.exists()) {
          new Resource()
          {
            private final String val$name;
            private final URL val$url;
            private final File val$file;
            
            public String getName()
            {
              return this.val$name;
            }
            
            public URL getURL()
            {
              return this.val$url;
            }
            
            public URL getCodeSourceURL()
            {
              return JNLPClassPath.FileDirectoryLoader.this.getBaseURL();
            }
            
            public InputStream getInputStream()
              throws IOException
            {
              return new FileInputStream(this.val$file);
            }
            
            public int getContentLength()
              throws IOException
            {
              return (int)this.val$file.length();
            }
          };
        }
      }
      catch (Exception localException)
      {
        return null;
      }
      return null;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\security\JNLPClassPath.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */