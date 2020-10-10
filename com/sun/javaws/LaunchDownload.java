package com.sun.javaws;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.cache.DiskCacheEntry;
import com.sun.javaws.cache.DownloadProtocol;
import com.sun.javaws.cache.DownloadProtocol.DownloadDelegate;
import com.sun.javaws.exceptions.ErrorCodeResponseException;
import com.sun.javaws.exceptions.FailedDownloadingResourceException;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.exceptions.LaunchDescException;
import com.sun.javaws.exceptions.MissingFieldException;
import com.sun.javaws.exceptions.MultipleHostsException;
import com.sun.javaws.exceptions.NativeLibViolationException;
import com.sun.javaws.exceptions.UnsignedAccessViolationException;
import com.sun.javaws.jnl.AppletDesc;
import com.sun.javaws.jnl.ApplicationDesc;
import com.sun.javaws.jnl.ExtensionDesc;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.InstallerDesc;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.PackageDesc;
import com.sun.javaws.jnl.PropertyDesc;
import com.sun.javaws.jnl.ResourceVisitor;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.security.AppPolicy;
import com.sun.javaws.security.SigningInfo;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class LaunchDownload
{
  private static boolean updateAvailable = false;
  private static int numThread = 0;
  private static Object syncObj = new Object();
  private static final String SIGNED_JNLP_ENTRY = "JNLP-INF/APPLICATION.JNLP";
  
  private static boolean compareByteArray(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    if (paramArrayOfByte1.length == paramArrayOfByte2.length)
    {
      for (int i = 0; i < paramArrayOfByte1.length; i++) {
        if (paramArrayOfByte1[i] != paramArrayOfByte2[i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  static boolean updateLaunchDescInCache(LaunchDesc paramLaunchDesc)
  {
    URL localURL = paramLaunchDesc.getLocation();
    if (localURL == null) {
      localURL = paramLaunchDesc.getCanonicalHome();
    }
    if (localURL != null) {
      try
      {
        DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getCachedLaunchedFile(localURL);
        if (localDiskCacheEntry != null)
        {
          File localFile = localDiskCacheEntry.getFile();
          byte[] arrayOfByte1 = LaunchDescFactory.readBytes(new FileInputStream(localFile), localFile.length());
          
          byte[] arrayOfByte2 = paramLaunchDesc.getSource().getBytes();
          if ((arrayOfByte1 != null) && (arrayOfByte2 != null) && 
            (compareByteArray(arrayOfByte1, arrayOfByte2))) {
            return false;
          }
        }
        Cache.putCanonicalLaunchDesc(localURL, paramLaunchDesc);
        return true;
      }
      catch (JNLPException localJNLPException)
      {
        Trace.ignoredException(localJNLPException);
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
    return false;
  }
  
  static LaunchDesc getUpdatedLaunchDesc(LaunchDesc paramLaunchDesc)
    throws JNLPException, IOException
  {
    if (paramLaunchDesc.getLocation() == null) {
      return null;
    }
    boolean bool = DownloadProtocol.isLaunchFileUpdateAvailable(paramLaunchDesc.getLocation());
    if (!bool) {
      return null;
    }
    Trace.println("Downloading updated JNLP descriptor from: " + paramLaunchDesc.getLocation(), TraceLevel.BASIC);
    
    DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getLaunchFile(paramLaunchDesc.getLocation(), false);
    try
    {
      return LaunchDescFactory.buildDescriptor(localDiskCacheEntry.getFile());
    }
    catch (JNLPException localJNLPException)
    {
      Cache.removeEntry(localDiskCacheEntry);
      throw localJNLPException;
    }
  }
  
  public static boolean isInCache(LaunchDesc paramLaunchDesc)
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return true;
    }
    try
    {
      if (paramLaunchDesc.getLocation() != null)
      {
        DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getCachedLaunchedFile(paramLaunchDesc.getLocation());
        if (localDiskCacheEntry == null) {
          return false;
        }
      }
      boolean bool = getCachedExtensions(paramLaunchDesc);
      if (!bool) {
        return false;
      }
      JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(false);
      for (int i = 0; i < arrayOfJARDesc.length; i++)
      {
        int j = arrayOfJARDesc[i].isJavaFile() ? 0 : 1;
        if (!DownloadProtocol.isInCache(arrayOfJARDesc[i].getLocation(), arrayOfJARDesc[i].getVersion(), j)) {
          return false;
        }
      }
    }
    catch (JNLPException localJNLPException)
    {
      Trace.ignoredException(localJNLPException);
      
      return false;
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
      
      return false;
    }
    return true;
  }
  
  private static void updateCheck(URL paramURL, String paramString, int paramInt, boolean paramBoolean)
  {
    synchronized (syncObj)
    {
      numThread += 1;
    }
    new Thread(new Runnable()
    {
      private final URL val$url;
      private final String val$version;
      private final int val$type;
      private final boolean val$lazy;
      
      public void run()
      {
        try
        {
          boolean bool = DownloadProtocol.isUpdateAvailable(this.val$url, this.val$version, this.val$type);
          if ((bool) && (this.val$lazy))
          {
            File localFile = DownloadProtocol.getCachedVersion(this.val$url, this.val$version, this.val$type).getFile();
            if (localFile != null) {
              localFile.delete();
            }
          }
          synchronized (LaunchDownload.syncObj)
          {
            if ((bool) && (!LaunchDownload.updateAvailable)) {
              LaunchDownload.access$102(true);
            }
          }
        }
        catch (JNLPException bool)
        {
          Trace.ignoredException((Exception)???);
        }
        finally
        {
          synchronized (LaunchDownload.syncObj)
          {
            LaunchDownload.access$210();
          }
        }
      }
    }).start();
  }
  
  public static boolean isUpdateAvailable(LaunchDesc paramLaunchDesc)
    throws JNLPException
  {
    URL localURL1 = paramLaunchDesc.getLocation();
    if (localURL1 != null)
    {
      boolean bool = DownloadProtocol.isLaunchFileUpdateAvailable(localURL1);
      if (bool) {
        return true;
      }
    }
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return false;
    }
    ExtensionDesc[] arrayOfExtensionDesc = localResourcesDesc.getExtensionDescs();
    for (int i = 0; i < arrayOfExtensionDesc.length; i++)
    {
      URL localURL2 = arrayOfExtensionDesc[i].getLocation();
      if (localURL2 != null) {
        updateCheck(localURL2, arrayOfExtensionDesc[i].getVersion(), 4, false);
      }
    }
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(true);
    Object localObject1;
    for (int j = 0; j < arrayOfJARDesc.length; j++)
    {
      URL localURL3 = arrayOfJARDesc[j].getLocation();
      localObject1 = arrayOfJARDesc[j].getVersion();
      int m = arrayOfJARDesc[j].isJavaFile() ? 0 : 1;
      if (DownloadProtocol.isInCache(localURL3, (String)localObject1, m)) {
        updateCheck(localURL3, (String)localObject1, m, arrayOfJARDesc[j].isLazyDownload());
      }
    }
    IconDesc[] arrayOfIconDesc = paramLaunchDesc.getInformation().getIcons();
    if (arrayOfIconDesc != null) {
      for (int k = 0; k < arrayOfIconDesc.length; k++)
      {
        localObject1 = arrayOfIconDesc[k].getLocation();
        String str = arrayOfIconDesc[k].getVersion();
        int n = 2;
        if (DownloadProtocol.isInCache((URL)localObject1, str, n)) {
          updateCheck((URL)localObject1, str, n, false);
        }
      }
    }
    while (numThread > 0) {
      synchronized (syncObj)
      {
        if (updateAvailable) {
          break;
        }
      }
    }
    return updateAvailable;
  }
  
  private static class DownloadCallbackHelper
    implements DownloadProtocol.DownloadDelegate
  {
    LaunchDownload.DownloadProgress _downloadProgress;
    long _totalSize;
    long _downloadedSoFar;
    long _currentTotal;
    
    public DownloadCallbackHelper(LaunchDownload.DownloadProgress paramDownloadProgress, long paramLong)
    {
      this._downloadProgress = paramDownloadProgress;
      this._totalSize = paramLong;
      this._downloadedSoFar = 0L;
    }
    
    public void downloading(URL paramURL, String paramString, int paramInt1, int paramInt2, boolean paramBoolean)
    {
      int i = -1;
      if (this._totalSize != -1L)
      {
        double d1 = paramBoolean ? 0.8D : 0.9D;
        double d2 = this._downloadedSoFar + d1 * paramInt1;
        
        i = getPercent(d2);
        this._currentTotal = paramInt2;
      }
      if (this._downloadProgress != null) {
        this._downloadProgress.progress(paramURL, paramString, this._downloadedSoFar + paramInt1, this._totalSize, i);
      }
    }
    
    public void patching(URL paramURL, String paramString, int paramInt)
    {
      int i = -1;
      if (this._totalSize != -1L)
      {
        double d = this._downloadedSoFar + this._currentTotal * (0.8D + paramInt / 1000.0D);
        
        i = getPercent(d);
      }
      if (this._downloadProgress != null) {
        this._downloadProgress.patching(paramURL, paramString, paramInt, i);
      }
    }
    
    public void validating(URL paramURL, int paramInt1, int paramInt2)
    {
      int i = -1;
      if ((this._totalSize != -1L) && (paramInt2 != 0))
      {
        double d = this._downloadedSoFar + 0.9D * this._currentTotal + 0.1D * this._currentTotal * (paramInt1 / paramInt2);
        
        i = getPercent(d);
      }
      if (this._downloadProgress != null) {
        this._downloadProgress.validating(paramURL, null, paramInt1, paramInt2, i);
      }
      if (paramInt1 == paramInt2) {
        this._downloadedSoFar += this._currentTotal;
      }
    }
    
    public void downloadFailed(URL paramURL, String paramString)
    {
      if (this._downloadProgress != null) {
        this._downloadProgress.downloadFailed(paramURL, paramString);
      }
    }
    
    private int getPercent(double paramDouble)
    {
      if (paramDouble > this._totalSize)
      {
        this._totalSize = -1L;
        return -1;
      }
      double d = paramDouble * 100.0D / this._totalSize;
      return (int)(d + 0.5D);
    }
  }
  
  public static File[] getNativeDirectories(LaunchDesc paramLaunchDesc)
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return new File[0];
    }
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(true);
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < arrayOfJARDesc.length; i++) {
      if (arrayOfJARDesc[i].isNativeLib())
      {
        URL localURL = arrayOfJARDesc[i].getLocation();
        String str = arrayOfJARDesc[i].getVersion();
        DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getCachedVersion(localURL, str, 1);
        if (localDiskCacheEntry != null) {
          localArrayList.add(localDiskCacheEntry.getDirectory());
        }
      }
    }
    File[] arrayOfFile = new File[localArrayList.size()];
    return (File[])localArrayList.toArray(arrayOfFile);
  }
  
  static void downloadExtensions(LaunchDesc paramLaunchDesc, DownloadProgress paramDownloadProgress, int paramInt, ArrayList paramArrayList)
    throws IOException, JNLPException
  {
    downloadExtensionsHelper(paramLaunchDesc, paramDownloadProgress, paramInt, false, paramArrayList);
  }
  
  private static boolean getCachedExtensions(LaunchDesc paramLaunchDesc)
    throws IOException, JNLPException
  {
    return downloadExtensionsHelper(paramLaunchDesc, null, 0, true, null);
  }
  
  private static boolean downloadExtensionsHelper(LaunchDesc paramLaunchDesc, DownloadProgress paramDownloadProgress, int paramInt, boolean paramBoolean, ArrayList paramArrayList)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return true;
    }
    String str1 = JREInfo.getKnownPlatforms();
    
    ArrayList localArrayList = new ArrayList();
    
    localResourcesDesc.visit(new ResourceVisitor()
    {
      private final ArrayList val$list;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc) {}
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
      {
        this.val$list.add(paramAnonymousExtensionDesc);
      }
    });
    paramInt += localArrayList.size();
    for (int i = 0; i < localArrayList.size(); i++)
    {
      ExtensionDesc localExtensionDesc = (ExtensionDesc)localArrayList.get(i);
      
      String str2 = localExtensionDesc.getName();
      if (str2 == null)
      {
        str2 = localExtensionDesc.getLocation().toString();
        int j = str2.lastIndexOf('/');
        if (j > 0) {
          str2 = str2.substring(j + 1, str2.length());
        }
      }
      paramInt--;
      if (paramDownloadProgress != null) {
        paramDownloadProgress.extensionDownload(str2, paramInt);
      }
      DiskCacheEntry localDiskCacheEntry = null;
      if (!paramBoolean)
      {
        localDiskCacheEntry = DownloadProtocol.getExtension(localExtensionDesc.getLocation(), localExtensionDesc.getVersion(), str1, false);
      }
      else
      {
        localDiskCacheEntry = DownloadProtocol.getCachedExtension(localExtensionDesc.getLocation(), localExtensionDesc.getVersion(), str1);
        if (localDiskCacheEntry == null) {
          return false;
        }
      }
      Trace.println("Downloaded extension: " + localExtensionDesc.getLocation() + ": " + localDiskCacheEntry.getFile(), TraceLevel.NETWORK);
      
      LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localDiskCacheEntry.getFile());
      int k = 0;
      if (localLaunchDesc.getLaunchType() == 3)
      {
        k = 1;
      }
      else if (localLaunchDesc.getLaunchType() == 4)
      {
        localExtensionDesc.setInstaller(true);
        
        LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(localDiskCacheEntry.getLocation(), localDiskCacheEntry.getVersionId(), paramLaunchDesc, false);
        
        k = !localLocalApplicationProperties.isLocallyInstalled() ? 1 : 0;
        if ((paramArrayList != null) && ((isUpdateAvailable(localLaunchDesc)) || (k != 0))) {
          paramArrayList.add(localDiskCacheEntry.getFile());
        }
        if ((paramBoolean) && (k != 0)) {
          return false;
        }
      }
      else
      {
        throw new MissingFieldException(localLaunchDesc.getSource(), "<component-desc>|<installer-desc>");
      }
      if (k != 0)
      {
        localExtensionDesc.setExtensionDesc(localLaunchDesc);
        
        boolean bool = downloadExtensionsHelper(localLaunchDesc, paramDownloadProgress, paramInt, paramBoolean, paramArrayList);
        if (!bool) {
          return false;
        }
      }
    }
    return true;
  }
  
  public static void downloadJRE(LaunchDesc paramLaunchDesc, DownloadProgress paramDownloadProgress, ArrayList paramArrayList)
    throws JNLPException, IOException
  {
    JREDesc localJREDesc = paramLaunchDesc.getResources().getSelectedJRE();
    String str1 = localJREDesc.getVersion();
    URL localURL = localJREDesc.getHref();
    
    boolean bool = localURL == null;
    if (localURL == null)
    {
      str2 = Config.getProperty("deployment.javaws.installURL");
      if (str2 != null) {
        try
        {
          localURL = new URL(str2);
        }
        catch (MalformedURLException localMalformedURLException) {}
      }
    }
    if (paramDownloadProgress != null) {
      paramDownloadProgress.jreDownload(str1, localURL);
    }
    String str2 = JREInfo.getKnownPlatforms();
    DiskCacheEntry localDiskCacheEntry = null;
    try
    {
      localDiskCacheEntry = DownloadProtocol.getJRE(localURL, str1, bool, str2);
    }
    catch (ErrorCodeResponseException localErrorCodeResponseException)
    {
      localErrorCodeResponseException.setJreDownload(true);
      throw localErrorCodeResponseException;
    }
    LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localDiskCacheEntry.getFile());
    if (localLaunchDesc.getLaunchType() != 4) {
      throw new MissingFieldException(localLaunchDesc.getSource(), "<installer-desc>");
    }
    if (paramArrayList != null) {
      paramArrayList.add(localDiskCacheEntry.getFile());
    }
    localJREDesc.setExtensionDesc(localLaunchDesc);
    
    downloadExtensionsHelper(localLaunchDesc, paramDownloadProgress, 0, false, paramArrayList);
  }
  
  public static void downloadResource(LaunchDesc paramLaunchDesc, URL paramURL, String paramString, DownloadProgress paramDownloadProgress, boolean paramBoolean)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return;
    }
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getResource(paramURL, paramString);
    downloadJarFiles(arrayOfJARDesc, paramDownloadProgress, paramBoolean);
  }
  
  public static void downloadParts(LaunchDesc paramLaunchDesc, String[] paramArrayOfString, DownloadProgress paramDownloadProgress, boolean paramBoolean)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return;
    }
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getPartJars(paramArrayOfString);
    downloadJarFiles(arrayOfJARDesc, paramDownloadProgress, paramBoolean);
  }
  
  public static void downloadExtensionPart(LaunchDesc paramLaunchDesc, URL paramURL, String paramString, String[] paramArrayOfString, DownloadProgress paramDownloadProgress, boolean paramBoolean)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return;
    }
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getExtensionPart(paramURL, paramString, paramArrayOfString);
    downloadJarFiles(arrayOfJARDesc, paramDownloadProgress, paramBoolean);
  }
  
  public static void downloadEagerorAll(LaunchDesc paramLaunchDesc, boolean paramBoolean1, DownloadProgress paramDownloadProgress, boolean paramBoolean2)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return;
    }
    Object localObject = localResourcesDesc.getEagerOrAllJarDescs(paramBoolean1);
    if (!paramBoolean1)
    {
      JARDesc[] arrayOfJARDesc1 = localResourcesDesc.getEagerOrAllJarDescs(true);
      if (arrayOfJARDesc1.length != localObject.length)
      {
        HashSet localHashSet = new HashSet(Arrays.asList((Object[])localObject));
        int i = 0;
        for (int j = 0; j < arrayOfJARDesc1.length; j++)
        {
          URL localURL = arrayOfJARDesc1[j].getLocation();
          String str = arrayOfJARDesc1[j].getVersion();
          int n = arrayOfJARDesc1[j].isJavaFile() ? 0 : 1;
          if ((!localHashSet.contains(arrayOfJARDesc1[j])) && (DownloadProtocol.isInCache(localURL, str, n))) {
            i++;
          } else {
            arrayOfJARDesc1[j] = null;
          }
        }
        if (i > 0)
        {
          JARDesc[] arrayOfJARDesc2 = new JARDesc[localObject.length + i];
          System.arraycopy(localObject, 0, arrayOfJARDesc2, 0, localObject.length);
          int k = localObject.length;
          for (int m = 0; m < arrayOfJARDesc1.length; m++) {
            if (arrayOfJARDesc1[m] != null) {
              arrayOfJARDesc2[(k++)] = arrayOfJARDesc1[m];
            }
          }
          localObject = arrayOfJARDesc2;
        }
      }
    }
    downloadJarFiles((JARDesc[])localObject, paramDownloadProgress, paramBoolean2);
  }
  
  private static void downloadJarFiles(JARDesc[] paramArrayOfJARDesc, DownloadProgress paramDownloadProgress, boolean paramBoolean)
    throws JNLPException, IOException
  {
    if (paramArrayOfJARDesc == null) {
      return;
    }
    Trace.println("Contacting server for JAR file sizes", TraceLevel.NETWORK);
    
    long l1 = 0L;
    for (int i = 0; (i < paramArrayOfJARDesc.length) && (l1 != -1L); i++) {
      try
      {
        JARDesc localJARDesc1 = paramArrayOfJARDesc[i];
        
        int k = localJARDesc1.isNativeLib() ? 1 : 0;
        
        long l2 = localJARDesc1.getSize();
        if (l2 == 0L) {
          l2 = DownloadProtocol.getDownloadSize(paramArrayOfJARDesc[i].getLocation(), paramArrayOfJARDesc[i].getVersion(), k);
        }
        Trace.println("Size of " + paramArrayOfJARDesc[i].getLocation() + ": " + l2, TraceLevel.NETWORK);
        if (l2 == -1L) {
          l1 = -1L;
        } else {
          l1 += l2;
        }
      }
      catch (JNLPException localJNLPException1)
      {
        if (paramDownloadProgress != null) {
          paramDownloadProgress.downloadFailed(paramArrayOfJARDesc[i].getLocation(), paramArrayOfJARDesc[i].getVersion());
        }
        throw localJNLPException1;
      }
    }
    Trace.println("Total size to download: " + l1, TraceLevel.NETWORK);
    if (l1 == 0L) {
      return;
    }
    DownloadCallbackHelper localDownloadCallbackHelper = new DownloadCallbackHelper(paramDownloadProgress, l1);
    for (int j = 0; j < paramArrayOfJARDesc.length; j++)
    {
      JARDesc localJARDesc2 = paramArrayOfJARDesc[j];
      try
      {
        int m = localJARDesc2.isNativeLib() ? 1 : 0;
        
        DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getResource(paramArrayOfJARDesc[j].getLocation(), paramArrayOfJARDesc[j].getVersion(), m, paramBoolean, localDownloadCallbackHelper);
        
        Trace.println("Downloaded " + paramArrayOfJARDesc[j].getLocation() + ": " + localDiskCacheEntry, TraceLevel.NETWORK);
        if (localDiskCacheEntry == null) {
          throw new FailedDownloadingResourceException(null, paramArrayOfJARDesc[j].getLocation(), paramArrayOfJARDesc[j].getVersion(), null);
        }
      }
      catch (JNLPException localJNLPException2)
      {
        if (paramDownloadProgress != null) {
          paramDownloadProgress.downloadFailed(localJARDesc2.getLocation(), localJARDesc2.getVersion());
        }
        throw localJNLPException2;
      }
    }
  }
  
  static void checkJNLPSecurity(LaunchDesc paramLaunchDesc)
    throws MultipleHostsException, NativeLibViolationException
  {
    boolean[] arrayOfBoolean1 = new boolean[1];
    boolean[] arrayOfBoolean2 = new boolean[1];
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return;
    }
    JARDesc localJARDesc = paramLaunchDesc.getResources().getMainJar(true);
    if (localJARDesc == null) {
      return;
    }
    checkJNLPSecurityHelper(paramLaunchDesc, localJARDesc.getLocation().getHost(), arrayOfBoolean2, arrayOfBoolean1);
    if (arrayOfBoolean2[0] != 0) {
      throw new MultipleHostsException();
    }
    if (arrayOfBoolean1[0] != 0) {
      throw new NativeLibViolationException();
    }
  }
  
  private static void checkJNLPSecurityHelper(LaunchDesc paramLaunchDesc, String paramString, boolean[] paramArrayOfBoolean1, boolean[] paramArrayOfBoolean2)
  {
    if (paramLaunchDesc.getSecurityModel() != 0) {
      return;
    }
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return;
    }
    localResourcesDesc.visit(new ResourceVisitor()
    {
      private final boolean[] val$hostViolation;
      private final String val$host;
      private final boolean[] val$nativeLibViolation;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc)
      {
        String str = paramAnonymousJARDesc.getLocation().getHost();
        this.val$hostViolation[0] = ((this.val$hostViolation[0] != 0) || (!this.val$host.equals(str)) ? 1 : false);
        this.val$nativeLibViolation[0] = ((this.val$nativeLibViolation[0] != 0) || (paramAnonymousJARDesc.isNativeLib()) ? 1 : false);
      }
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
      {
        if ((this.val$hostViolation[0] == 0) && (this.val$nativeLibViolation[0] == 0))
        {
          LaunchDesc localLaunchDesc = paramAnonymousExtensionDesc.getExtensionDesc();
          if ((localLaunchDesc != null) && (localLaunchDesc.getSecurityModel() == 0))
          {
            String str = paramAnonymousExtensionDesc.getLocation().getHost();
            this.val$hostViolation[0] = ((this.val$hostViolation[0] != 0) || (!this.val$host.equals(str)) ? 1 : false);
            if (this.val$hostViolation[0] == 0) {
              LaunchDownload.checkJNLPSecurityHelper(localLaunchDesc, this.val$host, this.val$hostViolation, this.val$nativeLibViolation);
            }
          }
        }
      }
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
    });
  }
  
  public static long getCachedSize(LaunchDesc paramLaunchDesc)
  {
    long l = 0L;
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return l;
    }
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(true);
    for (int i = 0; i < arrayOfJARDesc.length; i++)
    {
      int j = arrayOfJARDesc[i].isNativeLib() ? 1 : 0;
      
      l += DownloadProtocol.getCachedSize(arrayOfJARDesc[i].getLocation(), arrayOfJARDesc[i].getVersion(), 0);
    }
    return l;
  }
  
  static String getMainClassName(LaunchDesc paramLaunchDesc, boolean paramBoolean)
    throws IOException, JNLPException, LaunchDescException
  {
    String str = null;
    
    ApplicationDesc localApplicationDesc = paramLaunchDesc.getApplicationDescriptor();
    if (localApplicationDesc != null) {
      str = localApplicationDesc.getMainClass();
    }
    InstallerDesc localInstallerDesc = paramLaunchDesc.getInstallerDescriptor();
    if (localInstallerDesc != null) {
      str = localInstallerDesc.getMainClass();
    }
    AppletDesc localAppletDesc = paramLaunchDesc.getAppletDescriptor();
    if (localAppletDesc != null) {
      str = localAppletDesc.getAppletClass();
    }
    if ((str != null) && (str.length() == 0)) {
      str = null;
    }
    if (paramLaunchDesc.getResources() == null) {
      return null;
    }
    JARDesc localJARDesc = paramLaunchDesc.getResources().getMainJar(paramBoolean);
    if (localJARDesc == null) {
      return null;
    }
    DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getResource(localJARDesc.getLocation(), localJARDesc.getVersion(), 0, true, null);
    
    JarFile localJarFile = new JarFile(localDiskCacheEntry.getFile());
    if ((str == null) && (paramLaunchDesc.getLaunchType() != 2))
    {
      localObject = localJarFile.getManifest();
      str = localObject != null ? ((Manifest)localObject).getMainAttributes().getValue("Main-Class") : null;
    }
    if (str == null) {
      throw new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.nomainclassspec"), null);
    }
    Object localObject = str.replace('.', '/') + ".class";
    if (localJarFile.getEntry((String)localObject) == null) {
      throw new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.nomainclass", str, localJARDesc.getLocation().toString()), null);
    }
    return str;
  }
  
  static void checkSignedLaunchDesc(LaunchDesc paramLaunchDesc)
    throws IOException, JNLPException
  {
    ArrayList localArrayList = new ArrayList();
    
    addExtensions(localArrayList, paramLaunchDesc);
    for (int i = 0; i < localArrayList.size(); i++)
    {
      LaunchDesc localLaunchDesc = (LaunchDesc)localArrayList.get(i);
      checkSignedLaunchDescHelper(localLaunchDesc);
    }
  }
  
  static void checkSignedResources(LaunchDesc paramLaunchDesc)
    throws IOException, JNLPException
  {
    ArrayList localArrayList = new ArrayList();
    
    addExtensions(localArrayList, paramLaunchDesc);
    for (int i = 0; i < localArrayList.size(); i++)
    {
      LaunchDesc localLaunchDesc = (LaunchDesc)localArrayList.get(i);
      checkSignedResourcesHelper(localLaunchDesc);
    }
  }
  
  private static void addExtensions(ArrayList paramArrayList, LaunchDesc paramLaunchDesc)
  {
    paramArrayList.add(paramLaunchDesc);
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc != null) {
      localResourcesDesc.visit(new ResourceVisitor()
      {
        private final ArrayList val$list;
        
        public void visitJARDesc(JARDesc paramAnonymousJARDesc) {}
        
        public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
        
        public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
        
        public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
        
        public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
        {
          if (!paramAnonymousExtensionDesc.isInstaller()) {
            LaunchDownload.addExtensions(this.val$list, paramAnonymousExtensionDesc.getExtensionDesc());
          }
        }
      });
    }
  }
  
  private static void checkSignedLaunchDescHelper(LaunchDesc paramLaunchDesc)
    throws IOException, JNLPException
  {
    boolean bool = paramLaunchDesc.isApplicationDescriptor();
    
    byte[] arrayOfByte = null;
    try
    {
      arrayOfByte = getSignedJNLPFile(paramLaunchDesc, bool);
      if (arrayOfByte != null)
      {
        LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(arrayOfByte);
        
        Trace.println("Signed JNLP file: ", TraceLevel.BASIC);
        Trace.println(localLaunchDesc.toString(), TraceLevel.BASIC);
        
        paramLaunchDesc.checkSigning(localLaunchDesc);
        arrayOfByte = null;
      }
    }
    catch (LaunchDescException localLaunchDescException)
    {
      localLaunchDescException.setIsSignedLaunchDesc();
      throw localLaunchDescException;
    }
    catch (IOException localIOException)
    {
      throw localIOException;
    }
    catch (JNLPException localJNLPException)
    {
      throw localJNLPException;
    }
  }
  
  private static void checkSignedResourcesHelper(LaunchDesc paramLaunchDesc)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null) {
      return;
    }
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getLocalJarDescs();
    
    int i = 1;
    Object localObject1 = null;
    Object localObject2 = null;
    URL localURL1 = paramLaunchDesc.getCanonicalHome();
    
    int j = 0;
    URL localURL2 = null;
    for (int k = 0; k < arrayOfJARDesc.length; k++)
    {
      JARDesc localJARDesc = arrayOfJARDesc[k];
      int m = localJARDesc.isJavaFile() ? 0 : 1;
      
      DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getCachedVersion(localJARDesc.getLocation(), localJARDesc.getVersion(), m);
      if (localDiskCacheEntry != null)
      {
        j++;
        JarFile localJarFile = new JarFile(localDiskCacheEntry.getFile());
        CodeSource localCodeSource = SigningInfo.getCodeSource(localURL1, localJarFile);
        if (localCodeSource != null)
        {
          Certificate[] arrayOfCertificate = localCodeSource.getCertificates();
          if (arrayOfCertificate == null)
          {
            Trace.println("getCertChain returned null for: " + localDiskCacheEntry.getFile(), TraceLevel.BASIC);
            
            i = 0;
            localURL2 = localJARDesc.getLocation();
          }
          if (localObject1 == null)
          {
            localObject1 = arrayOfCertificate;
            localObject2 = localCodeSource;
          }
          else if (arrayOfCertificate != null)
          {
            if (!SigningInfo.equalChains((Certificate[])localObject1, arrayOfCertificate)) {
              throw new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.singlecertviolation"), null);
            }
          }
        }
      }
    }
    if (paramLaunchDesc.getSecurityModel() != 0)
    {
      if (i == 0) {
        throw new UnsignedAccessViolationException(paramLaunchDesc, localURL2, true);
      }
      if (j > 0) {
        AppPolicy.getInstance().grantUnrestrictedAccess(paramLaunchDesc, (CodeSource)localObject2);
      }
    }
  }
  
  private static byte[] getSignedJNLPFile(LaunchDesc paramLaunchDesc, boolean paramBoolean)
    throws IOException, JNLPException
  {
    if (paramLaunchDesc.getResources() == null) {
      return null;
    }
    JARDesc localJARDesc = paramLaunchDesc.getResources().getMainJar(paramBoolean);
    if (localJARDesc == null) {
      return null;
    }
    DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getResource(localJARDesc.getLocation(), localJARDesc.getVersion(), 0, true, null);
    
    JarFile localJarFile = new JarFile(localDiskCacheEntry.getFile());
    Object localObject1 = localJarFile.getJarEntry("JNLP-INF/APPLICATION.JNLP");
    if (localObject1 == null)
    {
      localObject2 = localJarFile.entries();
      while ((((Enumeration)localObject2).hasMoreElements()) && (localObject1 == null))
      {
        localObject3 = (JarEntry)((Enumeration)localObject2).nextElement();
        if (((JarEntry)localObject3).getName().equalsIgnoreCase("JNLP-INF/APPLICATION.JNLP")) {
          localObject1 = localObject3;
        }
      }
    }
    if (localObject1 == null)
    {
      if (localJarFile != null) {
        localJarFile.close();
      }
      return null;
    }
    Object localObject2 = new byte[(int)((JarEntry)localObject1).getSize()];
    Object localObject3 = new DataInputStream(localJarFile.getInputStream((ZipEntry)localObject1));
    ((DataInputStream)localObject3).readFully((byte[])localObject2, 0, (int)((JarEntry)localObject1).getSize());
    ((DataInputStream)localObject3).close();
    localJarFile.close();
    
    return (byte[])localObject2;
  }
  
  public static abstract interface DownloadProgress
  {
    public abstract void jreDownload(String paramString, URL paramURL);
    
    public abstract void extensionDownload(String paramString, int paramInt);
    
    public abstract void progress(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt);
    
    public abstract void validating(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt);
    
    public abstract void patching(URL paramURL, String paramString, int paramInt1, int paramInt2);
    
    public abstract void downloadFailed(URL paramURL, String paramString);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\LaunchDownload.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */