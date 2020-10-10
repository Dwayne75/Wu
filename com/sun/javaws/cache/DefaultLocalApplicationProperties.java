package com.sun.javaws.cache;

import com.sun.deploy.util.Trace;
import com.sun.javaws.LocalApplicationProperties;
import com.sun.javaws.jnl.AssociationDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ShortcutDesc;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class DefaultLocalApplicationProperties
  implements LocalApplicationProperties
{
  private static final String REBOOT_NEEDED_KEY = "_default.rebootNeeded";
  private static final String UPDATE_CHECK_KEY = "_default.forcedUpdateCheck";
  private static final String NATIVELIB_DIR_KEY = "_default.nativeLibDir";
  private static final String INSTALL_DIR_KEY = "_default.installDir";
  private static final String LAST_ACCESSED_KEY = "_default.lastAccessed";
  private static final String LAUNCH_COUNT_KEY = "_default.launchCount";
  private static final String ASK_INSTALL_KEY = "_default.askedInstall";
  private static final String SHORTCUT_KEY = "_default.locallyInstalled";
  private static final String INDIRECT_PATH_KEY = "_default.indirectPath";
  private static final String ASSOCIATION_MIME_KEY = "_default.mime.types.";
  private static final String REGISTERED_TITLE_KEY = "_default.title";
  private static final String ASSOCIATION_EXTENSIONS_KEY = "_default.extensions.";
  private static final DateFormat _df = ;
  private LaunchDesc _descriptor;
  private Properties _properties;
  private URL _location;
  private String _versionId;
  private long _lastAccessed;
  private boolean _isApplicationDescriptor;
  private boolean _dirty;
  private boolean _isLocallyInstalledSystem;
  
  public DefaultLocalApplicationProperties(URL paramURL, String paramString, LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    this._descriptor = paramLaunchDesc;
    this._location = paramURL;
    this._versionId = paramString;
    this._isApplicationDescriptor = paramBoolean;
    this._properties = getLocalApplicationPropertiesStorage(this);
    this._isLocallyInstalledSystem = false;
  }
  
  public URL getLocation()
  {
    return this._location;
  }
  
  public String getVersionId()
  {
    return this._versionId;
  }
  
  public LaunchDesc getLaunchDescriptor()
  {
    return this._descriptor;
  }
  
  public void setLastAccessed(Date paramDate)
  {
    put("_default.lastAccessed", _df.format(paramDate));
  }
  
  public Date getLastAccessed()
  {
    return getDate("_default.lastAccessed");
  }
  
  public void incrementLaunchCount()
  {
    int i = getLaunchCount();
    
    put("_default.launchCount", Integer.toString(++i));
  }
  
  public int getLaunchCount()
  {
    return getInteger("_default.launchCount");
  }
  
  public void setAskedForInstall(boolean paramBoolean)
  {
    put("_default.askedInstall", new Boolean(paramBoolean).toString());
  }
  
  public boolean getAskedForInstall()
  {
    return getBoolean("_default.askedInstall");
  }
  
  public void setRebootNeeded(boolean paramBoolean)
  {
    put("_default.rebootNeeded", new Boolean(paramBoolean).toString());
  }
  
  public boolean isRebootNeeded()
  {
    return getBoolean("_default.rebootNeeded");
  }
  
  public void setLocallyInstalled(boolean paramBoolean)
  {
    put("_default.locallyInstalled", new Boolean(paramBoolean).toString());
  }
  
  public boolean isLocallyInstalled()
  {
    return getBoolean("_default.locallyInstalled");
  }
  
  public boolean isLocallyInstalledSystem()
  {
    return this._isLocallyInstalledSystem;
  }
  
  public boolean forceUpdateCheck()
  {
    return getBoolean("_default.forcedUpdateCheck");
  }
  
  public void setForceUpdateCheck(boolean paramBoolean)
  {
    put("_default.forcedUpdateCheck", new Boolean(paramBoolean).toString());
  }
  
  public boolean isApplicationDescriptor()
  {
    return this._isApplicationDescriptor;
  }
  
  public boolean isExtensionDescriptor()
  {
    return !this._isApplicationDescriptor;
  }
  
  public String getInstallDirectory()
  {
    return get("_default.installDir");
  }
  
  public void setInstallDirectory(String paramString)
  {
    put("_default.installDir", paramString);
  }
  
  public String getNativeLibDirectory()
  {
    return get("_default.nativeLibDir");
  }
  
  public String getRegisteredTitle()
  {
    return get("_default.title");
  }
  
  public void setRegisteredTitle(String paramString)
  {
    put("_default.title", paramString);
  }
  
  public void setNativeLibDirectory(String paramString)
  {
    put("_default.nativeLibDir", paramString);
  }
  
  public void setAssociations(AssociationDesc[] paramArrayOfAssociationDesc)
  {
    int i = 0;
    if (paramArrayOfAssociationDesc == null)
    {
      AssociationDesc[] arrayOfAssociationDesc = getAssociations();
      if (arrayOfAssociationDesc != null)
      {
        put("_default.mime.types." + i, null);
        put("_default.extensions." + i, null);
      }
    }
    else
    {
      for (i = 0; i < paramArrayOfAssociationDesc.length; i++)
      {
        put("_default.mime.types." + i, paramArrayOfAssociationDesc[i].getMimeType());
        
        put("_default.extensions." + i, paramArrayOfAssociationDesc[i].getExtensions());
      }
      put("_default.mime.types." + i, null);
      put("_default.extensions." + i, null);
    }
  }
  
  public void addAssociation(AssociationDesc paramAssociationDesc)
  {
    AssociationDesc[] arrayOfAssociationDesc2 = getAssociations();
    int i = 0;
    AssociationDesc[] arrayOfAssociationDesc1;
    if (arrayOfAssociationDesc2 == null)
    {
      arrayOfAssociationDesc1 = new AssociationDesc[1];
    }
    else
    {
      arrayOfAssociationDesc1 = new AssociationDesc[arrayOfAssociationDesc2.length + 1];
      while (i < arrayOfAssociationDesc2.length)
      {
        arrayOfAssociationDesc1[i] = arrayOfAssociationDesc2[i];
        i++;
      }
    }
    arrayOfAssociationDesc1[i] = paramAssociationDesc;
    setAssociations(arrayOfAssociationDesc1);
  }
  
  public AssociationDesc[] getAssociations()
  {
    ArrayList localArrayList = new ArrayList();
    for (int i = 0;; i++)
    {
      String str1 = get("_default.mime.types." + i);
      String str2 = get("_default.extensions." + i);
      if ((str1 == null) && (str2 == null)) {
        break;
      }
      localArrayList.add(new AssociationDesc(str2, str1));
    }
    return (AssociationDesc[])localArrayList.toArray(new AssociationDesc[0]);
  }
  
  public void put(String paramString1, String paramString2)
  {
    synchronized (this)
    {
      if (paramString2 == null) {
        this._properties.remove(paramString1);
      } else {
        this._properties.put(paramString1, paramString2);
      }
      this._dirty = true;
    }
  }
  
  /* Error */
  public String get(String paramString)
  {
    // Byte code:
    //   0: aload_0
    //   1: dup
    //   2: astore_2
    //   3: monitorenter
    //   4: aload_0
    //   5: getfield 276	com/sun/javaws/cache/DefaultLocalApplicationProperties:_properties	Ljava/util/Properties;
    //   8: aload_1
    //   9: invokevirtual 328	java/util/Properties:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   12: checkcast 163	java/lang/String
    //   15: aload_2
    //   16: monitorexit
    //   17: areturn
    //   18: astore_3
    //   19: aload_2
    //   20: monitorexit
    //   21: aload_3
    //   22: athrow
    // Line number table:
    //   Java source line #273	-> byte code offset #0
    //   Java source line #274	-> byte code offset #4
    //   Java source line #275	-> byte code offset #18
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	23	0	this	DefaultLocalApplicationProperties
    //   0	23	1	paramString	String
    //   2	18	2	Ljava/lang/Object;	Object
    //   18	4	3	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   4	17	18	finally
    //   18	21	18	finally
  }
  
  public int getInteger(String paramString)
  {
    String str = get(paramString);
    if (str == null) {
      return 0;
    }
    int i = 0;
    try
    {
      i = Integer.parseInt(str);
    }
    catch (NumberFormatException localNumberFormatException)
    {
      i = 0;
    }
    return i;
  }
  
  public boolean getBoolean(String paramString)
  {
    String str = get(paramString);
    if (str == null) {
      return false;
    }
    return Boolean.valueOf(str).booleanValue();
  }
  
  public Date getDate(String paramString)
  {
    String str = get(paramString);
    if (str == null) {
      return null;
    }
    try
    {
      return _df.parse(str);
    }
    catch (ParseException localParseException) {}
    return null;
  }
  
  public boolean doesNewVersionExist()
  {
    synchronized (this)
    {
      long l = Cache.getLastAccessed();
      if (l == 0L) {
        return false;
      }
      if (l > this._lastAccessed) {
        return true;
      }
    }
    return false;
  }
  
  public synchronized void store()
    throws IOException
  {
    putLocalApplicationPropertiesStorage(this, this._properties);
    this._dirty = false;
  }
  
  public void refreshIfNecessary()
  {
    synchronized (this)
    {
      if ((!this._dirty) && (doesNewVersionExist())) {
        refresh();
      }
    }
  }
  
  public void refresh()
  {
    synchronized (this)
    {
      Properties localProperties = getLocalApplicationPropertiesStorage(this);
      this._properties = localProperties;
      this._dirty = false;
    }
  }
  
  public boolean isShortcutSupported()
  {
    DiskCacheEntry localDiskCacheEntry = null;
    try
    {
      localDiskCacheEntry = Cache.getCacheEntry('A', this._location, null);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
      return false;
    }
    if ((localDiskCacheEntry == null) || (localDiskCacheEntry.isEmpty())) {
      return false;
    }
    ShortcutDesc localShortcutDesc = this._descriptor.getInformation().getShortcut();
    return (localShortcutDesc == null) || (localShortcutDesc.getDesktop()) || (localShortcutDesc.getMenu());
  }
  
  private Properties getLocalApplicationPropertiesStorage(DefaultLocalApplicationProperties paramDefaultLocalApplicationProperties)
  {
    Properties localProperties = new Properties();
    try
    {
      URL localURL = paramDefaultLocalApplicationProperties.getLocation();
      String str1 = paramDefaultLocalApplicationProperties.getVersionId();
      if (localURL != null)
      {
        char c = paramDefaultLocalApplicationProperties.isApplicationDescriptor() ? 'A' : 'E';
        
        byte[] arrayOfByte = Cache.getLapData(c, localURL, str1, true);
        if (arrayOfByte != null)
        {
          localProperties.load(new ByteArrayInputStream(arrayOfByte));
          String str2 = (String)localProperties.get("_default.locallyInstalled");
          if (str2 != null) {
            this._isLocallyInstalledSystem = Boolean.valueOf(str2).booleanValue();
          }
        }
        arrayOfByte = Cache.getLapData(c, localURL, str1, false);
        if (arrayOfByte != null) {
          localProperties.load(new ByteArrayInputStream(arrayOfByte));
        }
      }
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    return localProperties;
  }
  
  private void putLocalApplicationPropertiesStorage(DefaultLocalApplicationProperties paramDefaultLocalApplicationProperties, Properties paramProperties)
    throws IOException
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    try
    {
      paramProperties.store(localByteArrayOutputStream, "LAP");
    }
    catch (IOException localIOException) {}
    localByteArrayOutputStream.close();
    char c = paramDefaultLocalApplicationProperties.isApplicationDescriptor() ? 'A' : 'E';
    
    Cache.putLapData(c, paramDefaultLocalApplicationProperties.getLocation(), paramDefaultLocalApplicationProperties.getVersionId(), localByteArrayOutputStream.toByteArray());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\cache\DefaultLocalApplicationProperties.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */