package com.sun.javaws;

import com.sun.deploy.util.Trace;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

public class Globals
{
  private static final String JAVAWS_NAME = "javaws-1.5.0_04";
  public static final String JAVAWS_VERSION = "1.5.0_04";
  private static final String JNLP_VERSION = "1.5";
  private static final String WIN_ID = "Windows";
  private static boolean _isOffline = false;
  private static boolean _isImportMode = false;
  private static boolean _isSilentMode = false;
  private static boolean _isInstallMode = false;
  private static boolean _isSystemCache = false;
  private static boolean _isSecureMode = false;
  private static String _codebaseOverride = null;
  private static String[] _applicationArgs = null;
  private static boolean _createShortcut = false;
  private static boolean _createAssoc = false;
  private static URL _codebase = null;
  private static final String DEFAULT_LOGHOST = "localhost:8205";
  public static String BootClassPath = "NONE";
  public static String JCOV = "NONE";
  public static boolean TraceDefault = true;
  public static boolean TraceBasic = false;
  public static boolean TraceNetwork = false;
  public static boolean TraceSecurity = false;
  public static boolean TraceCache = false;
  public static boolean TraceExtensions = false;
  public static boolean TraceTemp = false;
  public static String LogToHost = null;
  public static boolean SupportJREinstallation = true;
  public static boolean OverrideSystemClassLoader = true;
  public static boolean TCKHarnessRun = false;
  public static boolean TCKResponse = false;
  public static final String JAVA_STARTED = "Java Started";
  public static final String JNLP_LAUNCHING = "JNLP Launching";
  public static final String NEW_VM_STARTING = "JVM Starting";
  public static final String JAVA_SHUTDOWN = "JVM Shutdown";
  public static final String CACHE_CLEAR_OK = "Cache Clear Success";
  public static final String CACHE_CLEAR_FAILED = "Cache Clear Failed";
  private static final Locale defaultLocale = Locale.getDefault();
  private static final String defaultLocaleString = getDefaultLocale().toString();
  
  public static String getDefaultLocaleString()
  {
    return defaultLocaleString;
  }
  
  public static Locale getDefaultLocale()
  {
    return defaultLocale;
  }
  
  public static boolean isOffline()
  {
    return _isOffline;
  }
  
  public static boolean createShortcut()
  {
    return _createShortcut;
  }
  
  public static boolean createAssoc()
  {
    return _createAssoc;
  }
  
  public static boolean isImportMode()
  {
    return _isImportMode;
  }
  
  public static boolean isInstallMode()
  {
    return _isInstallMode;
  }
  
  public static boolean isSilentMode()
  {
    return (_isSilentMode) && ((_isImportMode) || (_isInstallMode));
  }
  
  public static boolean isSystemCache()
  {
    return _isSystemCache;
  }
  
  public static boolean isSecureMode()
  {
    return _isSecureMode;
  }
  
  public static String getCodebaseOverride()
  {
    return _codebaseOverride;
  }
  
  public static String[] getApplicationArgs()
  {
    return _applicationArgs;
  }
  
  public static URL getCodebase()
  {
    return _codebase;
  }
  
  public static void setCodebase(URL paramURL)
  {
    _codebase = paramURL;
  }
  
  public static void setCreateShortcut(boolean paramBoolean)
  {
    _createShortcut = paramBoolean;
  }
  
  public static void setCreateAssoc(boolean paramBoolean)
  {
    _createAssoc = paramBoolean;
  }
  
  public static void setOffline(boolean paramBoolean)
  {
    _isOffline = paramBoolean;
  }
  
  public static void setImportMode(boolean paramBoolean)
  {
    _isImportMode = paramBoolean;
  }
  
  public static void setSilentMode(boolean paramBoolean)
  {
    _isSilentMode = paramBoolean;
  }
  
  public static void setInstallMode(boolean paramBoolean)
  {
    _isInstallMode = paramBoolean;
  }
  
  public static void setSystemCache(boolean paramBoolean)
  {
    _isSystemCache = paramBoolean;
  }
  
  public static void setSecureMode(boolean paramBoolean)
  {
    _isSecureMode = paramBoolean;
  }
  
  public static void setCodebaseOverride(String paramString)
  {
    if ((paramString != null) && (!paramString.endsWith(File.separator))) {
      paramString = paramString + File.separator;
    }
    _codebaseOverride = paramString;
  }
  
  public static void setApplicationArgs(String[] paramArrayOfString)
  {
    _applicationArgs = paramArrayOfString;
  }
  
  public static boolean isHeadless()
  {
    if (!isJavaVersionAtLeast14()) {
      return false;
    }
    return GraphicsEnvironment.isHeadless();
  }
  
  public static boolean havePack200()
  {
    return isJavaVersionAtLeast15();
  }
  
  private static final String _javaVersionProperty = System.getProperty("java.version");
  private static final boolean _atLeast14 = (!_javaVersionProperty.startsWith("1.2")) && (!_javaVersionProperty.startsWith("1.3"));
  private static final boolean _atLeast15 = (_atLeast14) && (!_javaVersionProperty.startsWith("1.4"));
  
  public static boolean isJavaVersionAtLeast15()
  {
    return _atLeast15;
  }
  
  public static boolean isJavaVersionAtLeast14()
  {
    return _atLeast14;
  }
  
  public static String getBuildID()
  {
    String str = null;
    InputStream localInputStream = Globals.class.getResourceAsStream("/build.id");
    if (localInputStream != null)
    {
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream));
      try
      {
        str = localBufferedReader.readLine();
      }
      catch (IOException localIOException) {}
    }
    return (str == null) || (str.length() == 0) ? "<internal>" : str;
  }
  
  public static String getJavaVersion()
  {
    return _javaVersionProperty;
  }
  
  public static String getComponentName()
  {
    return "javaws-1.5.0_04";
  }
  
  public static String getUserAgent()
  {
    return "JNLP/1.5 javaws/1.5.0_04 (" + getBuildID() + ")" + " J2SE/" + System.getProperty("java.version");
  }
  
  public static String[] parseOptions(String[] paramArrayOfString)
  {
    readOptionFile();
    
    ArrayList localArrayList = new ArrayList();
    
    int i = 0;
    int j = 0;
    while (i < paramArrayOfString.length)
    {
      localObject = paramArrayOfString[(i++)];
      if ((((String)localObject).startsWith("-XX:")) && (j == 0)) {
        parseOption(((String)localObject).substring(4), false);
      } else {
        localArrayList.add(localObject);
      }
      if (!((String)localObject).startsWith("-")) {
        j = 1;
      }
    }
    setTCKOptions();
    Object localObject = new String[localArrayList.size()];
    return (String[])localArrayList.toArray((Object[])localObject);
  }
  
  public static void getDebugOptionsFromProperties(Properties paramProperties)
  {
    int i = 0;
    for (;;)
    {
      String str = paramProperties.getProperty("javaws.debug." + i);
      if (str == null) {
        return;
      }
      parseOption(str, true);
      i++;
    }
  }
  
  private static void setTCKOptions()
  {
    if ((TCKHarnessRun == true) && 
      (LogToHost == null)) {
      Trace.println("Warning: LogHost = null");
    }
  }
  
  private static void parseOption(String paramString, boolean paramBoolean)
  {
    String str1 = null;
    String str2 = null;
    
    int i = paramString.indexOf('=');
    if (i == -1)
    {
      str1 = paramString;
      str2 = null;
    }
    else
    {
      str1 = paramString.substring(0, i);
      str2 = paramString.substring(i + 1);
    }
    if ((str1.length() > 0) && ((str1.startsWith("-")) || (str1.startsWith("+"))))
    {
      str1 = str1.substring(1);
      str2 = paramString.startsWith("+") ? "true" : "false";
    }
    if ((paramBoolean) && (!str1.startsWith("x")) && (!str1.startsWith("Trace"))) {
      str1 = null;
    }
    if ((str1 != null) && (setOption(str1, str2))) {
      System.out.println("# Option: " + str1 + "=" + str2);
    } else {
      System.out.println("# Ignoring option: " + paramString);
    }
  }
  
  private static boolean setOption(String paramString1, String paramString2)
  {
    Class localClass1 = new String().getClass();
    boolean bool = true;
    try
    {
      Field localField = new Globals().getClass().getDeclaredField(paramString1);
      if ((localField.getModifiers() & 0x8) == 0) {
        return false;
      }
      Class localClass2 = localField.getType();
      if (localClass2 == localClass1) {
        localField.set(null, paramString2);
      } else if (localClass2 == Boolean.TYPE) {
        localField.setBoolean(null, Boolean.valueOf(paramString2).booleanValue());
      } else if (localClass2 == Integer.TYPE) {
        localField.setInt(null, Integer.parseInt(paramString2));
      } else if (localClass2 == Float.TYPE) {
        localField.setFloat(null, Float.parseFloat(paramString2));
      } else if (localClass2 == Double.TYPE) {
        localField.setDouble(null, Double.parseDouble(paramString2));
      } else if (localClass2 == Long.TYPE) {
        localField.setLong(null, Long.parseLong(paramString2));
      } else {
        return false;
      }
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      return false;
    }
    catch (NoSuchFieldException localNoSuchFieldException)
    {
      return false;
    }
    return bool;
  }
  
  private static void readOptionFile()
  {
    FileInputStream localFileInputStream = null;
    try
    {
      localFileInputStream = new FileInputStream(".javawsrc");
    }
    catch (FileNotFoundException localFileNotFoundException1)
    {
      try
      {
        localFileInputStream = new FileInputStream(System.getProperty("user.home") + File.separator + ".javawsrc");
      }
      catch (FileNotFoundException localFileNotFoundException2)
      {
        return;
      }
    }
    try
    {
      Properties localProperties = new Properties();
      localProperties.load(localFileInputStream);
      
      Enumeration localEnumeration = localProperties.propertyNames();
      if (localEnumeration.hasMoreElements()) {
        System.out.println("\nSetting options from .javawsrc file:");
      }
      while (localEnumeration.hasMoreElements())
      {
        String str1 = (String)localEnumeration.nextElement();
        String str2 = localProperties.getProperty(str1);
        parseOption(str1 + "=" + str2, false);
      }
    }
    catch (IOException localIOException) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\Globals.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */