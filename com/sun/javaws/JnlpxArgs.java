package com.sun.javaws;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.util.GeneralUtil;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

public class JnlpxArgs
{
  private static final String ARG_JVM = "jnlpx.jvm";
  private static final String ARG_SPLASHPORT = "jnlpx.splashport";
  private static final String ARG_REMOVE = "jnlpx.remove";
  private static final String ARG_OFFLINE = "jnlpx.offline";
  private static final String ARG_HEAPSIZE = "jnlpx.heapsize";
  private static final String ARG_VMARGS = "jnlpx.vmargs";
  private static final String ARG_HOME = "jnlpx.home";
  private static File _currentJVMCommand = null;
  private static final String JAVAWS_JAR = Config.isDebugMode() ? "javaws_g.jar" : "javaws.jar";
  private static final String DEPLOY_JAR = Config.isDebugMode() ? "deploy_g.jar" : "deploy.jar";
  
  public static int getSplashPort()
  {
    try
    {
      return Integer.parseInt(System.getProperty("jnlpx.splashport", "-1"));
    }
    catch (NumberFormatException localNumberFormatException) {}
    return -1;
  }
  
  public static String getVMArgs()
  {
    return System.getProperty("jnlpx.vmargs");
  }
  
  static File getJVMCommand()
  {
    if (_currentJVMCommand == null)
    {
      String str = System.getProperty("jnlpx.jvm", "").trim();
      if (str.startsWith("X")) {
        str = JREInfo.getDefaultJavaPath();
      }
      if (str.startsWith("\"")) {
        str = str.substring(1);
      }
      if (str.endsWith("\"")) {
        str = str.substring(0, str.length() - 1);
      }
      _currentJVMCommand = new File(str);
    }
    return _currentJVMCommand;
  }
  
  public static boolean shouldRemoveArgumentFile()
  {
    return getBooleanProperty("jnlpx.remove");
  }
  
  public static void setShouldRemoveArgumentFile(String paramString)
  {
    System.setProperty("jnlpx.remove", paramString);
  }
  
  public static boolean isOffline()
  {
    return getBooleanProperty("jnlpx.offline");
  }
  
  public static void SetIsOffline()
  {
    System.setProperty("jnlpx.offline", "true");
  }
  
  public static String getHeapSize()
  {
    return System.getProperty("jnlpx.heapsize");
  }
  
  public static long getInitialHeapSize()
  {
    String str1 = getHeapSize();
    if (str1 == null) {
      return -1L;
    }
    String str2 = str1.substring(str1.lastIndexOf('=') + 1);
    String str3 = str2.substring(0, str2.lastIndexOf(','));
    return GeneralUtil.heapValToLong(str3);
  }
  
  public static long getMaxHeapSize()
  {
    String str1 = getHeapSize();
    if (str1 == null) {
      return -1L;
    }
    String str2 = str1.substring(str1.lastIndexOf('=') + 1);
    String str3 = str2.substring(str2.lastIndexOf(',') + 1, str2.length());
    return GeneralUtil.heapValToLong(str3);
  }
  
  public static boolean isCurrentRunningJREHeap(long paramLong1, long paramLong2)
  {
    long l1 = getInitialHeapSize();
    long l2 = getMaxHeapSize();
    
    Trace.println("isCurrentRunningJREHeap: passed args: " + paramLong1 + ", " + paramLong2, TraceLevel.BASIC);
    Trace.println("JnlpxArgs is " + l1 + ", " + l2, TraceLevel.BASIC);
    
    return (l1 == paramLong1) && (l2 == paramLong2);
  }
  
  public static boolean isAuxArgsMatch(Properties paramProperties, String paramString)
  {
    String[] arrayOfString = Config.getSecureProperties();
    String str;
    for (int i = 0; i < arrayOfString.length; i++)
    {
      str = arrayOfString[i];
      if (paramProperties.containsKey(str))
      {
        Object localObject = paramProperties.get(str);
        if ((localObject != null) && (!localObject.equals(System.getProperty(str)))) {
          return false;
        }
      }
    }
    if ((paramString != null) && (getVMArgs() == null))
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString);
      while (localStringTokenizer.hasMoreTokens())
      {
        str = localStringTokenizer.nextToken();
        if (Config.isSecureVmArg(str)) {
          return false;
        }
      }
    }
    return true;
  }
  
  private static boolean heapSizesValid(long paramLong1, long paramLong2)
  {
    return (paramLong1 != -1L) || (paramLong2 != -1L);
  }
  
  public static String[] getArgumentList(String paramString1, long paramLong1, long paramLong2, Properties paramProperties, String paramString2)
  {
    String str1 = "-Djnlpx.heapsize=NULL,NULL";
    String str2 = "";
    String str3 = "";
    if (heapSizesValid(paramLong1, paramLong2))
    {
      str1 = "-Djnlpx.heapsize=" + paramLong1 + "," + paramLong2;
      if (paramLong1 > 0L) {
        str2 = "-Xms" + paramLong1;
      }
      if (paramLong2 > 0L) {
        str3 = "-Xmx" + paramLong2;
      }
    }
    String str4 = getDesiredVMArgs(getVMArgs(), paramString2);
    
    String[] arrayOfString1 = { "-Xbootclasspath/a:" + Config.getJavaHome() + File.separator + "lib" + File.separator + JAVAWS_JAR + File.pathSeparator + Config.getJavaHome() + File.separator + "lib" + File.separator + DEPLOY_JAR, "-classpath", File.pathSeparator + Config.getJavaHome() + File.separator + "lib" + File.separator + DEPLOY_JAR, str2, str3, str4 != null ? "-Djnlpx.vmargs=" + str4 : "", "-Djnlpx.jvm=" + paramString1, "-Djnlpx.splashport=" + getSplashPort(), "-Djnlpx.home=" + Config.getJavaHome() + File.separator + "bin", "-Djnlpx.remove=" + (shouldRemoveArgumentFile() ? "true" : "false"), "-Djnlpx.offline=" + (isOffline() ? "true" : "false"), str1, "-Djava.security.policy=" + getPolicyURLString(), "-DtrustProxy=true", "-Xverify:remote", useJCOV(), useBootClassPath(), useJpiProfile(), useDebugMode(), useDebugVMMode(), "com.sun.javaws.Main", setTCKHarnessOption(), useLogToHost() };
    
    int i = 0;
    for (int j = 0; j < arrayOfString1.length; j++) {
      if (!arrayOfString1[j].equals("")) {
        i++;
      }
    }
    String[] arrayOfString2 = getVMArgList(paramProperties, paramString2);
    int k = arrayOfString2.length;
    
    String[] arrayOfString3 = new String[i + k];
    int m = 0;
    for (m = 0; m < k; m++) {
      arrayOfString3[m] = arrayOfString2[m];
    }
    for (int n = 0; n < arrayOfString1.length; n++) {
      if (!arrayOfString1[n].equals("")) {
        arrayOfString3[(m++)] = arrayOfString1[n];
      }
    }
    return arrayOfString3;
  }
  
  static String getPolicyURLString()
  {
    String str1 = Config.getJavaHome() + File.separator + "lib" + File.separator + "security" + File.separator + "javaws.policy";
    
    String str2 = str1;
    try
    {
      URL localURL = new URL("file", "", str1);
      str2 = localURL.toString();
    }
    catch (Exception localException) {}
    return str2;
  }
  
  private static String getDesiredVMArgs(String paramString1, String paramString2)
  {
    if ((paramString1 == null) && 
      (paramString2 != null))
    {
      Object localObject = "";
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString2, " \t\n\r\f\"");
      while (localStringTokenizer.hasMoreTokens())
      {
        String str = localStringTokenizer.nextToken();
        if (Config.isSecureVmArg(str)) {
          if (((String)localObject).length() == 0) {
            localObject = str;
          } else {
            localObject = (String)localObject + " " + str;
          }
        }
      }
      if (((String)localObject).length() > 0) {
        return (String)localObject;
      }
    }
    return paramString1;
  }
  
  private static String[] getVMArgList(Properties paramProperties, String paramString)
  {
    Vector localVector = new Vector();
    String str1 = null;
    if ((str1 = getVMArgs()) != null)
    {
      localObject1 = new StringTokenizer(str1, " \t\n\r\f\"");
      while (((StringTokenizer)localObject1).hasMoreTokens()) {
        localVector.add(((StringTokenizer)localObject1).nextToken());
      }
    }
    if (paramString != null)
    {
      localObject1 = new StringTokenizer(paramString, " \t\n\r\f\"");
      while (((StringTokenizer)localObject1).hasMoreTokens())
      {
        String str2 = ((StringTokenizer)localObject1).nextToken();
        if ((Config.isSecureVmArg(str2)) && 
          (!localVector.contains(str2))) {
          localVector.add(str2);
        }
      }
    }
    Object localObject1 = Config.getSecureProperties();
    for (int i = 0; i < localObject1.length; i++)
    {
      Object localObject2 = localObject1[i];
      if (paramProperties.containsKey(localObject2))
      {
        String str3 = "-D" + (String)localObject2 + "=" + paramProperties.get(localObject2);
        if (!localVector.contains(str3)) {
          localVector.add(str3);
        }
      }
    }
    String[] arrayOfString = new String[localVector.size()];
    for (int j = 0; j < localVector.size(); j++) {
      arrayOfString[j] = new String((String)localVector.elementAt(j));
    }
    return arrayOfString;
  }
  
  public static String useLogToHost()
  {
    if (Globals.LogToHost != null) {
      return "-XX:LogToHost=" + Globals.LogToHost;
    }
    return "";
  }
  
  public static String setTCKHarnessOption()
  {
    if (Globals.TCKHarnessRun == true) {
      return "-XX:TCKHarnessRun=true";
    }
    return "";
  }
  
  public static String useBootClassPath()
  {
    if (Globals.BootClassPath.equals("NONE")) {
      return "";
    }
    return "-Xbootclasspath" + Globals.BootClassPath;
  }
  
  public static String useJpiProfile()
  {
    String str = System.getProperty("javaplugin.user.profile");
    if (str != null) {
      return "-Djavaplugin.user.profile=" + str;
    }
    return "";
  }
  
  public static String useJCOV()
  {
    if (Globals.JCOV.equals("NONE")) {
      return "";
    }
    return "-Xrunjcov:file=" + Globals.JCOV;
  }
  
  public static String useDebugMode()
  {
    if (Config.isDebugMode()) {
      return "-Ddeploy.debugMode=true";
    }
    return "";
  }
  
  public static String useDebugVMMode()
  {
    if (Config.isDebugVMMode()) {
      return "-Ddeploy.useDebugJavaVM=true";
    }
    return "";
  }
  
  public static void removeArgumentFile(String[] paramArrayOfString)
  {
    if ((shouldRemoveArgumentFile()) && (paramArrayOfString != null) && (paramArrayOfString.length > 0)) {
      new File(paramArrayOfString[0]).delete();
    }
  }
  
  static void verify()
  {
    Trace.println("Java part started", TraceLevel.BASIC);
    Trace.println("jnlpx.jvm: " + getJVMCommand(), TraceLevel.BASIC);
    Trace.println("jnlpx.splashport: " + getSplashPort(), TraceLevel.BASIC);
    Trace.println("jnlpx.remove: " + shouldRemoveArgumentFile(), TraceLevel.BASIC);
    Trace.println("jnlpx.heapsize: " + getHeapSize(), TraceLevel.BASIC);
  }
  
  private static boolean getBooleanProperty(String paramString)
  {
    String str = System.getProperty(paramString, "false");
    return (str != null) && (str.equals("true"));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\JnlpxArgs.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */