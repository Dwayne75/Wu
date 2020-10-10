package com.sun.javaws;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.net.cookie.DeployCookieSelector;
import com.sun.deploy.net.proxy.DeployProxySelector;
import com.sun.deploy.net.proxy.NSPreferences;
import com.sun.deploy.net.proxy.StaticProxyManager;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.ConsoleController;
import com.sun.deploy.util.ConsoleHelper;
import com.sun.deploy.util.ConsoleTraceListener;
import com.sun.deploy.util.ConsoleWindow;
import com.sun.deploy.util.DialogFactory;
import com.sun.deploy.util.FileTraceListener;
import com.sun.deploy.util.LoggerTraceListener;
import com.sun.deploy.util.PerfLogger;
import com.sun.deploy.util.SocketTraceListener;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.deploy.util.TraceListener;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.exceptions.CacheAccessException;
import com.sun.javaws.exceptions.CouldNotLoadArgumentException;
import com.sun.javaws.exceptions.FailedDownloadingResourceException;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.exceptions.LaunchDescException;
import com.sun.javaws.exceptions.TooManyArgumentsException;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.security.AppContextUtil;
import com.sun.javaws.ui.CacheViewer;
import com.sun.javaws.util.JavawsConsoleController;
import com.sun.javaws.util.JavawsDialogListener;
import com.sun.jnlp.JNLPClassLoader;
import com.sun.jnlp.JnlpLookupStub;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLException;

public class Main
{
  private static boolean _isViewer = false;
  private static boolean _launchingAllowed = false;
  private static ThreadGroup _systemTG;
  private static ThreadGroup _securityTG;
  private static ThreadGroup _launchTG;
  private static String[] _tempfile = new String[1];
  private static DataInputStream _tckStream = null;
  private static long t0;
  private static long t1;
  private static long t2;
  private static long t3;
  private static long t4;
  private static long t5;
  private static boolean _timeing = true;
  private static boolean uninstall = false;
  
  public static void main(String[] paramArrayOfString)
  {
    PerfLogger.setStartTime("JavaWebStart main started");
    
    Thread.currentThread().setContextClassLoader(JNLPClassLoader.createClassLoader());
    if (_timeing) {
      t0 = new Date().getTime();
    }
    Toolkit.getDefaultToolkit();
    if (_timeing) {
      t1 = new Date().getTime();
    }
    _launchingAllowed = Config.isConfigValid();
    if (_timeing) {
      t2 = new Date().getTime();
    }
    JPDA.setup();
    
    paramArrayOfString = Globals.parseOptions(paramArrayOfString);
    if (_timeing) {
      t3 = new Date().getTime();
    }
    initTrace();
    if (_timeing) {
      t4 = new Date().getTime();
    }
    updateCache();
    
    paramArrayOfString = parseArgs(paramArrayOfString);
    if (paramArrayOfString.length > 0) {
      _tempfile[0] = paramArrayOfString[0];
    }
    if (Cache.canWrite())
    {
      setupBrowser();
      
      JnlpxArgs.verify();
      
      initializeExecutionEnvironment();
      if (uninstall) {
        uninstallCache(paramArrayOfString.length > 0 ? paramArrayOfString[0] : null);
      }
      if (Globals.TCKHarnessRun) {
        tckprintln("Java Started");
      }
      if (paramArrayOfString.length == 0) {
        _isViewer = true;
      }
      if (!_isViewer) {
        launchApp(paramArrayOfString, true);
      }
      if (_isViewer)
      {
        JnlpxArgs.removeArgumentFile(paramArrayOfString);
        try
        {
          if (_timeing)
          {
            t5 = new Date().getTime();
            Trace.println("startup times: \n      toolkit: " + (t1 - t0) + "\n" + "       config: " + (t2 - t1) + "\n" + "         args: " + (t3 - t2) + "\n" + "        trace: " + (t4 - t3) + "\n" + "     the rest: " + (t5 - t4) + "\n" + "", TraceLevel.BASIC);
          }
          Trace.println("Launching Cache Viewer", TraceLevel.BASIC);
          
          Trace.flush();
          CacheViewer.main(paramArrayOfString);
        }
        catch (Exception localException)
        {
          LaunchErrorDialog.show(null, localException, true);
        }
      }
    }
    else
    {
      LaunchErrorDialog.show(null, new CacheAccessException(Globals.isSystemCache()), true);
    }
    Trace.flush();
  }
  
  public static void launchApp(String[] paramArrayOfString, boolean paramBoolean)
  {
    if (paramArrayOfString.length > 1)
    {
      JnlpxArgs.removeArgumentFile(paramArrayOfString);
      LaunchErrorDialog.show(null, new TooManyArgumentsException(paramArrayOfString), paramBoolean);
      
      return;
    }
    LaunchDesc localLaunchDesc = null;
    try
    {
      localLaunchDesc = LaunchDescFactory.buildDescriptor(paramArrayOfString[0]);
    }
    catch (IOException localIOException)
    {
      Object localObject = null;
      JnlpxArgs.removeArgumentFile(paramArrayOfString);
      
      localObject = new CouldNotLoadArgumentException(paramArrayOfString[0], localIOException);
      if (Globals.isJavaVersionAtLeast14()) {
        if (((localIOException instanceof SSLException)) || ((localIOException.getMessage() != null) && (localIOException.getMessage().toLowerCase().indexOf("https") != -1))) {
          try
          {
            localObject = new FailedDownloadingResourceException(new URL(paramArrayOfString[0]), null, localIOException);
          }
          catch (MalformedURLException localMalformedURLException)
          {
            Trace.ignoredException(localMalformedURLException);
          }
        }
      }
      LaunchErrorDialog.show(null, (Throwable)localObject, paramBoolean);
      return;
    }
    catch (JNLPException localJNLPException)
    {
      JnlpxArgs.removeArgumentFile(paramArrayOfString);
      
      LaunchErrorDialog.show(null, localJNLPException, paramBoolean);
      return;
    }
    Globals.setCodebase(localLaunchDesc.getCodebase());
    if (localLaunchDesc.getLaunchType() == 5)
    {
      JnlpxArgs.removeArgumentFile(paramArrayOfString);
      String str = localLaunchDesc.getInternalCommand();
      if (str.equals("viewer"))
      {
        _isViewer = true;
      }
      else if (str.equals("player"))
      {
        _isViewer = true;
      }
      else
      {
        launchJavaControlPanel(str);
        systemExit(0);
      }
    }
    else if (_launchingAllowed)
    {
      new Launcher(localLaunchDesc).launch(paramArrayOfString, paramBoolean);
    }
    else
    {
      LaunchErrorDialog.show(null, new LaunchDescException(localLaunchDesc, ResourceManager.getString("enterprize.cfg.mandatory", Config.getEnterprizeString()), null), paramBoolean);
    }
  }
  
  public static void importApp(String paramString)
  {
    String[] arrayOfString = new String[1];
    arrayOfString[0] = paramString;
    Globals.setImportMode(true);
    Globals.setSilentMode(true);
    launchApp(arrayOfString, false);
    Launcher.checkCacheMax();
  }
  
  public static void launchJavaControlPanel(String paramString)
  {
    String[] arrayOfString = new String[7];
    String str = System.getProperty("javaplugin.user.profile");
    if (str == null) {
      str = "";
    }
    arrayOfString[0] = Config.getInstance().toExecArg(JREInfo.getDefaultJavaPath());
    arrayOfString[1] = "-cp";
    arrayOfString[2] = Config.getInstance().toExecArg(Config.getJavaHome() + File.separator + "lib" + File.separator + "deploy.jar");
    
    arrayOfString[3] = Config.getInstance().toExecArg("-Djavaplugin.user.profile=" + str);
    
    arrayOfString[4] = "com.sun.deploy.panel.ControlPanel";
    arrayOfString[5] = "-tab";
    arrayOfString[6] = (paramString == null ? "general" : paramString);
    Trace.println("Launching Control Panel: " + arrayOfString[0] + " " + arrayOfString[1] + " " + arrayOfString[2] + " " + arrayOfString[3] + " " + arrayOfString[4] + " " + arrayOfString[5] + " ", TraceLevel.BASIC);
    try
    {
      Runtime.getRuntime().exec(arrayOfString);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
  }
  
  private static void uninstallCache(String paramString)
  {
    int i = -1;
    try
    {
      i = uninstall(paramString);
    }
    catch (Exception localException)
    {
      LaunchErrorDialog.show(null, localException, !Globals.isSilentMode());
    }
    systemExit(i);
  }
  
  private static String[] parseArgs(String[] paramArrayOfString)
  {
    int j = 0;
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < paramArrayOfString.length; i++) {
      if (!paramArrayOfString[i].startsWith("-"))
      {
        localArrayList.add(paramArrayOfString[i]);
      }
      else if (paramArrayOfString[i].equals("-Xclearcache"))
      {
        try
        {
          Cache.remove();
          long l = Cache.getCacheSize();
          if (l > 0L)
          {
            System.err.println("Could not clean all entries  in cache since they are in use");
            if (Globals.TCKHarnessRun) {
              tckprintln("Cache Clear Failed");
            }
            systemExit(-1);
          }
        }
        catch (IOException localIOException)
        {
          Trace.println("Clear cached failed: " + localIOException.getMessage());
          if (Globals.TCKHarnessRun) {
            tckprintln("Cache Clear Failed");
          }
          systemExit(-1);
        }
        if (Globals.TCKHarnessRun) {
          tckprintln("Cache Clear Success");
        }
      }
      else if (paramArrayOfString[i].equals("-offline"))
      {
        JnlpxArgs.SetIsOffline();
        Globals.setOffline(true);
      }
      else if (!paramArrayOfString[i].equals("-online"))
      {
        if (!paramArrayOfString[i].equals("-Xnosplash")) {
          if (paramArrayOfString[i].equals("-installer"))
          {
            Globals.setInstallMode(true);
          }
          else if (paramArrayOfString[i].equals("-uninstall"))
          {
            uninstall = true;
            Globals.setInstallMode(true);
          }
          else if (paramArrayOfString[i].equals("-updateVersions"))
          {
            systemExit(0);
          }
          else if (paramArrayOfString[i].equals("-import"))
          {
            Globals.setImportMode(true);
            j = 1;
          }
          else if (paramArrayOfString[i].equals("-silent"))
          {
            Globals.setSilentMode(true);
          }
          else if (paramArrayOfString[i].equals("-shortcut"))
          {
            Globals.setCreateShortcut(true);
          }
          else if (paramArrayOfString[i].equals("-association"))
          {
            Globals.setCreateAssoc(true);
          }
          else if (paramArrayOfString[i].equals("-codebase"))
          {
            if (i + 1 < paramArrayOfString.length)
            {
              localObject = paramArrayOfString[(++i)];
              try
              {
                new URL((String)localObject);
              }
              catch (MalformedURLException localMalformedURLException)
              {
                LaunchErrorDialog.show(null, localMalformedURLException, true);
              }
              Globals.setCodebaseOverride((String)localObject);
            }
            j = 1;
          }
          else if (paramArrayOfString[i].equals("-system"))
          {
            Globals.setSystemCache(true);
            j = 1;
          }
          else if (paramArrayOfString[i].equals("-secure"))
          {
            Globals.setSecureMode(true);
          }
          else if ((paramArrayOfString[i].equals("-open")) || (paramArrayOfString[i].equals("-print")))
          {
            if (i + 1 < paramArrayOfString.length)
            {
              localObject = new String[2];
              localObject[0] = paramArrayOfString[(i++)];
              localObject[1] = paramArrayOfString[i];
              Globals.setApplicationArgs((String[])localObject);
            }
            j = 1;
          }
          else if (paramArrayOfString[i].equals("-viewer"))
          {
            _isViewer = true;
          }
          else
          {
            Trace.println("unsupported option: " + paramArrayOfString[i], TraceLevel.BASIC);
          }
        }
      }
    }
    Object localObject = new String[localArrayList.size()];
    for (int k = 0; k < localObject.length; k++) {
      localObject[k] = ((String)localArrayList.get(k));
    }
    return (String[])localObject;
  }
  
  private static void initTrace()
  {
    Trace.redirectStdioStderr();
    
    Trace.resetTraceLevel();
    
    Trace.setInitialTraceLevel();
    if (Globals.TraceBasic) {
      Trace.setBasicTrace(true);
    }
    if (Globals.TraceNetwork) {
      Trace.setNetTrace(true);
    }
    if (Globals.TraceCache) {
      Trace.setCacheTrace(true);
    }
    if (Globals.TraceSecurity) {
      Trace.setSecurityTrace(true);
    }
    if (Globals.TraceExtensions) {
      Trace.setExtTrace(true);
    }
    if (Globals.TraceTemp) {
      Trace.setTempTrace(true);
    }
    Object localObject3;
    if ((Config.getProperty("deployment.console.startup.mode").equals("SHOW")) && (!Globals.isHeadless()))
    {
      localObject1 = JavawsConsoleController.getInstance();
      
      localObject2 = new ConsoleTraceListener((ConsoleController)localObject1);
      
      localObject3 = ConsoleWindow.create((ConsoleController)localObject1);
      ((JavawsConsoleController)localObject1).setConsole((ConsoleWindow)localObject3);
      if (localObject2 != null)
      {
        ((ConsoleTraceListener)localObject2).setConsole((ConsoleWindow)localObject3);
        Trace.addTraceListener((TraceListener)localObject2);
        ((ConsoleTraceListener)localObject2).print(ConsoleHelper.displayVersion() + "\n");
        ((ConsoleTraceListener)localObject2).print(ConsoleHelper.displayHelp());
      }
    }
    Object localObject1 = initSocketTrace();
    if (localObject1 != null) {
      Trace.addTraceListener((TraceListener)localObject1);
    }
    Object localObject2 = initFileTrace();
    if (localObject2 != null) {
      Trace.addTraceListener((TraceListener)localObject2);
    }
    if ((Globals.isJavaVersionAtLeast14()) && (Config.getBooleanProperty("deployment.log")))
    {
      localObject3 = null;
      try
      {
        localObject3 = Config.getProperty("deployment.javaws.logFileName");
        File localFile = new File(Config.getLogDirectory());
        if ((localObject3 != null) && (localObject3 != "")) {
          if (((String)localObject3).compareToIgnoreCase("TEMP") != 0)
          {
            localObject4 = new File((String)localObject3);
            if (((File)localObject4).isDirectory())
            {
              localObject3 = "";
            }
            else
            {
              localFile = ((File)localObject4).getParentFile();
              if (localFile != null) {
                localFile.mkdirs();
              }
            }
          }
          else
          {
            localObject3 = "";
          }
        }
        if (localObject3 == "")
        {
          localFile.mkdirs();
          
          localObject3 = Config.getLogDirectory() + File.separator + "javaws.log";
        }
        Object localObject4 = new LoggerTraceListener("com.sun.deploy", (String)localObject3);
        if (localObject4 != null)
        {
          ((LoggerTraceListener)localObject4).getLogger().setLevel(Level.ALL);
          JavawsConsoleController.getInstance().setLogger(((LoggerTraceListener)localObject4).getLogger());
          Trace.addTraceListener((TraceListener)localObject4);
        }
      }
      catch (Exception localException)
      {
        Trace.println("can not create log file in directory: " + Config.getLogDirectory(), TraceLevel.BASIC);
      }
    }
  }
  
  private static FileTraceListener initFileTrace()
  {
    if (Config.getBooleanProperty("deployment.trace"))
    {
      File localFile1 = null;
      String str1 = Config.getProperty("deployment.user.logdir");
      String str2 = Config.getProperty("deployment.javaws.traceFileName");
      try
      {
        if ((str2 != null) && (str2 != "") && (str2.compareToIgnoreCase("TEMP") != 0))
        {
          localFile1 = new File(str2);
          if (!localFile1.isDirectory())
          {
            int i = str2.lastIndexOf(File.separator);
            if (i != -1) {
              str1 = str2.substring(0, i);
            }
          }
          else
          {
            localFile1 = null;
          }
        }
        File localFile2 = new File(str1);
        localFile2.mkdirs();
        if (localFile1 == null) {
          localFile1 = File.createTempFile("javaws", ".trace", localFile2);
        }
        return new FileTraceListener(localFile1, true);
      }
      catch (Exception localException)
      {
        Trace.println("cannot create trace file in Directory: " + str1, TraceLevel.BASIC);
      }
    }
    return null;
  }
  
  private static SocketTraceListener initSocketTrace()
  {
    if (Globals.LogToHost != null)
    {
      String str1 = Globals.LogToHost;
      String str2 = null;
      int i = -1;
      
      int j = 0;
      int k = 0;
      if ((str1.charAt(0) == '[') && ((k = str1.indexOf(1, 93)) != -1)) {
        j = 1;
      } else {
        k = str1.indexOf(":");
      }
      str2 = str1.substring(j, k);
      if (str2 == null) {
        return null;
      }
      try
      {
        String str3 = str1.substring(str1.lastIndexOf(':') + 1);
        i = Integer.parseInt(str3);
      }
      catch (NumberFormatException localNumberFormatException)
      {
        i = -1;
      }
      if (i < 0) {
        return null;
      }
      SocketTraceListener localSocketTraceListener = new SocketTraceListener(str2, i);
      if (localSocketTraceListener != null)
      {
        Socket localSocket = localSocketTraceListener.getSocket();
        if ((Globals.TCKResponse) && (localSocket != null)) {
          try
          {
            _tckStream = new DataInputStream(localSocket.getInputStream());
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
        }
      }
      return localSocketTraceListener;
    }
    return null;
  }
  
  private static int uninstall(String paramString)
  {
    if (paramString == null)
    {
      Trace.println("Uninstall all!", TraceLevel.BASIC);
      uninstallAll();
      if (Globals.TCKHarnessRun) {
        tckprintln("Cache Clear Success");
      }
    }
    else
    {
      Trace.println("Uninstall: " + paramString, TraceLevel.BASIC);
      
      LaunchDesc localLaunchDesc = null;
      try
      {
        localLaunchDesc = LaunchDescFactory.buildDescriptor(paramString);
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
      catch (JNLPException localJNLPException)
      {
        Trace.ignoredException(localJNLPException);
      }
      if (localLaunchDesc != null)
      {
        LocalApplicationProperties localLocalApplicationProperties = null;
        if ((localLaunchDesc.isInstaller()) || (localLaunchDesc.isLibrary())) {
          localLocalApplicationProperties = Cache.getLocalApplicationProperties(paramString, localLaunchDesc);
        } else {
          localLocalApplicationProperties = Cache.getLocalApplicationProperties(localLaunchDesc.getCanonicalHome(), localLaunchDesc);
        }
        if (localLocalApplicationProperties != null)
        {
          Cache.remove(paramString, localLocalApplicationProperties, localLaunchDesc);
          Cache.clean();
          if (Globals.TCKHarnessRun) {
            tckprintln("Cache Clear Success");
          }
          return 0;
        }
      }
      Trace.println("Error uninstalling!", TraceLevel.BASIC);
      if (Globals.TCKHarnessRun) {
        tckprintln("Cache Clear Failed");
      }
      if (!Globals.isSilentMode())
      {
        SplashScreen.hide();
        DialogFactory.showErrorDialog(null, ResourceManager.getMessage("uninstall.failedMessage"), ResourceManager.getMessage("uninstall.failedMessageTitle"));
      }
    }
    return 0;
  }
  
  private static void uninstallAll() {}
  
  private static void setupBrowser()
  {
    if (Config.getBooleanProperty("deployment.capture.mime.types"))
    {
      setupNS6();
      setupOpera();
      
      Config.setBooleanProperty("deployment.capture.mime.types", false);
    }
  }
  
  private static void setupOpera()
  {
    OperaSupport localOperaSupport = BrowserSupport.getInstance().getOperaSupport();
    if ((localOperaSupport != null) && 
      (localOperaSupport.isInstalled())) {
      localOperaSupport.enableJnlp(new File(JREInfo.getDefaultJavaPath()), Config.getBooleanProperty("deployment.update.mime.types"));
    }
  }
  
  private static void setupNS6()
  {
    String str1 = null;
    
    str1 = BrowserSupport.getInstance().getNS6MailCapInfo();
    
    String str2 = "user_pref(\"browser.helperApps.neverAsk.openFile\", \"application%2Fx-java-jnlp-file\");\n";
    
    String str3 = System.getProperty("user.home");
    
    File localFile1 = new File(str3 + "/.mozilla/appreg");
    
    File localFile2 = null;
    try
    {
      localFile2 = NSPreferences.getNS6PrefsFile(localFile1);
    }
    catch (IOException localIOException1)
    {
      Trace.println("cannot determine NS6 prefs.js location", TraceLevel.BASIC);
    }
    if (localFile2 == null) {
      return;
    }
    FileInputStream localFileInputStream = null;
    try
    {
      String str4 = null;
      localFileInputStream = new FileInputStream(localFile2);
      
      localObject1 = new BufferedReader(new InputStreamReader(localFileInputStream));
      
      localObject2 = "";
      int i = 1;
      int j;
      if (str1 == null) {
        j = 0;
      } else {
        j = 1;
      }
      try
      {
        for (;;)
        {
          str4 = ((BufferedReader)localObject1).readLine();
          if (str4 == null)
          {
            localFileInputStream.close();
            break;
          }
          localObject2 = (String)localObject2 + str4 + "\n";
          if (str4.indexOf("x-java-jnlp-file") != -1) {
            i = 0;
          }
          if ((str1 != null) && (str4.indexOf(".mime.types") != -1)) {
            j = 0;
          }
        }
      }
      catch (IOException localIOException3)
      {
        Trace.ignoredException(localIOException3);
      }
      if ((i == 0) && (j == 0)) {
        return;
      }
      if (i != 0) {
        localObject2 = (String)localObject2 + str2;
      }
      if ((str1 != null) && (j != 0)) {
        localObject2 = (String)localObject2 + str1;
      }
      FileOutputStream localFileOutputStream = new FileOutputStream(localFile2);
      try
      {
        localFileOutputStream.write(((String)localObject2).getBytes());
        localFileOutputStream.close();
      }
      catch (IOException localIOException4)
      {
        Trace.ignoredException(localIOException4);
      }
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      Object localObject2;
      Trace.ignoredException(localFileNotFoundException);
      Object localObject1 = "";
      if (str1 != null) {
        localObject1 = (String)localObject1 + str1;
      }
      localObject1 = (String)localObject1 + str2;
      try
      {
        localObject2 = new FileOutputStream(localFile2);
        
        ((FileOutputStream)localObject2).write(((String)localObject1).getBytes());
        ((FileOutputStream)localObject2).close();
      }
      catch (IOException localIOException2)
      {
        Trace.ignoredException(localIOException2);
      }
    }
  }
  
  private static void updateCache()
  {
    if (Config.getProperty("deployment.javaws.cachedir") != null)
    {
      Cache.updateCache();
      Config.setProperty("deployment.javaws.cachedir", null);
      Config.storeIfDirty();
    }
  }
  
  private static void initializeExecutionEnvironment()
  {
    int i = Config.getOSName().indexOf("Windows") != -1 ? 1 : 0;
    boolean bool = Globals.isJavaVersionAtLeast15();
    if (i != 0)
    {
      if (bool) {
        com.sun.deploy.services.ServiceManager.setService(33024);
      } else {
        com.sun.deploy.services.ServiceManager.setService(16640);
      }
    }
    else if (bool) {
      com.sun.deploy.services.ServiceManager.setService(36864);
    } else {
      com.sun.deploy.services.ServiceManager.setService(20480);
    }
    Properties localProperties = System.getProperties();
    
    localProperties.put("http.auth.serializeRequests", "true");
    if (Globals.isJavaVersionAtLeast14())
    {
      String str = (String)localProperties.get("java.protocol.handler.pkgs");
      if (str != null) {
        localProperties.put("java.protocol.handler.pkgs", str + "|com.sun.deploy.net.protocol");
      } else {
        localProperties.put("java.protocol.handler.pkgs", "com.sun.deploy.net.protocol");
      }
    }
    localProperties.setProperty("javawebstart.version", Globals.getComponentName());
    try
    {
      DeployProxySelector.reset();
      
      DeployCookieSelector.reset();
    }
    catch (Throwable localThrowable)
    {
      StaticProxyManager.reset();
    }
    if (Config.getBooleanProperty("deployment.security.authenticator"))
    {
      JAuthenticator localJAuthenticator = JAuthenticator.getInstance((Frame)null);
      
      Authenticator.setDefault(localJAuthenticator);
    }
    javax.jnlp.ServiceManager.setServiceManagerStub(new JnlpLookupStub());
    
    addToSecurityProperty("package.access", "com.sun.javaws");
    addToSecurityProperty("package.access", "com.sun.deploy");
    addToSecurityProperty("package.definition", "com.sun.javaws");
    addToSecurityProperty("package.definition", "com.sun.deploy");
    addToSecurityProperty("package.definition", "com.sun.jnlp");
    
    addToSecurityProperty("package.access", "org.mozilla.jss");
    addToSecurityProperty("package.definition", "org.mozilla.jss");
    
    DialogFactory.addDialogListener(new JavawsDialogListener());
    if ((localProperties.get("https.protocols") == null) && (!Config.getBooleanProperty("deployment.security.TLSv1"))) {
      localProperties.put("https.protocols", "SSLv3,SSLv2Hello");
    }
  }
  
  private static void addToSecurityProperty(String paramString1, String paramString2)
  {
    String str = Security.getProperty(paramString1);
    
    Trace.println("property " + paramString1 + " value " + str, TraceLevel.SECURITY);
    if (str != null) {
      str = str + "," + paramString2;
    } else {
      str = paramString2;
    }
    Security.setProperty(paramString1, str);
    
    Trace.println("property " + paramString1 + " new value " + str, TraceLevel.SECURITY);
  }
  
  public static void systemExit(int paramInt)
  {
    JnlpxArgs.removeArgumentFile(_tempfile);
    SplashScreen.hide();
    Trace.flush();
    System.exit(paramInt);
  }
  
  static boolean isViewer()
  {
    return _isViewer;
  }
  
  public static final ThreadGroup getLaunchThreadGroup()
  {
    initializeThreadGroups();
    return _launchTG;
  }
  
  public static final ThreadGroup getSecurityThreadGroup()
  {
    initializeThreadGroups();
    return _securityTG;
  }
  
  private static void initializeThreadGroups()
  {
    if (_securityTG == null)
    {
      _systemTG = Thread.currentThread().getThreadGroup();
      while (_systemTG.getParent() != null) {
        _systemTG = _systemTG.getParent();
      }
      _securityTG = new ThreadGroup(_systemTG, "javawsSecurityThreadGroup");
      
      new Thread(_securityTG, new Runnable()
      {
        public void run() {}
      }).start();
      _launchTG = new ThreadGroup(_systemTG, "javawsApplicationThreadGroup");
    }
  }
  
  public static synchronized void tckprintln(String paramString)
  {
    long l = System.currentTimeMillis();
    Trace.println("##TCKHarnesRun##:" + l + ":" + Runtime.getRuntime().hashCode() + ":" + Thread.currentThread() + ":" + paramString);
    if (_tckStream != null) {
      try
      {
        while (_tckStream.readLong() < l) {}
      }
      catch (IOException localIOException)
      {
        System.err.println("Warning:Exceptions occurred, while logging to logSocket");
        
        localIOException.printStackTrace(System.err);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\Main.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */