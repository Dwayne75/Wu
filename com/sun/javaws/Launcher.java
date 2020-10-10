package com.sun.javaws;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.si.SingleInstanceManager;
import com.sun.deploy.util.DialogFactory;
import com.sun.deploy.util.PerfLogger;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.cache.DiskCacheEntry;
import com.sun.javaws.cache.DownloadProtocol;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.exceptions.FailedDownloadingResourceException;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.exceptions.JreExecException;
import com.sun.javaws.exceptions.LaunchDescException;
import com.sun.javaws.exceptions.MissingFieldException;
import com.sun.javaws.exceptions.NoLocalJREException;
import com.sun.javaws.exceptions.OfflineLaunchException;
import com.sun.javaws.jnl.AppletDesc;
import com.sun.javaws.jnl.ApplicationDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.security.AppPolicy;
import com.sun.javaws.security.JavaWebStartSecurity;
import com.sun.javaws.ui.AutoDownloadPrompt;
import com.sun.javaws.ui.DownloadWindow;
import com.sun.javaws.util.JavawsConsoleController;
import com.sun.javaws.util.VersionID;
import com.sun.javaws.util.VersionString;
import com.sun.jnlp.AppletContainer;
import com.sun.jnlp.AppletContainerCallback;
import com.sun.jnlp.BasicServiceImpl;
import com.sun.jnlp.ExtensionInstallerServiceImpl;
import com.sun.jnlp.JNLPClassLoader;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Authenticator;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.net.ssl.SSLHandshakeException;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Launcher
  implements Runnable
{
  private DownloadWindow _downloadWindow = null;
  private LaunchDesc _launchDesc;
  private String[] _args;
  private boolean _exit = true;
  private JAuthenticator _ja;
  
  public Launcher(LaunchDesc paramLaunchDesc)
  {
    this._launchDesc = paramLaunchDesc;
    
    this._downloadWindow = new DownloadWindow();
    
    Trace.println("new Launcher: " + paramLaunchDesc.toString(), TraceLevel.BASIC);
  }
  
  public void launch(String[] paramArrayOfString, boolean paramBoolean)
  {
    this._args = paramArrayOfString;
    this._exit = paramBoolean;
    new Thread(Main.getLaunchThreadGroup(), this, "javawsApplicationMain").start();
  }
  
  private void removeTempJnlpFile(LaunchDesc paramLaunchDesc)
  {
    DiskCacheEntry localDiskCacheEntry = null;
    try
    {
      if (paramLaunchDesc.isApplicationDescriptor()) {
        localDiskCacheEntry = DownloadProtocol.getCachedLaunchedFile(paramLaunchDesc.getCanonicalHome());
      }
    }
    catch (JNLPException localJNLPException)
    {
      Trace.ignoredException(localJNLPException);
    }
    if (localDiskCacheEntry == null) {
      return;
    }
    File localFile = localDiskCacheEntry.getFile();
    if ((this._args != null) && (localFile != null) && (JnlpxArgs.shouldRemoveArgumentFile()))
    {
      new File(this._args[0]).delete();
      
      JnlpxArgs.setShouldRemoveArgumentFile(String.valueOf(false));
      
      this._args[0] = localFile.getPath();
    }
  }
  
  public void run()
  {
    LaunchDesc localLaunchDesc = this._launchDesc;
    
    boolean bool1 = LaunchDownload.updateLaunchDescInCache(localLaunchDesc);
    
    removeTempJnlpFile(localLaunchDesc);
    if (localLaunchDesc.getResources() != null) {
      Globals.getDebugOptionsFromProperties(localLaunchDesc.getResources().getResourceProperties());
    }
    if (Config.getBooleanProperty("deployment.security.authenticator"))
    {
      this._ja = JAuthenticator.getInstance(this._downloadWindow.getFrame());
      Authenticator.setDefault(this._ja);
    }
    int i = 0;
    int j = 0;
    boolean bool2 = Globals.isSilentMode();
    boolean bool3 = (Globals.isImportMode()) || (localLaunchDesc.getLaunchType() == 3);
    try
    {
      do
      {
        j = i == 3 ? 1 : 0;
        this._downloadWindow.setLaunchDesc(localLaunchDesc, true);
        localLaunchDesc = handleLaunchFile(localLaunchDesc, this._args, j == 0, bool3, bool2, bool1);
        
        i++;
        if (localLaunchDesc == null) {
          break;
        }
      } while (j == 0);
    }
    catch (ExitException localExitException)
    {
      int k = localExitException.getReason() == 0 ? 0 : -1;
      if (localExitException.getReason() == 2) {
        LaunchErrorDialog.show(this._downloadWindow == null ? null : this._downloadWindow.getFrame(), localExitException.getException(), this._exit);
      }
      if (this._exit) {
        Main.systemExit(k);
      }
    }
  }
  
  private LaunchDesc handleLaunchFile(LaunchDesc paramLaunchDesc, String[] paramArrayOfString, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4)
    throws ExitException
  {
    VersionString localVersionString = new VersionString(paramLaunchDesc.getSpecVersion());
    VersionID localVersionID = new VersionID("1.5");
    if (!localVersionString.contains(new VersionID("1.5"))) {
      if (!localVersionString.contains(new VersionID("1.0")))
      {
        JNLPException.setDefaultLaunchDesc(paramLaunchDesc);
        handleJnlpFileException(paramLaunchDesc, new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.badjnlversion", paramLaunchDesc.getSpecVersion()), null));
      }
    }
    if (paramLaunchDesc.getResources() == null) {
      handleJnlpFileException(paramLaunchDesc, new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.noappresources", paramLaunchDesc.getSpecVersion()), null));
    }
    if ((!paramBoolean2) && (!paramLaunchDesc.isLibrary()) && 
      (!paramLaunchDesc.isJRESpecified()))
    {
      LaunchDescException localLaunchDescException = new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.missingjreversion"), null);
      
      handleJnlpFileException(paramLaunchDesc, localLaunchDescException);
    }
    boolean bool = paramLaunchDesc.getLaunchType() == 4;
    
    return handleApplicationDesc(paramLaunchDesc, paramArrayOfString, bool, paramBoolean1, paramBoolean2, paramBoolean3, paramBoolean4);
  }
  
  private LaunchDesc handleApplicationDesc(LaunchDesc paramLaunchDesc, String[] paramArrayOfString, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4, boolean paramBoolean5)
    throws ExitException
  {
    JNLPException.setDefaultLaunchDesc(paramLaunchDesc);
    
    JFrame localJFrame = this._downloadWindow.getFrame();
    
    URL localURL = paramLaunchDesc.getCanonicalHome();
    if (localURL == null)
    {
      localObject1 = new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.nomainjar"), null);
      
      throw new ExitException((Exception)localObject1, 2);
    }
    Object localObject1 = null;
    if (paramBoolean1)
    {
      localObject1 = Cache.getLocalApplicationProperties(paramArrayOfString[0], paramLaunchDesc);
      if ((localObject1 == null) || (!Globals.isInstallMode())) {
        handleJnlpFileException(paramLaunchDesc, new MissingFieldException(paramLaunchDesc.getSource(), "<application-desc>|<applet-desc>"));
      }
      localURL = ((LocalApplicationProperties)localObject1).getLocation();
    }
    else
    {
      localObject1 = Cache.getLocalApplicationProperties(localURL, paramLaunchDesc);
    }
    Trace.println("LaunchDesc location: " + localURL + ", version: " + ((LocalApplicationProperties)localObject1).getVersionId(), TraceLevel.BASIC);
    
    boolean bool1 = LaunchDownload.isInCache(paramLaunchDesc);
    boolean bool2 = (bool1) && (Globals.isOffline());
    
    JREInfo localJREInfo = null;
    if (!paramBoolean3)
    {
      localJREInfo = LaunchSelection.selectJRE(paramLaunchDesc);
      if (localJREInfo == null)
      {
        String str1 = Config.getProperty("deployment.javaws.autodownload");
        if ((str1 != null) && (str1.equalsIgnoreCase("NEVER")))
        {
          localObject2 = paramLaunchDesc.getResources().getSelectedJRE().getVersion();
          
          throw new ExitException(new NoLocalJREException(paramLaunchDesc, (String)localObject2, false), 2);
        }
        if ((str1 != null) && (str1.equalsIgnoreCase("PROMPT"))) {
          if (!AutoDownloadPrompt.prompt(localJFrame, paramLaunchDesc))
          {
            localObject2 = paramLaunchDesc.getResources().getSelectedJRE().getVersion();
            
            throw new ExitException(new NoLocalJREException(paramLaunchDesc, (String)localObject2, true), 2);
          }
        }
      }
    }
    int i = Config.getIntProperty("deployment.javaws.update.timeout");
    
    boolean bool3 = (!bool1) || ((!paramBoolean3) && (localJREInfo == null)) || ((!bool2) && ((((LocalApplicationProperties)localObject1).forceUpdateCheck()) || (paramBoolean1) || (new RapidUpdateCheck().doUpdateCheck(paramLaunchDesc, (LocalApplicationProperties)localObject1, i))));
    
    Trace.println("Offline mode: " + bool2 + "\nIsInCache: " + bool1 + "\nforceUpdate: " + bool3 + "\nInstalled JRE: " + localJREInfo + "\nIsInstaller: " + paramBoolean1, TraceLevel.BASIC);
    if ((bool3) && (bool2)) {
      throw new ExitException(new OfflineLaunchException(), 2);
    }
    Object localObject2 = new ArrayList();
    Object localObject3;
    if (bool3)
    {
      localObject3 = downloadResources(paramLaunchDesc, (!paramBoolean3) && (localJREInfo == null), (!paramBoolean1) && (paramBoolean2), (ArrayList)localObject2, paramBoolean4);
      if (localObject3 != null)
      {
        removeTempJnlpFile(paramLaunchDesc);
        return (LaunchDesc)localObject3;
      }
      if (((LocalApplicationProperties)localObject1).forceUpdateCheck())
      {
        ((LocalApplicationProperties)localObject1).setForceUpdateCheck(false);
        try
        {
          ((LocalApplicationProperties)localObject1).store();
        }
        catch (IOException localIOException1)
        {
          Trace.ignoredException(localIOException1);
        }
      }
      if (!paramBoolean4) {
        checkCacheMax();
      }
    }
    if (SingleInstanceManager.isServerRunning(paramLaunchDesc.getCanonicalHome().toString()))
    {
      localObject3 = Globals.getApplicationArgs();
      if (localObject3 != null) {
        paramLaunchDesc.getApplicationDescriptor().setArguments((String[])localObject3);
      }
      if (SingleInstanceManager.connectToServer(paramLaunchDesc.toString())) {
        throw new ExitException(null, 0);
      }
    }
    if (!paramBoolean4) {
      SplashScreen.generateCustomSplash(localJFrame, paramLaunchDesc, (bool3) || (paramBoolean5));
    }
    if ((!paramBoolean3) && (!((ArrayList)localObject2).isEmpty()))
    {
      if (paramBoolean1) {}
      executeInstallers((ArrayList)localObject2);
    }
    if ((!paramBoolean4) && (this._downloadWindow.getFrame() != null))
    {
      localObject3 = ResourceManager.getString("launch.launchApplication");
      if (paramLaunchDesc.getLaunchType() == 4) {
        localObject3 = ResourceManager.getString("launch.launchInstaller");
      }
      this._downloadWindow.showLaunchingApplication((String)localObject3);
    }
    if (!paramBoolean3)
    {
      if (localJREInfo == null)
      {
        Config.refreshProps();
        localJREInfo = LaunchSelection.selectJRE(paramLaunchDesc);
        if (localJREInfo == null)
        {
          localObject3 = new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.missingjreversion"), null);
          
          throw new ExitException((Exception)localObject3, 2);
        }
      }
      localObject3 = paramLaunchDesc.getResources().getSelectedJRE();
      long l1 = ((JREDesc)localObject3).getMinHeap();
      long l2 = ((JREDesc)localObject3).getMaxHeap();
      boolean bool4 = JnlpxArgs.isCurrentRunningJREHeap(l1, l2);
      
      Properties localProperties = paramLaunchDesc.getResources().getResourceProperties();
      String str2 = ((JREDesc)localObject3).getVmArgs();
      boolean bool5 = JnlpxArgs.isAuxArgsMatch(localProperties, str2);
      
      int j = (JPDA.getDebuggeeType() == 1) || (JPDA.getDebuggeeType() == 3) ? 1 : 0;
      if ((j != 0) || (!JnlpxArgs.getJVMCommand().equals(new File(localJREInfo.getPath()))) || (!bool4) || (!bool5))
      {
        try
        {
          paramArrayOfString = insertApplicationArgs(paramArrayOfString);
          execProgram(localJREInfo, paramArrayOfString, l1, l2, localProperties, str2);
        }
        catch (IOException localIOException2)
        {
          throw new ExitException(new JreExecException(localJREInfo.getPath(), localIOException2), 2);
        }
        if (JnlpxArgs.shouldRemoveArgumentFile()) {
          JnlpxArgs.setShouldRemoveArgumentFile(String.valueOf(false));
        }
        throw new ExitException(null, 0);
      }
    }
    JnlpxArgs.removeArgumentFile(paramArrayOfString);
    if (paramBoolean3)
    {
      this._downloadWindow.disposeWindow();
      
      notifyLocalInstallHandler(paramLaunchDesc, (LocalApplicationProperties)localObject1, (bool3) || (paramBoolean5), paramBoolean3, paramBoolean4, null);
      
      Trace.println("Exiting after import", TraceLevel.BASIC);
      throw new ExitException(null, 0);
    }
    Trace.println("continuing launch in this VM", TraceLevel.BASIC);
    
    continueLaunch((LocalApplicationProperties)localObject1, bool2, localURL, paramLaunchDesc, (bool3) || (paramBoolean5), paramBoolean3, paramBoolean4);
    
    return null;
  }
  
  public static void checkCacheMax()
  {
    long l1 = Config.getCacheSizeMax();
    if (l1 > 0L) {
      try
      {
        long l2 = Cache.getCacheSize();
        if (l2 > l1 * 90L / 100L)
        {
          String str1 = Config.getTempDirectory() + File.separator + "cachemax.timestamp";
          
          File localFile = new File(str1);
          localFile.createNewFile();
          long l3 = localFile.lastModified();
          long l4 = new Date().getTime();
          if (l4 - l3 > 60000L)
          {
            localFile.setLastModified(l4);
            String str2 = ResourceManager.getString("jnlp.cache.warning.title");
            
            String str3 = ResourceManager.getString("jnlp.cache.warning.message", sizeString(l2), sizeString(l1));
            
            SwingUtilities.invokeAndWait(new Runnable()
            {
              private final String val$message;
              private final String val$title;
              
              public void run()
              {
                DialogFactory.showMessageDialog(3, this.val$message, this.val$title, true);
              }
            });
          }
        }
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
    }
  }
  
  private String[] insertApplicationArgs(String[] paramArrayOfString)
  {
    String[] arrayOfString1 = Globals.getApplicationArgs();
    if (arrayOfString1 == null) {
      return paramArrayOfString;
    }
    String[] arrayOfString2 = new String[arrayOfString1.length + paramArrayOfString.length];
    for (int i = 0; i < arrayOfString1.length; i++) {
      arrayOfString2[i] = arrayOfString1[i];
    }
    for (int j = 0; j < paramArrayOfString.length; j++) {
      arrayOfString2[(i++)] = paramArrayOfString[j];
    }
    return arrayOfString2;
  }
  
  private static String sizeString(long paramLong)
  {
    if (paramLong > 1048576L) {
      return "" + paramLong / 1048576L + "Mb";
    }
    return "" + paramLong + "bytes";
  }
  
  private static class EatInput
    implements Runnable
  {
    private InputStream _is;
    
    EatInput(InputStream paramInputStream)
    {
      this._is = paramInputStream;
    }
    
    public void run()
    {
      byte[] arrayOfByte = new byte['Ѐ'];
      try
      {
        int i = 0;
        while (i != -1) {
          i = this._is.read(arrayOfByte);
        }
      }
      catch (IOException localIOException) {}
    }
    
    private static void eatInput(InputStream paramInputStream)
    {
      EatInput localEatInput = new EatInput(paramInputStream);
      new Thread(localEatInput).start();
    }
  }
  
  private void executeInstallers(ArrayList paramArrayList)
    throws ExitException
  {
    if (this._downloadWindow.getFrame() != null)
    {
      String str = ResourceManager.getString("launch.launchInstaller");
      this._downloadWindow.showLaunchingApplication(str);
      
      new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            Thread.sleep(5000L);
          }
          catch (Exception localException) {}
          Launcher.this._downloadWindow.setVisible(false);
        }
      }).start();
    }
    for (int i = 0; i < paramArrayList.size(); i++)
    {
      File localFile = (File)paramArrayList.get(i);
      try
      {
        LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile);
        LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(localFile.getPath(), localLaunchDesc);
        
        localLocalApplicationProperties.setLocallyInstalled(false);
        localLocalApplicationProperties.store();
        
        Trace.println("Installing extension: " + localFile, TraceLevel.EXTENSIONS);
        
        String[] arrayOfString = { "-installer", localFile.getAbsolutePath() };
        
        JREInfo localJREInfo = LaunchSelection.selectJRE(localLaunchDesc);
        if (localJREInfo == null)
        {
          this._downloadWindow.setVisible(true);
          
          LaunchDescException localLaunchDescException = new LaunchDescException(localLaunchDesc, ResourceManager.getString("launch.error.missingjreversion"), null);
          
          throw new ExitException(localLaunchDescException, 2);
        }
        boolean bool = JnlpxArgs.shouldRemoveArgumentFile();
        
        JnlpxArgs.setShouldRemoveArgumentFile("false");
        Properties localProperties = localLaunchDesc.getResources().getResourceProperties();
        Process localProcess = execProgram(localJREInfo, arrayOfString, -1L, -1L, localProperties, null);
        EatInput.eatInput(localProcess.getErrorStream());
        EatInput.eatInput(localProcess.getInputStream());
        localProcess.waitFor();
        
        JnlpxArgs.setShouldRemoveArgumentFile(String.valueOf(bool));
        
        localLocalApplicationProperties.refresh();
        if (localLocalApplicationProperties.isRebootNeeded())
        {
          int j = 0;
          ExtensionInstallHandler localExtensionInstallHandler = ExtensionInstallHandler.getInstance();
          if ((localExtensionInstallHandler != null) && (localExtensionInstallHandler.doPreRebootActions(this._downloadWindow.getFrame()))) {
            j = 1;
          }
          localLocalApplicationProperties.setLocallyInstalled(true);
          localLocalApplicationProperties.setRebootNeeded(false);
          localLocalApplicationProperties.store();
          if ((j != 0) && (localExtensionInstallHandler.doReboot())) {
            throw new ExitException(null, 1);
          }
        }
        if (!localLocalApplicationProperties.isLocallyInstalled())
        {
          this._downloadWindow.setVisible(true);
          
          throw new ExitException(new LaunchDescException(localLaunchDesc, ResourceManager.getString("Launch.error.installfailed"), null), 2);
        }
      }
      catch (JNLPException localJNLPException)
      {
        this._downloadWindow.setVisible(true);
        throw new ExitException(localJNLPException, 2);
      }
      catch (IOException localIOException)
      {
        this._downloadWindow.setVisible(true);
        throw new ExitException(localIOException, 2);
      }
      catch (InterruptedException localInterruptedException)
      {
        this._downloadWindow.setVisible(true);
        throw new ExitException(localInterruptedException, 2);
      }
    }
  }
  
  public static void executeUninstallers(ArrayList paramArrayList)
    throws ExitException
  {
    for (int i = 0; i < paramArrayList.size(); i++)
    {
      File localFile = (File)paramArrayList.get(i);
      try
      {
        LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile);
        LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(localFile.getPath(), localLaunchDesc);
        
        Trace.println("uninstalling extension: " + localFile, TraceLevel.EXTENSIONS);
        
        String[] arrayOfString = { "-silent", "-secure", "-installer", localFile.getAbsolutePath() };
        
        JREInfo localJREInfo = LaunchSelection.selectJRE(localLaunchDesc);
        if (localJREInfo == null)
        {
          localObject = new LaunchDescException(localLaunchDesc, ResourceManager.getString("launch.error.missingjreversion"), null);
          
          throw new ExitException((Exception)localObject, 2);
        }
        Object localObject = localLaunchDesc.getResources().getResourceProperties();
        Process localProcess = execProgram(localJREInfo, arrayOfString, -1L, -1L, (Properties)localObject, null);
        EatInput.eatInput(localProcess.getErrorStream());
        EatInput.eatInput(localProcess.getInputStream());
        localProcess.waitFor();
        
        localLocalApplicationProperties.refresh();
        if (localLocalApplicationProperties.isRebootNeeded())
        {
          int j = 0;
          ExtensionInstallHandler localExtensionInstallHandler = ExtensionInstallHandler.getInstance();
          if ((localExtensionInstallHandler != null) && (localExtensionInstallHandler.doPreRebootActions(null))) {
            j = 1;
          }
          localLocalApplicationProperties.setRebootNeeded(false);
          localLocalApplicationProperties.setLocallyInstalled(false);
          localLocalApplicationProperties.store();
          if ((j != 0) && (localExtensionInstallHandler.doReboot())) {
            throw new ExitException(null, 1);
          }
        }
      }
      catch (JNLPException localJNLPException)
      {
        throw new ExitException(localJNLPException, 2);
      }
      catch (IOException localIOException)
      {
        throw new ExitException(localIOException, 2);
      }
      catch (InterruptedException localInterruptedException)
      {
        throw new ExitException(localInterruptedException, 2);
      }
    }
  }
  
  private static Process execProgram(JREInfo paramJREInfo, String[] paramArrayOfString, long paramLong1, long paramLong2, Properties paramProperties, String paramString)
    throws IOException
  {
    String str1 = null;
    String str2 = null;
    str2 = paramJREInfo.getPath();
    if ((Config.isDebugMode()) && (Config.isDebugVMMode())) {
      str1 = paramJREInfo.getDebugJavaPath();
    } else {
      str1 = paramJREInfo.getPath();
    }
    if ((str1.length() == 0) || (str2.length() == 0)) {
      throw new IllegalArgumentException("must exist");
    }
    String[] arrayOfString1 = JnlpxArgs.getArgumentList(str2, paramLong1, paramLong2, paramProperties, paramString);
    
    int i = 1 + arrayOfString1.length + paramArrayOfString.length;
    String[] arrayOfString2 = new String[i];
    int j = 0;
    arrayOfString2[(j++)] = str1;
    for (int k = 0; k < arrayOfString1.length; k++) {
      arrayOfString2[(j++)] = arrayOfString1[k];
    }
    for (k = 0; k < paramArrayOfString.length; k++) {
      arrayOfString2[(j++)] = paramArrayOfString[k];
    }
    arrayOfString2 = JPDA.JpdaSetup(arrayOfString2, paramJREInfo);
    
    Trace.println("Launching new JRE version: " + paramJREInfo, TraceLevel.BASIC);
    for (k = 0; k < arrayOfString2.length; k++) {
      Trace.println("cmd " + k + " : " + arrayOfString2[k], TraceLevel.BASIC);
    }
    if (Globals.TCKHarnessRun) {
      Main.tckprintln("JVM Starting");
    }
    Trace.flush();
    return Runtime.getRuntime().exec(arrayOfString2);
  }
  
  private void continueLaunch(LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean1, URL paramURL, LaunchDesc paramLaunchDesc, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4)
    throws ExitException
  {
    AppPolicy localAppPolicy = AppPolicy.createInstance(paramLaunchDesc.getCanonicalHome().getHost());
    try
    {
      LaunchDownload.checkSignedResources(paramLaunchDesc);
      
      LaunchDownload.checkSignedLaunchDesc(paramLaunchDesc);
    }
    catch (JNLPException localJNLPException1)
    {
      throw new ExitException(localJNLPException1, 2);
    }
    catch (IOException localIOException1)
    {
      throw new ExitException(localIOException1, 2);
    }
    JNLPClassLoader localJNLPClassLoader = JNLPClassLoader.createClassLoader(paramLaunchDesc, localAppPolicy);
    
    Thread.currentThread().setContextClassLoader(localJNLPClassLoader);
    
    System.setSecurityManager(new JavaWebStartSecurity());
    try
    {
      SwingUtilities.invokeAndWait(new Runnable()
      {
        private final JNLPClassLoader val$netLoader;
        
        public void run()
        {
          Thread.currentThread().setContextClassLoader(this.val$netLoader);
          try
          {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
          }
          catch (UnsupportedLookAndFeelException localUnsupportedLookAndFeelException)
          {
            localUnsupportedLookAndFeelException.printStackTrace();
            Trace.ignoredException(localUnsupportedLookAndFeelException);
          }
        }
      });
    }
    catch (InterruptedException localInterruptedException)
    {
      Trace.ignoredException(localInterruptedException);
    }
    catch (InvocationTargetException localInvocationTargetException1)
    {
      Trace.ignoredException(localInvocationTargetException1);
    }
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run() {}
    });
    String str1 = null;
    Class localClass = null;
    try
    {
      str1 = LaunchDownload.getMainClassName(paramLaunchDesc, true);
      
      Trace.println("Main-class: " + str1, TraceLevel.BASIC);
      if (str1 == null) {
        throw new ClassNotFoundException(str1);
      }
      localClass = localJNLPClassLoader.loadClass(str1);
      if (getClass().getPackage().equals(localClass.getPackage())) {
        throw new ClassNotFoundException(str1);
      }
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new ExitException(localClassNotFoundException, 2);
    }
    catch (IOException localIOException2)
    {
      throw new ExitException(localIOException2, 2);
    }
    catch (JNLPException localJNLPException2)
    {
      throw new ExitException(localJNLPException2, 2);
    }
    catch (Exception localException1)
    {
      throw new ExitException(localException1, 2);
    }
    catch (Throwable localThrowable1)
    {
      localThrowable1.printStackTrace();
      throw new ExitException(new Exception(), 2);
    }
    URL localURL = paramLaunchDesc.getCodebase();
    if (localURL == null) {
      localURL = URLUtil.getBase(paramURL);
    }
    try
    {
      BasicServiceImpl.initialize(localURL, paramBoolean1, BrowserSupport.isWebBrowserSupported());
      if (paramLaunchDesc.getLaunchType() == 4)
      {
        String str2 = paramLocalApplicationProperties.getInstallDirectory();
        if (str2 == null)
        {
          str2 = Cache.getNewExtensionInstallDirectory();
          paramLocalApplicationProperties.setInstallDirectory(str2);
        }
        ExtensionInstallerServiceImpl.initialize(str2, paramLocalApplicationProperties, this._downloadWindow);
      }
    }
    catch (IOException localIOException3)
    {
      throw new ExitException(localIOException3, 2);
    }
    try
    {
      DownloadWindow localDownloadWindow = this._downloadWindow;
      this._downloadWindow = null;
      
      notifyLocalInstallHandler(paramLaunchDesc, paramLocalApplicationProperties, paramBoolean2, paramBoolean3, paramBoolean4, localDownloadWindow.getFrame());
      if (Globals.TCKHarnessRun) {
        Main.tckprintln("JNLP Launching");
      }
      executeMainClass(paramLaunchDesc, paramLocalApplicationProperties, localClass, localDownloadWindow);
    }
    catch (SecurityException localSecurityException)
    {
      throw new ExitException(localSecurityException, 2);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new ExitException(localIllegalAccessException, 2);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw new ExitException(localIllegalArgumentException, 2);
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new ExitException(localInstantiationException, 2);
    }
    catch (InvocationTargetException localInvocationTargetException2)
    {
      Object localObject = localInvocationTargetException2;
      Throwable localThrowable2 = localInvocationTargetException2.getTargetException();
      if ((localThrowable2 instanceof Exception)) {
        localObject = (Exception)localInvocationTargetException2.getTargetException();
      } else {
        localThrowable2.printStackTrace();
      }
      throw new ExitException((Exception)localObject, 2);
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw new ExitException(localNoSuchMethodException, 2);
    }
    catch (Exception localException2)
    {
      Trace.ignoredException(localException2);
    }
    if (paramLaunchDesc.getLaunchType() == 4) {
      throw new ExitException(null, 0);
    }
  }
  
  private boolean _shownDownloadWindow = false;
  
  private LaunchDesc downloadResources(LaunchDesc paramLaunchDesc, boolean paramBoolean1, boolean paramBoolean2, ArrayList paramArrayList, boolean paramBoolean3)
    throws ExitException
  {
    if ((!this._shownDownloadWindow) && (!paramBoolean3))
    {
      this._shownDownloadWindow = true;
      this._downloadWindow.buildIntroScreen();
      this._downloadWindow.showLoadingProgressScreen();
      this._downloadWindow.setVisible(true);
      SplashScreen.hide();
    }
    try
    {
      if (paramBoolean2)
      {
        LaunchDesc localLaunchDesc = LaunchDownload.getUpdatedLaunchDesc(paramLaunchDesc);
        if (localLaunchDesc != null) {
          return localLaunchDesc;
        }
      }
      LaunchDownload.downloadExtensions(paramLaunchDesc, this._downloadWindow, 0, paramArrayList);
      if (paramBoolean1) {
        LaunchDownload.downloadJRE(paramLaunchDesc, this._downloadWindow, paramArrayList);
      }
      LaunchDownload.checkJNLPSecurity(paramLaunchDesc);
      
      LaunchDownload.downloadEagerorAll(paramLaunchDesc, false, this._downloadWindow, false);
    }
    catch (SecurityException localSecurityException)
    {
      throw new ExitException(localSecurityException, 2);
    }
    catch (JNLPException localJNLPException)
    {
      throw new ExitException(localJNLPException, 2);
    }
    catch (IOException localIOException)
    {
      throw new ExitException(localIOException, 2);
    }
    return null;
  }
  
  private void notifyLocalInstallHandler(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, Frame paramFrame)
  {
    if (paramLocalApplicationProperties == null) {
      return;
    }
    paramLocalApplicationProperties.setLastAccessed(new Date());
    paramLocalApplicationProperties.incrementLaunchCount();
    
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    if ((paramLaunchDesc.isApplicationDescriptor()) && ((paramLaunchDesc.getLocation() != null) || (paramLaunchDesc.getInformation().supportsOfflineOperation())))
    {
      Object localObject;
      if ((localLocalInstallHandler != null) && (localLocalInstallHandler.isLocalInstallSupported()))
      {
        localObject = paramLocalApplicationProperties.getAssociations();
        if ((localObject != null) && (localObject.length > 0))
        {
          if (paramBoolean1)
          {
            localLocalInstallHandler.removeAssociations(paramLaunchDesc, paramLocalApplicationProperties);
            localLocalInstallHandler.createAssociations(paramLaunchDesc, paramLocalApplicationProperties, true, paramFrame);
          }
        }
        else {
          localLocalInstallHandler.createAssociations(paramLaunchDesc, paramLocalApplicationProperties, paramBoolean3, paramFrame);
        }
        if (paramLocalApplicationProperties.isLocallyInstalled())
        {
          if ((paramBoolean1) && (!paramLocalApplicationProperties.isLocallyInstalledSystem()))
          {
            localLocalInstallHandler.uninstall(paramLaunchDesc, paramLocalApplicationProperties, true);
            localLocalInstallHandler.install(paramLaunchDesc, paramLocalApplicationProperties);
          }
        }
        else {
          localLocalInstallHandler.installFromLaunch(paramLaunchDesc, paramLocalApplicationProperties, paramBoolean3, paramFrame);
        }
      }
      if (paramBoolean1)
      {
        localObject = paramLaunchDesc.getInformation().getTitle();
        String str1 = paramLaunchDesc.getCanonicalHome().toString();
        
        String str2 = paramLocalApplicationProperties.getRegisteredTitle();
        if ((str2 != null) && (str2.length() != 0)) {
          Config.getInstance().addRemoveProgramsRemove(str2, Globals.isSystemCache());
        }
        paramLocalApplicationProperties.setRegisteredTitle((String)localObject);
        Config.getInstance().addRemoveProgramsAdd(Config.getInstance().toExecArg(str1), (String)localObject, Globals.isSystemCache());
      }
    }
    try
    {
      paramLocalApplicationProperties.store();
    }
    catch (IOException localIOException)
    {
      Trace.println("Couldn't save LAP: " + localIOException, TraceLevel.BASIC);
    }
  }
  
  private void executeMainClass(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, Class paramClass, DownloadWindow paramDownloadWindow)
    throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException
  {
    if (paramLaunchDesc.getLaunchType() == 2) {
      executeApplet(paramLaunchDesc, paramClass, paramDownloadWindow);
    } else {
      executeApplication(paramLaunchDesc, paramLocalApplicationProperties, paramClass, paramDownloadWindow);
    }
  }
  
  private void executeApplication(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, Class paramClass, DownloadWindow paramDownloadWindow)
    throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
  {
    String[] arrayOfString = null;
    if (paramLaunchDesc.getLaunchType() == 4)
    {
      paramDownloadWindow.reset();
      
      arrayOfString = new String[1];
      arrayOfString[0] = (paramLocalApplicationProperties.isLocallyInstalled() ? "uninstall" : "install");
      paramLocalApplicationProperties.setLocallyInstalled(false);
      paramLocalApplicationProperties.setRebootNeeded(false);
      try
      {
        paramLocalApplicationProperties.store();
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
    else
    {
      paramDownloadWindow.disposeWindow();
      SplashScreen.hide();
      if (Globals.getApplicationArgs() != null) {
        arrayOfString = Globals.getApplicationArgs();
      } else {
        arrayOfString = paramLaunchDesc.getApplicationDescriptor().getArguments();
      }
    }
    Object[] arrayOfObject = { arrayOfString };
    
    Class[] arrayOfClass = { new String[0].getClass() };
    Method localMethod = paramClass.getMethod("main", arrayOfClass);
    if (!Modifier.isStatic(localMethod.getModifiers())) {
      throw new NoSuchMethodException(ResourceManager.getString("launch.error.nonstaticmainmethod"));
    }
    localMethod.setAccessible(true);
    
    PerfLogger.setEndTime("Calling Application main");
    PerfLogger.outputLog();
    
    localMethod.invoke(null, arrayOfObject);
  }
  
  private void executeApplet(LaunchDesc paramLaunchDesc, Class paramClass, DownloadWindow paramDownloadWindow)
    throws IllegalAccessException, InstantiationException
  {
    AppletDesc localAppletDesc = paramLaunchDesc.getAppletDescriptor();
    int i = localAppletDesc.getWidth();
    int j = localAppletDesc.getHeight();
    
    Applet localApplet = null;
    localApplet = (Applet)paramClass.newInstance();
    
    SplashScreen.hide();
    if (paramDownloadWindow.getFrame() == null)
    {
      paramDownloadWindow.buildIntroScreen();
      paramDownloadWindow.showLaunchingApplication(paramLaunchDesc.getInformation().getTitle());
    }
    JFrame localJFrame = paramDownloadWindow.getFrame();
    
    boolean bool = BrowserSupport.isWebBrowserSupported();
    
    AppletContainerCallback local5 = new AppletContainerCallback()
    {
      private final JFrame val$mainFrame;
      
      public void showDocument(URL paramAnonymousURL)
      {
        BrowserSupport.showDocument(paramAnonymousURL);
      }
      
      public void relativeResize(Dimension paramAnonymousDimension)
      {
        Dimension localDimension = this.val$mainFrame.getSize();
        localDimension.width += paramAnonymousDimension.width;
        localDimension.height += paramAnonymousDimension.height;
        this.val$mainFrame.setSize(localDimension);
      }
    };
    URL localURL1 = BasicServiceImpl.getInstance().getCodeBase();
    URL localURL2 = localAppletDesc.getDocumentBase();
    if (localURL2 == null) {
      localURL2 = localURL1;
    }
    AppletContainer localAppletContainer = new AppletContainer(local5, localApplet, localAppletDesc.getName(), localURL2, localURL1, i, j, localAppletDesc.getParameters());
    
    localJFrame.removeWindowListener(paramDownloadWindow);
    localJFrame.addWindowListener(new WindowAdapter()
    {
      private final AppletContainer val$ac;
      
      public void windowClosing(WindowEvent paramAnonymousWindowEvent)
      {
        this.val$ac.stopApplet();
      }
    });
    paramDownloadWindow.clearWindow();
    
    localJFrame.setTitle(paramLaunchDesc.getInformation().getTitle());
    Container localContainer = localJFrame.getContentPane();
    localContainer.setLayout(new BorderLayout());
    localContainer.add("Center", localAppletContainer);
    localJFrame.pack();
    Dimension localDimension = localAppletContainer.getPreferredFrameSize(localJFrame);
    localJFrame.setSize(localDimension);
    
    localJFrame.getRootPane().revalidate();
    localJFrame.getRootPane().repaint();
    localJFrame.setResizable(false);
    if (!localJFrame.isVisible()) {
      SwingUtilities.invokeLater(new Runnable()
      {
        private final JFrame val$mainFrame;
        
        public void run()
        {
          this.val$mainFrame.setVisible(true);
        }
      });
    }
    localAppletContainer.startApplet();
  }
  
  private void handleJnlpFileException(LaunchDesc paramLaunchDesc, Exception paramException)
    throws ExitException
  {
    DiskCacheEntry localDiskCacheEntry = null;
    try
    {
      localDiskCacheEntry = DownloadProtocol.getCachedLaunchedFile(paramLaunchDesc.getCanonicalHome());
      if (localDiskCacheEntry != null) {
        Cache.removeEntry(localDiskCacheEntry);
      }
    }
    catch (JNLPException localJNLPException)
    {
      Trace.ignoredException(localJNLPException);
    }
    JFrame localJFrame = this._downloadWindow == null ? null : this._downloadWindow.getFrame();
    
    throw new ExitException(paramException, 2);
  }
  
  private class RapidUpdateCheck
    extends Thread
  {
    private LaunchDesc _ld;
    private LocalApplicationProperties _lap;
    private boolean _updateAvailable;
    private boolean _checkCompleted;
    private Object _signalObject = null;
    
    public RapidUpdateCheck()
    {
      this._ld = null;
      this._signalObject = new Object();
    }
    
    private boolean doUpdateCheck(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, int paramInt)
    {
      this._ld = paramLaunchDesc;
      this._lap = paramLocalApplicationProperties;
      boolean bool = false;
      synchronized (this._signalObject)
      {
        this._updateAvailable = false;
        this._checkCompleted = false;
        start();
        do
        {
          do
          {
            if (paramLaunchDesc.getInformation().supportsOfflineOperation()) {
              try
              {
                this._signalObject.wait(paramInt);
                bool = this._updateAvailable;
              }
              catch (InterruptedException localInterruptedException1)
              {
                bool = false;
              }
            } else {
              try
              {
                this._signalObject.wait(paramInt);
                bool = (this._updateAvailable) || (!this._checkCompleted);
              }
              catch (InterruptedException localInterruptedException2)
              {
                bool = true;
              }
            }
          } while ((this._ld.isHttps()) && (!this._checkCompleted));
          if (Launcher.this._ja == null) {
            break;
          }
        } while (Launcher.this._ja.isChallanging());
      }
      return bool;
    }
    
    public void run()
    {
      boolean bool = false;
      try
      {
        bool = LaunchDownload.isUpdateAvailable(this._ld);
      }
      catch (FailedDownloadingResourceException localFailedDownloadingResourceException)
      {
        if (this._ld.isHttps())
        {
          Throwable localThrowable = localFailedDownloadingResourceException.getWrappedException();
          if ((localThrowable != null) && ((localThrowable instanceof SSLHandshakeException))) {
            Main.systemExit(0);
          }
        }
        Trace.ignoredException(localFailedDownloadingResourceException);
      }
      catch (JNLPException localJNLPException)
      {
        Trace.ignoredException(localJNLPException);
      }
      synchronized (this._signalObject)
      {
        this._updateAvailable = bool;
        this._checkCompleted = true;
        this._signalObject.notify();
      }
      if (this._updateAvailable)
      {
        this._lap.setForceUpdateCheck(true);
        try
        {
          this._lap.store();
        }
        catch (IOException localIOException)
        {
          Trace.ignoredException(localIOException);
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\Launcher.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */