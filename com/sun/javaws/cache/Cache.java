package com.sun.javaws.cache;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.Globals;
import com.sun.javaws.Launcher;
import com.sun.javaws.LocalApplicationProperties;
import com.sun.javaws.LocalInstallHandler;
import com.sun.javaws.SplashScreen;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.RContentDesc;
import com.sun.javaws.util.VersionID;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

public class Cache
{
  public static final char RESOURCE_TYPE = 'R';
  public static final char APPLICATION_TYPE = 'A';
  public static final char EXTENSION_TYPE = 'E';
  public static final char MUFFIN_TYPE = 'P';
  public static final char MUFFIN_TAG_INDEX = '\000';
  public static final char MUFFIN_MAXSIZE_INDEX = '\001';
  private static DiskCache _activeCache = null;
  private static DiskCache _readOnlyCache = null;
  private static DiskCache _muffincache = null;
  private static final String LAST_ACCESSED_FILE_NAME = "lastAccessed";
  private static final String INDIRECT_EXTENSION = ".ind";
  private static HashMap _loadedProperties;
  
  static
  {
    initialize();
  }
  
  private static void initialize()
  {
    DiskCache localDiskCache1 = new DiskCache(getUserBaseDir());
    DiskCache localDiskCache2 = null;
    File localFile1 = getSysBaseDir();
    if (localFile1 != null) {
      localDiskCache2 = new DiskCache(localFile1);
    }
    File localFile2 = getMuffinCacheBaseDir();
    _muffincache = new DiskCache(localFile2);
    _loadedProperties = new HashMap();
    if ((localDiskCache2 != null) && (Globals.isSystemCache()))
    {
      _readOnlyCache = null;
      _activeCache = localDiskCache2;
    }
    else
    {
      _readOnlyCache = localDiskCache2;
      _activeCache = localDiskCache1;
      if (Globals.isSystemCache())
      {
        Globals.setSystemCache(false);
        Trace.println("There is no system cache configured, \"-system\" option ignored");
      }
    }
  }
  
  private static final char[] cacheTypes = { 'D', 'X', 'V', 'I', 'R', 'A', 'E', 'P' };
  
  public static boolean canWrite()
  {
    return _activeCache.canWrite();
  }
  
  public static void updateCache()
  {
    String str1 = Config.getProperty("deployment.javaws.cachedir");
    String str2 = Config.getProperty("deployment.user.cachedir") + File.separator + "javaws";
    
    File localFile1 = new File(str1);
    File localFile2 = new File(str2);
    
    Iterator localIterator = _activeCache.getOrphans();
    while (localIterator.hasNext()) {
      localObject1 = (DiskCacheEntry)localIterator.next();
    }
    localIterator = _activeCache.getJnlpCacheEntries();
    Object localObject1 = new ArrayList();
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    LocalApplicationProperties localLocalApplicationProperties;
    while (localIterator.hasNext())
    {
      DiskCacheEntry localDiskCacheEntry1 = (DiskCacheEntry)localIterator.next();
      LaunchDesc localLaunchDesc = null;
      localLocalApplicationProperties = null;
      try
      {
        localLaunchDesc = LaunchDescFactory.buildDescriptor(localDiskCacheEntry1.getFile());
        localLocalApplicationProperties = getLocalApplicationProperties(localDiskCacheEntry1, localLaunchDesc);
        if ((localLocalApplicationProperties != null) && (localLocalApplicationProperties.isLocallyInstalled()))
        {
          localLocalInstallHandler.uninstall(localLaunchDesc, localLocalApplicationProperties, true);
          ((ArrayList)localObject1).add(localDiskCacheEntry1);
        }
      }
      catch (Exception localException2)
      {
        Trace.ignoredException(localException2);
      }
    }
    int i = (str1.startsWith(str2)) || (str2.startsWith(str1)) ? 1 : 0;
    if ((i == 0) && (localFile1.exists()) && (localFile1.isDirectory())) {
      copy(localFile1, localFile2, new FilenameFilter()
      {
        private final File val$oldFile;
        
        public boolean accept(File paramAnonymousFile, String paramAnonymousString)
        {
          if ((paramAnonymousFile.equals(this.val$oldFile)) || (paramAnonymousFile.getParentFile().equals(this.val$oldFile))) {
            return !paramAnonymousString.equals("splashes");
          }
          if (paramAnonymousString.length() == 0) {
            return false;
          }
          int i = paramAnonymousString.charAt(0);
          for (int j = 0; j < Cache.cacheTypes.length; j++) {
            if (i == Cache.cacheTypes[j]) {
              return true;
            }
          }
          return false;
        }
      });
    }
    Config.setProperty("deployment.javaws.cachedir", null);
    Config.storeIfDirty();
    synchronized (Cache.class)
    {
      initialize();
    }
    localIterator = ((ArrayList)localObject1).iterator();
    while (localIterator.hasNext()) {
      try
      {
        ??? = null;
        localLocalApplicationProperties = null;
        DiskCacheEntry localDiskCacheEntry2 = (DiskCacheEntry)localIterator.next();
        ??? = LaunchDescFactory.buildDescriptor(localDiskCacheEntry2.getFile());
        localLocalApplicationProperties = getLocalApplicationProperties(localDiskCacheEntry2, (LaunchDesc)???);
        localLocalInstallHandler.doInstall((LaunchDesc)???, localLocalApplicationProperties);
      }
      catch (Exception localException1)
      {
        Trace.ignoredException(localException1);
      }
    }
  }
  
  private static void copy(File paramFile1, File paramFile2, FilenameFilter paramFilenameFilter)
  {
    Object localObject1;
    if (paramFile1.isDirectory())
    {
      paramFile2.mkdirs();
      localObject1 = paramFile1.listFiles(paramFilenameFilter);
      for (int i = 0; i < localObject1.length; i++) {
        copy(localObject1[i], new File(paramFile2.getPath() + File.separator + localObject1[i].getName()), paramFilenameFilter);
      }
    }
    else
    {
      localObject1 = new byte['Ѐ'];
      FileOutputStream localFileOutputStream = null;
      FileInputStream localFileInputStream = null;
      try
      {
        localFileOutputStream = new FileOutputStream(paramFile2);
        localFileInputStream = new FileInputStream(paramFile1);
        for (;;)
        {
          int j = localFileInputStream.read((byte[])localObject1);
          if (j == -1) {
            break;
          }
          localFileOutputStream.write((byte[])localObject1, 0, j);
        }
        return;
      }
      catch (Exception localException3)
      {
        Trace.ignoredException(localException3);
      }
      finally
      {
        try
        {
          if (localFileOutputStream != null) {
            localFileOutputStream.close();
          }
        }
        catch (Exception localException6) {}
        try
        {
          if (localFileInputStream != null) {
            localFileInputStream.close();
          }
        }
        catch (Exception localException7) {}
      }
    }
  }
  
  public static void saveRemovedApp(URL paramURL, String paramString)
  {
    Properties localProperties = getRemovedApps();
    localProperties.setProperty(paramURL.toString(), paramString);
    setRemovedApps(localProperties);
  }
  
  public static void setRemovedApps(Properties paramProperties)
  {
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(getRemovePath());
      paramProperties.store(localFileOutputStream, "Removed JNLP Applications");
    }
    catch (IOException localIOException) {}
  }
  
  public static Properties getRemovedApps()
  {
    Properties localProperties = new Properties();
    try
    {
      FileInputStream localFileInputStream = new FileInputStream(getRemovePath());
      localProperties.load(localFileInputStream);
    }
    catch (IOException localIOException) {}
    return localProperties;
  }
  
  public static String getRemovePath()
  {
    return Config.getJavawsCacheDir() + File.separator + "removed.apps";
  }
  
  private static File getMuffinCacheBaseDir()
  {
    String str = Config.getJavawsCacheDir() + File.separator + "muffins";
    File localFile = new File(str);
    if (!localFile.exists()) {
      localFile.mkdirs();
    }
    Trace.println("Muffin Cache = " + localFile, TraceLevel.CACHE);
    return localFile;
  }
  
  private static File getUserBaseDir()
  {
    String str = Config.getJavawsCacheDir();
    File localFile = new File(str);
    if (!localFile.exists()) {
      localFile.mkdirs();
    }
    Trace.println("User cache dir = " + localFile, TraceLevel.CACHE);
    return localFile;
  }
  
  private static File getSysBaseDir()
  {
    String str = Config.getSystemCacheDirectory();
    if ((str == null) || (str.length() == 0)) {
      return null;
    }
    File localFile = new File(str + File.separator + "javaws");
    if (!localFile.exists()) {
      localFile.mkdirs();
    }
    Trace.println("System cache dir = " + localFile, TraceLevel.CACHE);
    return localFile;
  }
  
  public static void remove()
  {
    Iterator localIterator = _activeCache.getJnlpCacheEntries();
    while (localIterator.hasNext())
    {
      DiskCacheEntry localDiskCacheEntry = (DiskCacheEntry)localIterator.next();
      LaunchDesc localLaunchDesc = null;
      try
      {
        localLaunchDesc = LaunchDescFactory.buildDescriptor(localDiskCacheEntry.getFile());
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
      if (localLaunchDesc != null)
      {
        LocalApplicationProperties localLocalApplicationProperties = getLocalApplicationProperties(localDiskCacheEntry, localLaunchDesc);
        
        remove(localDiskCacheEntry, localLocalApplicationProperties, localLaunchDesc);
      }
    }
    uninstallActiveCache();
    uninstallMuffinCache();
  }
  
  public static void remove(String paramString, LocalApplicationProperties paramLocalApplicationProperties, LaunchDesc paramLaunchDesc)
  {
    try
    {
      DiskCacheEntry localDiskCacheEntry = getCacheEntryFromFile(new File(paramString));
      remove(localDiskCacheEntry, paramLocalApplicationProperties, paramLaunchDesc);
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
  }
  
  public static void remove(DiskCacheEntry paramDiskCacheEntry, LocalApplicationProperties paramLocalApplicationProperties, LaunchDesc paramLaunchDesc)
  {
    InformationDesc localInformationDesc = paramLaunchDesc.getInformation();
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    if ((paramLaunchDesc.isApplicationDescriptor()) && (paramLaunchDesc.getLocation() != null)) {
      saveRemovedApp(paramLaunchDesc.getLocation(), localInformationDesc.getTitle());
    }
    paramLocalApplicationProperties.refresh();
    if (paramLocalApplicationProperties.isLocallyInstalled()) {
      if (paramLaunchDesc.isApplicationDescriptor())
      {
        if (localLocalInstallHandler != null) {
          localLocalInstallHandler.uninstall(paramLaunchDesc, paramLocalApplicationProperties, true);
        }
      }
      else if (paramLaunchDesc.isInstaller())
      {
        localObject1 = new ArrayList();
        ((ArrayList)localObject1).add(paramDiskCacheEntry.getFile());
        try
        {
          String str = paramLocalApplicationProperties.getInstallDirectory();
          Launcher.executeUninstallers((ArrayList)localObject1);
          JREInfo.removeJREsIn(str);
          deleteFile(new File(str));
        }
        catch (ExitException localExitException)
        {
          Trace.ignoredException(localExitException);
        }
      }
    }
    Object localObject1 = paramLocalApplicationProperties.getRegisteredTitle();
    Config.getInstance().addRemoveProgramsRemove((String)localObject1, Globals.isSystemCache());
    
    localLocalInstallHandler.removeAssociations(paramLaunchDesc, paramLocalApplicationProperties);
    
    SplashScreen.removeCustomSplash(paramLaunchDesc);
    if (localInformationDesc != null)
    {
      localObject2 = localInformationDesc.getIcons();
      Object localObject3;
      if (localObject2 != null) {
        for (int i = 0; i < localObject2.length; i++)
        {
          URL localURL = localObject2[i].getLocation();
          localObject3 = localObject2[i].getVersion();
          removeEntries('R', localURL, (String)localObject3);
        }
      }
      RContentDesc[] arrayOfRContentDesc = localInformationDesc.getRelatedContent();
      if (arrayOfRContentDesc != null) {
        for (int j = 0; j < arrayOfRContentDesc.length; j++)
        {
          localObject3 = arrayOfRContentDesc[j].getIcon();
          if (localObject3 != null) {
            removeEntries('R', (URL)localObject3, null);
          }
        }
      }
    }
    Object localObject2 = paramLaunchDesc.getCanonicalHome();
    if (localObject2 != null)
    {
      removeEntries('A', (URL)localObject2, null);
      removeEntries('E', (URL)localObject2, null);
    }
    if (paramDiskCacheEntry != null) {
      removeEntry(paramDiskCacheEntry);
    }
  }
  
  private static void deleteFile(File paramFile)
  {
    if (paramFile.isDirectory())
    {
      File[] arrayOfFile = paramFile.listFiles();
      if (arrayOfFile != null) {
        for (int i = 0; i < arrayOfFile.length; i++) {
          deleteFile(arrayOfFile[i]);
        }
      }
    }
    paramFile.delete();
  }
  
  private static void removeEntries(char paramChar, URL paramURL, String paramString)
  {
    if (paramURL == null) {
      return;
    }
    try
    {
      DiskCacheEntry[] arrayOfDiskCacheEntry = getCacheEntries(paramChar, paramURL, paramString, true);
      for (int i = 0; i < arrayOfDiskCacheEntry.length; i++) {
        removeEntry(arrayOfDiskCacheEntry[i]);
      }
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
  }
  
  public static File getCachedLaunchedFile(URL paramURL)
    throws IOException
  {
    DiskCacheEntry localDiskCacheEntry = getCacheEntry('A', paramURL, null);
    return localDiskCacheEntry == null ? null : localDiskCacheEntry.getFile();
  }
  
  public static File getCachedFile(URL paramURL)
  {
    File localFile1 = null;
    if (paramURL.getProtocol().equals("jar"))
    {
      String str1 = paramURL.getPath();
      int i = str1.indexOf("!/");
      if (i > 0) {
        try
        {
          String str2 = str1.substring(i + 2);
          URL localURL = new URL(str1.substring(0, i));
          
          File localFile2 = createNativeLibDir(localURL, null);
          return new File(localFile2, str2);
        }
        catch (MalformedURLException localMalformedURLException)
        {
          Trace.ignoredException(localMalformedURLException);
        }
        catch (IOException localIOException2)
        {
          Trace.ignoredException(localIOException2);
        }
      }
      return null;
    }
    if (paramURL.toString().endsWith(".jnlp")) {
      try
      {
        localFile1 = getCachedLaunchedFile(paramURL);
      }
      catch (IOException localIOException1)
      {
        Trace.ignoredException(localIOException1);
      }
    }
    return localFile1;
  }
  
  public static LocalApplicationProperties getLocalApplicationProperties(DiskCacheEntry paramDiskCacheEntry, LaunchDesc paramLaunchDesc)
  {
    return getLocalApplicationProperties(paramDiskCacheEntry.getLocation(), paramDiskCacheEntry.getVersionId(), paramLaunchDesc, paramDiskCacheEntry.getType() == 'A');
  }
  
  public static LocalApplicationProperties getLocalApplicationProperties(String paramString, LaunchDesc paramLaunchDesc)
  {
    DiskCacheEntry localDiskCacheEntry = getCacheEntryFromFile(new File(paramString));
    if (localDiskCacheEntry == null) {
      return null;
    }
    return getLocalApplicationProperties(localDiskCacheEntry.getLocation(), localDiskCacheEntry.getVersionId(), paramLaunchDesc, localDiskCacheEntry.getType() == 'A');
  }
  
  public static LocalApplicationProperties getLocalApplicationProperties(URL paramURL, LaunchDesc paramLaunchDesc)
  {
    return getLocalApplicationProperties(paramURL, null, paramLaunchDesc, true);
  }
  
  public static LocalApplicationProperties getLocalApplicationProperties(URL paramURL, String paramString, LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    if (paramURL == null) {
      return null;
    }
    String str = paramURL.toString().intern() + "?" + paramString;
    Object localObject1;
    synchronized (_loadedProperties)
    {
      localObject1 = (LocalApplicationProperties)_loadedProperties.get(str);
      if (localObject1 == null)
      {
        localObject1 = new DefaultLocalApplicationProperties(paramURL, paramString, paramLaunchDesc, paramBoolean);
        
        _loadedProperties.put(str, localObject1);
      }
      else
      {
        ((LocalApplicationProperties)localObject1).refreshIfNecessary();
      }
    }
    return (LocalApplicationProperties)localObject1;
  }
  
  public static LaunchDesc getLaunchDesc(URL paramURL, String paramString)
  {
    try
    {
      DiskCacheEntry localDiskCacheEntry = getCacheEntry('A', paramURL, paramString);
      if (localDiskCacheEntry != null) {
        try
        {
          return LaunchDescFactory.buildDescriptor(localDiskCacheEntry.getFile());
        }
        catch (Exception localException)
        {
          return null;
        }
      }
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    return null;
  }
  
  public static String getNewExtensionInstallDirectory()
    throws IOException
  {
    String str1 = getUserBaseDir().getAbsolutePath() + File.separator + "ext";
    
    String str2 = null;
    int i = 0;
    do
    {
      str2 = str1 + File.separator + "E" + new Date().getTime() + File.separator;
      
      File localFile = new File(str2);
      if (!localFile.mkdirs()) {
        str2 = null;
      }
      Thread.yield();
      if (str2 != null) {
        break;
      }
      i++;
    } while (i < 50);
    if (str2 == null) {
      throw new IOException("Unable to create temp. dir for extension");
    }
    return str2;
  }
  
  private static String createUniqueIndirectFile()
    throws IOException
  {
    String str = getUserBaseDir().getAbsolutePath() + File.separator + "indirect";
    
    File localFile1 = new File(str);
    
    localFile1.mkdirs();
    File localFile2 = File.createTempFile("indirect", ".ind", localFile1);
    
    return localFile2.getAbsolutePath();
  }
  
  public static void removeEntry(DiskCacheEntry paramDiskCacheEntry)
  {
    _activeCache.removeEntry(paramDiskCacheEntry);
  }
  
  public static long getLastAccessed(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      if (_readOnlyCache == null) {
        return 0L;
      }
      return _readOnlyCache.getLastUpdate();
    }
    return _activeCache.getLastUpdate();
  }
  
  public static long getLastAccessed()
  {
    return _activeCache.getLastUpdate();
  }
  
  public static void setLastAccessed()
  {
    _activeCache.recordLastUpdate();
  }
  
  public static String[] getBaseDirsForHost(URL paramURL)
  {
    String[] arrayOfString;
    if (_readOnlyCache == null)
    {
      arrayOfString = new String[1];
      arrayOfString[0] = _activeCache.getBaseDirForHost(paramURL);
    }
    else
    {
      arrayOfString = new String[2];
      arrayOfString[0] = _readOnlyCache.getBaseDirForHost(paramURL);
      arrayOfString[0] = _activeCache.getBaseDirForHost(paramURL);
    }
    return arrayOfString;
  }
  
  public static long getCacheSize()
    throws IOException
  {
    return _activeCache.getCacheSize();
  }
  
  public static void clean()
  {
    _activeCache.cleanResources();
  }
  
  public static long getOrphanSize(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      if (_readOnlyCache == null) {
        return 0L;
      }
      return _readOnlyCache.getOrphanSize();
    }
    return _activeCache.getOrphanSize();
  }
  
  public static void cleanResources()
  {
    _activeCache.cleanResources();
  }
  
  public static long getCacheSize(boolean paramBoolean)
  {
    try
    {
      if (paramBoolean)
      {
        if (_readOnlyCache == null) {
          return -1L;
        }
        return _readOnlyCache.getCacheSize();
      }
      return _activeCache.getCacheSize();
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    return 0L;
  }
  
  public static String[] getCacheVersions(char paramChar, URL paramURL)
    throws IOException
  {
    String[] arrayOfString1 = _activeCache.getCacheVersions(paramChar, paramURL);
    String[] arrayOfString2 = new String[0];
    String[] arrayOfString3 = arrayOfString1;
    if (_readOnlyCache != null)
    {
      arrayOfString2 = _readOnlyCache.getCacheVersions(paramChar, paramURL);
      if (arrayOfString2.length > 0)
      {
        arrayOfString3 = new String[arrayOfString1.length + arrayOfString2.length];
        System.arraycopy(arrayOfString1, 0, arrayOfString3, 0, arrayOfString1.length);
        System.arraycopy(arrayOfString2, 0, arrayOfString3, arrayOfString1.length, arrayOfString2.length);
      }
    }
    if (arrayOfString3.length > 1) {
      Arrays.sort(arrayOfString3, new Comparator()
      {
        public int compare(Object paramAnonymousObject1, Object paramAnonymousObject2)
        {
          VersionID localVersionID1 = new VersionID((String)paramAnonymousObject1);
          VersionID localVersionID2 = new VersionID((String)paramAnonymousObject2);
          return localVersionID1.isGreaterThan(localVersionID2) ? -1 : 1;
        }
      });
    }
    return arrayOfString3;
  }
  
  public static DiskCacheEntry[] getCacheEntries(char paramChar, URL paramURL, String paramString, boolean paramBoolean)
    throws IOException
  {
    DiskCacheEntry[] arrayOfDiskCacheEntry1 = _activeCache.getCacheEntries(paramChar, paramURL, paramString, paramBoolean);
    DiskCacheEntry[] arrayOfDiskCacheEntry2 = new DiskCacheEntry[0];
    if (_readOnlyCache != null) {
      arrayOfDiskCacheEntry2 = _readOnlyCache.getCacheEntries(paramChar, paramURL, paramString, paramBoolean);
    }
    if (arrayOfDiskCacheEntry2.length == 0) {
      return arrayOfDiskCacheEntry1;
    }
    int i = arrayOfDiskCacheEntry2.length + arrayOfDiskCacheEntry1.length;
    
    DiskCacheEntry[] arrayOfDiskCacheEntry3 = new DiskCacheEntry[i];
    int j = 0;
    for (j = 0; j < arrayOfDiskCacheEntry2.length; j++) {
      arrayOfDiskCacheEntry3[j] = arrayOfDiskCacheEntry2[j];
    }
    for (int k = 0; k < arrayOfDiskCacheEntry1.length; k++) {
      arrayOfDiskCacheEntry3[(j++)] = arrayOfDiskCacheEntry1[k];
    }
    return arrayOfDiskCacheEntry3;
  }
  
  public static DiskCacheEntry getMuffinCacheEntryFromFile(File paramFile)
  {
    return _muffincache.getCacheEntryFromFile(paramFile);
  }
  
  public static DiskCacheEntry getCacheEntryFromFile(File paramFile)
  {
    DiskCacheEntry localDiskCacheEntry1 = _activeCache.getCacheEntryFromFile(paramFile);
    if (_readOnlyCache != null)
    {
      DiskCacheEntry localDiskCacheEntry2 = _readOnlyCache.getCacheEntryFromFile(paramFile);
      if ((localDiskCacheEntry2 != null) && (localDiskCacheEntry2.newerThan(localDiskCacheEntry1))) {
        return localDiskCacheEntry2;
      }
    }
    return localDiskCacheEntry1;
  }
  
  public static File getTempCacheFile(URL paramURL, String paramString)
    throws IOException
  {
    return _activeCache.getTempCacheFile(paramURL, paramString);
  }
  
  public static DiskCacheEntry getCacheEntry(char paramChar, URL paramURL, String paramString)
    throws IOException
  {
    DiskCacheEntry localDiskCacheEntry1 = _activeCache.getCacheEntry(paramChar, paramURL, paramString);
    if (_readOnlyCache != null)
    {
      DiskCacheEntry localDiskCacheEntry2 = _readOnlyCache.getCacheEntry(paramChar, paramURL, paramString);
      if ((localDiskCacheEntry2 != null) && (localDiskCacheEntry2.newerThan(localDiskCacheEntry1))) {
        return localDiskCacheEntry2;
      }
    }
    return localDiskCacheEntry1;
  }
  
  public static File createNativeLibDir(URL paramURL, String paramString)
    throws IOException
  {
    return _activeCache.createNativeLibDir(paramURL, paramString);
  }
  
  public static Iterator getJnlpCacheEntries(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      if (_readOnlyCache == null) {
        return new ArrayList().iterator();
      }
      return _readOnlyCache.getJnlpCacheEntries();
    }
    return _activeCache.getJnlpCacheEntries();
  }
  
  public static File putMappedImage(URL paramURL, String paramString, File paramFile)
    throws IOException
  {
    return _activeCache.putMappedImage(paramURL, paramString, paramFile);
  }
  
  public static byte[] getLapData(char paramChar, URL paramURL, String paramString, boolean paramBoolean)
    throws IOException
  {
    if (paramBoolean) {
      return _readOnlyCache == null ? null : _readOnlyCache.getLapData(paramChar, paramURL, paramString);
    }
    return _activeCache.getLapData(paramChar, paramURL, paramString);
  }
  
  public static void putLapData(char paramChar, URL paramURL, String paramString, byte[] paramArrayOfByte)
    throws IOException
  {
    _activeCache.putLapData(paramChar, paramURL, paramString, paramArrayOfByte);
  }
  
  public static void insertEntry(char paramChar, URL paramURL, String paramString, File paramFile, long paramLong)
    throws IOException
  {
    _activeCache.insertEntry(paramChar, paramURL, paramString, paramFile, paramLong);
  }
  
  public static void putCanonicalLaunchDesc(URL paramURL, LaunchDesc paramLaunchDesc)
    throws IOException
  {
    if (paramLaunchDesc.isApplicationDescriptor())
    {
      File localFile = getTempCacheFile(paramURL, null);
      FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
      try
      {
        localFileOutputStream.write(paramLaunchDesc.getSource().getBytes());
      }
      finally
      {
        localFileOutputStream.close();
      }
      insertEntry('A', paramURL, null, localFile, new Date().getTime());
    }
  }
  
  public static void uninstallActiveCache()
  {
    _activeCache.uninstallCache();
  }
  
  public static long getMuffinSize(URL paramURL)
    throws IOException
  {
    return _muffincache.getMuffinSize(paramURL);
  }
  
  public static long[] getMuffinAttributes(URL paramURL)
    throws IOException
  {
    return _muffincache.getMuffinAttributes(paramURL);
  }
  
  public static void putMuffinAttributes(URL paramURL, int paramInt, long paramLong)
    throws IOException
  {
    _muffincache.putMuffinAttributes(paramURL, paramInt, paramLong);
  }
  
  public static URL[] getAccessibleMuffins(URL paramURL)
    throws IOException
  {
    return _muffincache.getAccessibleMuffins(paramURL);
  }
  
  public static void insertMuffinEntry(URL paramURL, File paramFile, int paramInt, long paramLong)
    throws IOException
  {
    _muffincache.insertMuffinEntry(paramURL, paramFile, paramInt, paramLong);
  }
  
  public static File getMuffinFileForURL(URL paramURL)
  {
    return _muffincache.getMuffinFileForURL(paramURL);
  }
  
  public static DiskCacheEntry getMuffinEntry(char paramChar, URL paramURL)
    throws IOException
  {
    return _muffincache.getMuffinEntry(paramChar, paramURL);
  }
  
  public static boolean isMainMuffinFile(File paramFile)
    throws IOException
  {
    return _muffincache.isMainMuffinFile(paramFile);
  }
  
  public static void removeMuffinEntry(DiskCacheEntry paramDiskCacheEntry)
  {
    _muffincache.removeMuffinEntry(paramDiskCacheEntry);
  }
  
  public static void uninstallMuffinCache()
  {
    _muffincache.uninstallCache();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\cache\Cache.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */