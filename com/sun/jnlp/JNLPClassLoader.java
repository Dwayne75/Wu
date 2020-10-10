package com.sun.jnlp;

import com.sun.deploy.config.Config;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.Globals;
import com.sun.javaws.LaunchDownload;
import com.sun.javaws.LaunchDownload.DownloadProgress;
import com.sun.javaws.Main;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.security.AppPolicy;
import com.sun.javaws.security.JNLPClassPath;
import com.sun.javaws.security.Resource;
import java.awt.AWTPermission;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import sun.awt.AppContext;

public final class JNLPClassLoader
  extends SecureClassLoader
{
  private static JNLPClassLoader _instance = null;
  private LaunchDesc _launchDesc = null;
  private JNLPClassPath _jcp = null;
  private AppPolicy _appPolicy;
  private AccessControlContext _acc = null;
  private boolean _initialized = false;
  
  public JNLPClassLoader(ClassLoader paramClassLoader)
  {
    super(paramClassLoader);
    
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null) {
      localSecurityManager.checkCreateClassLoader();
    }
  }
  
  private void initialize(LaunchDesc paramLaunchDesc, boolean paramBoolean, AppPolicy paramAppPolicy)
  {
    this._launchDesc = paramLaunchDesc;
    this._jcp = new JNLPClassPath(paramLaunchDesc, paramBoolean);
    this._acc = AccessController.getContext();
    this._appPolicy = paramAppPolicy;
    this._initialized = true;
  }
  
  public static synchronized JNLPClassLoader createClassLoader()
  {
    if (_instance == null)
    {
      ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
      if ((localClassLoader instanceof JNLPClassLoader)) {
        _instance = (JNLPClassLoader)localClassLoader;
      } else {
        _instance = new JNLPClassLoader(localClassLoader);
      }
    }
    return _instance;
  }
  
  public static synchronized JNLPClassLoader createClassLoader(LaunchDesc paramLaunchDesc, AppPolicy paramAppPolicy)
  {
    JNLPClassLoader localJNLPClassLoader = createClassLoader();
    if (!localJNLPClassLoader._initialized) {
      localJNLPClassLoader.initialize(paramLaunchDesc, paramLaunchDesc.isApplet(), paramAppPolicy);
    }
    return localJNLPClassLoader;
  }
  
  public static synchronized JNLPClassLoader getInstance()
  {
    return _instance;
  }
  
  public LaunchDesc getLaunchDesc()
  {
    return this._launchDesc;
  }
  
  public void downloadResource(URL paramURL, String paramString, LaunchDownload.DownloadProgress paramDownloadProgress, boolean paramBoolean)
    throws JNLPException, IOException
  {
    LaunchDownload.downloadResource(this._launchDesc, paramURL, paramString, paramDownloadProgress, paramBoolean);
  }
  
  public void downloadParts(String[] paramArrayOfString, LaunchDownload.DownloadProgress paramDownloadProgress, boolean paramBoolean)
    throws JNLPException, IOException
  {
    LaunchDownload.downloadParts(this._launchDesc, paramArrayOfString, paramDownloadProgress, paramBoolean);
  }
  
  public void downloadExtensionParts(URL paramURL, String paramString, String[] paramArrayOfString, LaunchDownload.DownloadProgress paramDownloadProgress, boolean paramBoolean)
    throws JNLPException, IOException
  {
    LaunchDownload.downloadExtensionPart(this._launchDesc, paramURL, paramString, paramArrayOfString, paramDownloadProgress, paramBoolean);
  }
  
  public void downloadEager(LaunchDownload.DownloadProgress paramDownloadProgress, boolean paramBoolean)
    throws JNLPException, IOException
  {
    LaunchDownload.downloadEagerorAll(this._launchDesc, false, paramDownloadProgress, paramBoolean);
  }
  
  public JARDesc getJarDescFromFileURL(URL paramURL)
  {
    return this._jcp.getJarDescFromFileURL(paramURL);
  }
  
  public int getDefaultSecurityModel()
  {
    return this._launchDesc.getSecurityModel();
  }
  
  public URL getResource(String paramString)
  {
    URL localURL = null;
    for (int i = 0; (localURL == null) && (i < 3); i++)
    {
      Trace.println("Looking up resource: " + paramString + " (attempt: " + i + ")", TraceLevel.BASIC);
      
      localURL = super.getResource(paramString);
    }
    return localURL;
  }
  
  public String findLibrary(String paramString)
  {
    if (!this._initialized) {
      return super.findLibrary(paramString);
    }
    paramString = Config.getInstance().getLibraryPrefix() + paramString + Config.getInstance().getLibrarySufix();
    
    Trace.println("Looking up native library: " + paramString, TraceLevel.BASIC);
    
    File[] arrayOfFile = LaunchDownload.getNativeDirectories(this._launchDesc);
    for (int i = 0; i < arrayOfFile.length; i++)
    {
      File localFile = new File(arrayOfFile[i], paramString);
      if (localFile.exists())
      {
        Trace.println("Native library found: " + localFile.getAbsolutePath(), TraceLevel.BASIC);
        
        return localFile.getAbsolutePath();
      }
    }
    Trace.println("Native library not found", TraceLevel.BASIC);
    
    return super.findLibrary(paramString);
  }
  
  protected Class findClass(String paramString)
    throws ClassNotFoundException
  {
    if (!this._initialized) {
      return super.findClass(paramString);
    }
    Trace.println("Loading class " + paramString, TraceLevel.BASIC);
    try
    {
      (Class)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final String val$name;
        
        public Object run()
          throws ClassNotFoundException
        {
          String str = this.val$name.replace('.', '/').concat(".class");
          Resource localResource = JNLPClassLoader.this._jcp.getResource(str, false);
          if (localResource != null) {
            try
            {
              return JNLPClassLoader.this.defineClass(this.val$name, localResource);
            }
            catch (IOException localIOException)
            {
              throw new ClassNotFoundException(this.val$name, localIOException);
            }
          }
          throw new ClassNotFoundException(this.val$name);
        }
      }, this._acc);
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((ClassNotFoundException)localPrivilegedActionException.getException());
    }
  }
  
  private Class defineClass(String paramString, Resource paramResource)
    throws IOException
  {
    int i = paramString.lastIndexOf('.');
    URL localURL = paramResource.getCodeSourceURL();
    Object localObject2;
    if (i != -1)
    {
      localObject1 = paramString.substring(0, i);
      
      localObject2 = getPackage((String)localObject1);
      Manifest localManifest = paramResource.getManifest();
      if (localObject2 != null)
      {
        boolean bool;
        if (((Package)localObject2).isSealed()) {
          bool = ((Package)localObject2).isSealed(localURL);
        } else {
          bool = (localManifest == null) || (!isSealed((String)localObject1, localManifest));
        }
        if (!bool) {
          throw new SecurityException("sealing violation");
        }
      }
      else if (localManifest != null)
      {
        definePackage((String)localObject1, localManifest, localURL);
      }
      else
      {
        definePackage((String)localObject1, null, null, null, null, null, null, null);
      }
    }
    Object localObject1 = paramResource.getBytes();
    if (Globals.isJavaVersionAtLeast15()) {
      localObject2 = new CodeSource(localURL, paramResource.getCodeSigners());
    } else {
      localObject2 = new CodeSource(localURL, paramResource.getCertificates());
    }
    return defineClass(paramString, (byte[])localObject1, 0, localObject1.length, (CodeSource)localObject2);
  }
  
  protected Package definePackage(String paramString, Manifest paramManifest, URL paramURL)
    throws IllegalArgumentException
  {
    String str1 = paramString.replace('.', '/').concat("/");
    String str2 = null;String str3 = null;String str4 = null;
    String str5 = null;String str6 = null;String str7 = null;
    String str8 = null;
    URL localURL = null;
    
    Attributes localAttributes = paramManifest.getAttributes(str1);
    if (localAttributes != null)
    {
      str2 = localAttributes.getValue(Attributes.Name.SPECIFICATION_TITLE);
      str3 = localAttributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
      str4 = localAttributes.getValue(Attributes.Name.SPECIFICATION_VENDOR);
      str5 = localAttributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
      str6 = localAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
      str7 = localAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
      str8 = localAttributes.getValue(Attributes.Name.SEALED);
    }
    localAttributes = paramManifest.getMainAttributes();
    if (localAttributes != null)
    {
      if (str2 == null) {
        str2 = localAttributes.getValue(Attributes.Name.SPECIFICATION_TITLE);
      }
      if (str3 == null) {
        str3 = localAttributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
      }
      if (str4 == null) {
        str4 = localAttributes.getValue(Attributes.Name.SPECIFICATION_VENDOR);
      }
      if (str5 == null) {
        str5 = localAttributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
      }
      if (str6 == null) {
        str6 = localAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
      }
      if (str7 == null) {
        str7 = localAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
      }
      if (str8 == null) {
        str8 = localAttributes.getValue(Attributes.Name.SEALED);
      }
    }
    if ("true".equalsIgnoreCase(str8)) {
      localURL = paramURL;
    }
    return definePackage(paramString, str2, str3, str4, str5, str6, str7, localURL);
  }
  
  private boolean isSealed(String paramString, Manifest paramManifest)
  {
    String str1 = paramString.replace('.', '/').concat("/");
    Attributes localAttributes = paramManifest.getAttributes(str1);
    String str2 = null;
    if (localAttributes != null) {
      str2 = localAttributes.getValue(Attributes.Name.SEALED);
    }
    if ((str2 == null) && 
      ((localAttributes = paramManifest.getMainAttributes()) != null)) {
      str2 = localAttributes.getValue(Attributes.Name.SEALED);
    }
    return "true".equalsIgnoreCase(str2);
  }
  
  public URL findResource(String paramString)
  {
    if (!this._initialized) {
      return super.findResource(paramString);
    }
    Resource localResource = (Resource)AccessController.doPrivileged(new PrivilegedAction()
    {
      private final String val$name;
      
      public Object run()
      {
        return JNLPClassLoader.this._jcp.getResource(this.val$name, true);
      }
    }, this._acc);
    
    return localResource != null ? this._jcp.checkURL(localResource.getURL()) : null;
  }
  
  public Enumeration findResources(String paramString)
    throws IOException
  {
    if (!this._initialized) {
      return super.findResources(paramString);
    }
    Enumeration localEnumeration = (Enumeration)AccessController.doPrivileged(new PrivilegedAction()
    {
      private final String val$name;
      
      public Object run()
      {
        return JNLPClassLoader.this._jcp.getResources(this.val$name, true);
      }
    }, this._acc);
    
    new Enumeration()
    {
      private URL res;
      private final Enumeration val$e;
      
      public Object nextElement()
      {
        if (this.res == null) {
          throw new NoSuchElementException();
        }
        URL localURL = this.res;
        this.res = null;
        return localURL;
      }
      
      public boolean hasMoreElements()
      {
        if (Thread.currentThread().getThreadGroup() == Main.getSecurityThreadGroup()) {
          return false;
        }
        if (this.res != null) {
          return true;
        }
        do
        {
          Resource localResource = (Resource)AccessController.doPrivileged(new PrivilegedAction()
          {
            public Object run()
            {
              if (!JNLPClassLoader.4.this.val$e.hasMoreElements()) {
                return null;
              }
              return JNLPClassLoader.4.this.val$e.nextElement();
            }
          }, JNLPClassLoader.this._acc);
          if (localResource == null) {
            break;
          }
          this.res = JNLPClassLoader.this._jcp.checkURL(localResource.getURL());
        } while (this.res == null);
        return this.res != null;
      }
    };
  }
  
  protected PermissionCollection getPermissions(CodeSource paramCodeSource)
  {
    PermissionCollection localPermissionCollection = super.getPermissions(paramCodeSource);
    
    this._appPolicy.addPermissions(localPermissionCollection, paramCodeSource);
    
    URL localURL = paramCodeSource.getLocation();
    Object localObject1;
    try
    {
      localObject1 = localURL.openConnection().getPermission();
    }
    catch (IOException localIOException)
    {
      localObject1 = null;
    }
    JARDesc localJARDesc = this._jcp.getJarDescFromFileURL(localURL);
    Object localObject2;
    if (localJARDesc != null)
    {
      localObject2 = Cache.getBaseDirsForHost(localJARDesc.getLocation());
      for (int i = 0; i < localObject2.length; i++)
      {
        String str = localObject2[i];
        if (str != null)
        {
          if (str.endsWith(File.separator)) {
            str = str + '-';
          } else {
            str = str + File.separator + '-';
          }
          localPermissionCollection.add(new FilePermission(str, "read"));
        }
      }
    }
    if ((localObject1 instanceof FilePermission))
    {
      localObject2 = ((Permission)localObject1).getName();
      if (((String)localObject2).endsWith(File.separator))
      {
        localObject2 = (String)localObject2 + "-";
        localObject1 = new FilePermission((String)localObject2, "read");
      }
    }
    else if ((localObject1 == null) && (localURL.getProtocol().equals("file")))
    {
      localObject2 = localURL.getFile().replace('/', File.separatorChar);
      if (((String)localObject2).endsWith(File.separator)) {
        localObject2 = (String)localObject2 + "-";
      }
      localObject1 = new FilePermission((String)localObject2, "read");
    }
    else
    {
      localObject2 = localURL.getHost();
      if (localObject2 == null) {
        localObject2 = "localhost";
      }
      localObject1 = new SocketPermission((String)localObject2, "connect, accept");
    }
    if (localObject1 != null)
    {
      localObject2 = System.getSecurityManager();
      if (localObject2 != null)
      {
        Object localObject3 = localObject1;
        AccessController.doPrivileged(new PrivilegedAction()
        {
          private final SecurityManager val$sm;
          private final Permission val$fp;
          
          public Object run()
            throws SecurityException
          {
            this.val$sm.checkPermission(this.val$fp);
            return null;
          }
        }, this._acc);
      }
      localPermissionCollection.add((Permission)localObject1);
    }
    if (!localPermissionCollection.implies(new AWTPermission("accessClipboard"))) {
      AppContext.getAppContext().put("UNTRUSTED_CLIPBOARD_ACCESS_KEY", Boolean.TRUE);
    }
    return localPermissionCollection;
  }
  
  public final synchronized Class loadClass(String paramString, boolean paramBoolean)
    throws ClassNotFoundException
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      int i = paramString.lastIndexOf('.');
      if (i != -1) {
        localSecurityManager.checkPackageAccess(paramString.substring(0, i));
      }
    }
    return super.loadClass(paramString, paramBoolean);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\JNLPClassLoader.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */