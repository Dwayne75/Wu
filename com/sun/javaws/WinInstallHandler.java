package com.sun.javaws;

import com.sun.deploy.association.Association;
import com.sun.deploy.association.AssociationAlreadyRegisteredException;
import com.sun.deploy.association.AssociationNotRegisteredException;
import com.sun.deploy.association.AssociationService;
import com.sun.deploy.association.RegisterFailedException;
import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.DialogFactory;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.deploy.util.WinRegistry;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.RContentDesc;
import com.sun.javaws.jnl.ShortcutDesc;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;

public class WinInstallHandler
  extends LocalInstallHandler
{
  private static final String INSTALLED_DESKTOP_SHORTCUT_KEY = "windows.installedDesktopShortcut";
  private static final String INSTALLED_START_MENU_KEY = "windows.installedStartMenuShortcut";
  private static final String UNINSTALLED_START_MENU_KEY = "windows.uninstalledStartMenuShortcut";
  private static final String RCONTENT_START_MENU_KEY = "windows.RContent.shortcuts";
  public static final int TYPE_DESKTOP = 1;
  public static final int TYPE_START_MENU = 2;
  private static final String REG_SHORTCUT_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders";
  private static final String REG_DESKTOP_PATH_KEY = "Desktop";
  private static final String REG_START_MENU_PATH_KEY = "Programs";
  private static final String SHORTCUT_EXTENSION = ".lnk";
  private static final int MAX_PATH = 200;
  private boolean _loadedPaths = false;
  private String _desktopPath;
  private String _startMenuPath;
  private static boolean useSystem;
  
  static
  {
    NativeLibrary.getInstance().load();
    String str = System.getProperty("os.name");
    if ((str.indexOf("2000") != -1) || (str.indexOf("XP") != -1)) {
      useSystem = false;
    } else {
      useSystem = true;
    }
  }
  
  public String getDefaultIconPath()
  {
    return Config.getInstance().getSystemJavawsPath();
  }
  
  public String getAssociationOpenCommand(String paramString)
  {
    return "\"" + Config.getJavawsCommand() + "\"" + " \"-open\" \"%1\" " + "\"" + paramString + "\"";
  }
  
  public String getAssociationPrintCommand(String paramString)
  {
    return "\"" + Config.getJavawsCommand() + "\"" + " \"-print\" \"%1\" " + "\"" + paramString + "\"";
  }
  
  public void registerAssociationInternal(Association paramAssociation)
    throws AssociationAlreadyRegisteredException, RegisterFailedException
  {
    AssociationService localAssociationService = new AssociationService();
    if ((Globals.isSystemCache()) || (useSystem)) {
      localAssociationService.registerSystemAssociation(paramAssociation);
    } else {
      localAssociationService.registerUserAssociation(paramAssociation);
    }
  }
  
  public void unregisterAssociationInternal(Association paramAssociation)
    throws AssociationNotRegisteredException, RegisterFailedException
  {
    AssociationService localAssociationService = new AssociationService();
    if ((Globals.isSystemCache()) || (useSystem)) {
      localAssociationService.unregisterSystemAssociation(paramAssociation);
    } else {
      localAssociationService.unregisterUserAssociation(paramAssociation);
    }
  }
  
  public boolean isLocalInstallSupported()
  {
    return true;
  }
  
  public boolean isAssociationSupported()
  {
    return true;
  }
  
  public void uninstall(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean)
  {
    if (paramLocalApplicationProperties == null)
    {
      Trace.println("No LAP for uninstall, bailing!", TraceLevel.TEMP);
      
      return;
    }
    Object localObject = null;
    int i = 0;
    String str1;
    if ((str1 = paramLocalApplicationProperties.get("windows.installedStartMenuShortcut")) != null)
    {
      if (!uninstallShortcut(str1)) {
        i = 1;
      } else {
        paramLocalApplicationProperties.put("windows.installedStartMenuShortcut", null);
      }
      localObject = str1;
    }
    if ((str1 = paramLocalApplicationProperties.get("windows.uninstalledStartMenuShortcut")) != null)
    {
      if (!uninstallShortcut(str1)) {
        i = 1;
      } else {
        paramLocalApplicationProperties.put("windows.uninstalledStartMenuShortcut", null);
      }
      localObject = str1;
    }
    String str2 = paramLocalApplicationProperties.get("windows.RContent.shortcuts");
    if (str2 != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(str2, File.pathSeparator);
      while (localStringTokenizer.hasMoreElements())
      {
        str1 = localStringTokenizer.nextToken();
        if (str1 != null)
        {
          if (!uninstallShortcut(str1)) {
            i = 1;
          }
          localObject = str1;
        }
      }
      paramLocalApplicationProperties.put("windows.RContent.shortcuts", null);
    }
    if (localObject != null) {
      checkEmpty((String)localObject);
    }
    if ((paramBoolean) && 
      ((str1 = paramLocalApplicationProperties.get("windows.installedDesktopShortcut")) != null)) {
      if (!uninstallShortcut(str1)) {
        i = 1;
      } else {
        paramLocalApplicationProperties.put("windows.installedDesktopShortcut", null);
      }
    }
    if (i != 0) {
      Trace.println("uninstall shortcut failed", TraceLevel.TEMP);
    }
    paramLocalApplicationProperties.setLocallyInstalled(false);
    save(paramLocalApplicationProperties);
  }
  
  private void checkEmpty(String paramString)
  {
    try
    {
      File localFile = new File(paramString).getParentFile();
      if ((localFile != null) && (localFile.isDirectory()) && (localFile.list().length == 0)) {
        localFile.delete();
      }
    }
    catch (Exception localException) {}
  }
  
  private boolean hasValidTitle(LaunchDesc paramLaunchDesc)
  {
    if (paramLaunchDesc == null) {
      return false;
    }
    InformationDesc localInformationDesc = paramLaunchDesc.getInformation();
    if ((localInformationDesc == null) || (localInformationDesc.getTitle().trim() == null))
    {
      Trace.println("Invalid: No title!", TraceLevel.TEMP);
      
      return false;
    }
    return true;
  }
  
  public void install(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
    if (!hasValidTitle(paramLaunchDesc)) {
      return;
    }
    if ((isApplicationInstalled(paramLaunchDesc)) && 
      (!shouldInstallOverExisting(paramLaunchDesc))) {
      return;
    }
    String str = null;
    try
    {
      str = Cache.getCachedLaunchedFile(paramLaunchDesc.getCanonicalHome()).getAbsolutePath();
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    if (str == null)
    {
      installFailed(paramLaunchDesc);
      return;
    }
    ShortcutDesc localShortcutDesc = paramLaunchDesc.getInformation().getShortcut();
    
    boolean bool1 = localShortcutDesc == null ? true : localShortcutDesc.getDesktop();
    if ((bool1) && 
      (!handleInstall(paramLaunchDesc, paramLocalApplicationProperties, str, 1)))
    {
      installFailed(paramLaunchDesc);
      return;
    }
    boolean bool2 = localShortcutDesc == null ? true : localShortcutDesc.getMenu();
    if ((bool2) && 
      (!handleInstall(paramLaunchDesc, paramLocalApplicationProperties, str, 2)))
    {
      uninstall(paramLaunchDesc, paramLocalApplicationProperties, bool1);
      installFailed(paramLaunchDesc);
      return;
    }
    if ((bool2) || (bool1))
    {
      paramLocalApplicationProperties.setLocallyInstalled(true);
      save(paramLocalApplicationProperties);
    }
  }
  
  private void installFailed(LaunchDesc paramLaunchDesc)
  {
    Runnable local1 = new Runnable()
    {
      private final LaunchDesc val$desc;
      
      public void run()
      {
        DialogFactory.showErrorDialog(ResourceManager.getString("install.installFailed", WinInstallHandler.this.getInstallName(this.val$desc)), ResourceManager.getString("install.installFailedTitle"));
      }
    };
    invokeRunnable(local1);
  }
  
  private void uninstallFailed(LaunchDesc paramLaunchDesc)
  {
    Runnable local2 = new Runnable()
    {
      private final LaunchDesc val$desc;
      
      public void run()
      {
        DialogFactory.showErrorDialog(ResourceManager.getString("install.uninstallFailed", WinInstallHandler.this.getInstallName(this.val$desc)), ResourceManager.getString("install.uninstallFailedTitle"));
      }
    };
    invokeRunnable(local2);
  }
  
  private boolean handleInstall(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, String paramString, int paramInt)
  {
    InformationDesc localInformationDesc = paramLaunchDesc.getInformation();
    ShortcutDesc localShortcutDesc = localInformationDesc.getShortcut();
    String str1 = null;
    String str2 = null;
    String str3 = IcoEncoder.getIconPath(paramLaunchDesc);
    String str4 = Config.getInstance().getSystemJavawsPath();
    String str5 = localInformationDesc.getDescription(1);
    boolean bool = true;
    if (str3 == null) {
      str3 = getDefaultIconPath();
    }
    int i = (!localInformationDesc.supportsOfflineOperation()) || (localShortcutDesc == null) || (localShortcutDesc.getOnline()) ? 1 : 0;
    
    String str6 = i != 0 ? "" : "-offline ";
    String str7 = str6 + "\"" + paramString + "\"";
    int j = 0;
    if (paramInt == 1)
    {
      str1 = getDesktopPath(paramLaunchDesc);
      str2 = getDesktopName(paramLaunchDesc);
      
      j = installWrapper(str1, str2, str5, str4, str7, null, str3);
      if (j == 0)
      {
        paramLocalApplicationProperties.put("windows.installedDesktopShortcut", str1);
        Trace.println("Installed desktop shortcut for: " + str2 + ".", TraceLevel.TEMP);
      }
      else
      {
        bool = false;
        Trace.println("Installed desktop shortcut for: " + str2 + " failed (" + j + ")!!!", TraceLevel.TEMP);
      }
    }
    else
    {
      File localFile1 = new File(getSubMenuPath(paramLaunchDesc));
      if ((localFile1.exists()) || (localFile1.mkdirs()))
      {
        str1 = getStartMenuPath(paramLaunchDesc);
        str2 = getStartMenuName(paramLaunchDesc);
        
        j = installWrapper(str1, str2, str5, str4, str7, null, str3);
        if (j == 0)
        {
          paramLocalApplicationProperties.put("windows.installedStartMenuShortcut", str1);
          Trace.println("Installed menu shortcut for: " + str2 + ".", TraceLevel.TEMP);
        }
        else
        {
          bool = false;
          Trace.println("Installed menu shortcut for: " + str2 + " failed (" + j + ")!!!", TraceLevel.TEMP);
        }
        String str8 = getSubMenuDir(paramLaunchDesc);
        if (((str8 == null) || (!str8.equals("Startup"))) && (addUninstallShortcut()))
        {
          str7 = "-uninstall \"" + paramString + "\"";
          str1 = getUninstallPath(paramLaunchDesc);
          str2 = ResourceManager.getString("install.startMenuUninstallShortcutName", str2);
          
          j = installWrapper(str1, str2, str5, str4, str7, null, str3);
          if (j == 0)
          {
            paramLocalApplicationProperties.put("windows.uninstalledStartMenuShortcut", str1);
            Trace.println("Installed menu shortcut for: " + str2 + ".", TraceLevel.TEMP);
          }
          else
          {
            bool = false;
            Trace.println("Installed menu shortcut for: " + str2 + " failed (" + j + ")!!!", TraceLevel.TEMP);
          }
        }
        RContentDesc[] arrayOfRContentDesc = localInformationDesc.getRelatedContent();
        StringBuffer localStringBuffer = new StringBuffer(200 * arrayOfRContentDesc.length);
        if (arrayOfRContentDesc != null) {
          for (int k = 0; k < arrayOfRContentDesc.length; k++)
          {
            str2 = arrayOfRContentDesc[k].getTitle().trim();
            if ((str2 == null) || (str2.length() == 0)) {
              str2 = getStartMenuName(paramLaunchDesc) + " #" + k;
            }
            str2 = getName(str2);
            URL localURL1 = arrayOfRContentDesc[k].getHref();
            if (!localURL1.toString().endsWith("jnlp"))
            {
              str5 = arrayOfRContentDesc[k].getDescription();
              URL localURL2 = arrayOfRContentDesc[k].getIcon();
              String str9 = null;
              if (localURL2 != null) {
                str9 = IcoEncoder.getIconPath(localURL2, null);
              }
              if (str9 == null) {
                str9 = str3;
              }
              str1 = getRCPath(paramLaunchDesc, str2);
              File localFile2 = Cache.getCachedFile(localURL1);
              
              str4 = new WinBrowserSupport().getDefaultHandler(localURL1);
              if (localFile2 != null)
              {
                str7 = "\"file:" + localFile2.getAbsolutePath() + "\"";
                
                j = installWrapper(str1, str2, str5, str4, str7, null, str9);
                if (j == 0)
                {
                  localStringBuffer.append(str1);
                  localStringBuffer.append(File.pathSeparator);
                  Trace.println("Installed menu shortcut for: " + str2 + ".", TraceLevel.TEMP);
                }
                else
                {
                  bool = false;
                  Trace.println("Installed menu shortcut for: " + str2 + " failed (" + j + ")!!!", TraceLevel.TEMP);
                }
              }
              else
              {
                str7 = localURL1.toString();
                
                j = installWrapper(str1, str2, str5, str4, str7, null, str9);
                if (j == 0)
                {
                  localStringBuffer.append(str1);
                  localStringBuffer.append(File.pathSeparator);
                  Trace.println("Installed menu shortcut for: " + str2 + ".", TraceLevel.TEMP);
                }
                else
                {
                  bool = false;
                  Trace.println("Installed menu shortcut for: " + str2 + " failed (" + j + ")!!!", TraceLevel.TEMP);
                }
              }
            }
          }
        }
        if (localStringBuffer.length() > 0) {
          paramLocalApplicationProperties.put("windows.RContent.shortcuts", localStringBuffer.toString());
        } else {
          paramLocalApplicationProperties.put("windows.RContent.shortcuts", null);
        }
      }
      else
      {
        bool = false;
        Trace.println("Installed menu shortcut for: " + str2 + " failed (can't create directory \"" + localFile1.getAbsolutePath() + "\")!!!", TraceLevel.TEMP);
      }
    }
    return bool;
  }
  
  private boolean isApplicationInstalled(LaunchDesc paramLaunchDesc)
  {
    boolean bool1 = false;
    boolean bool2 = false;
    String str = null;
    
    str = getDesktopPath(paramLaunchDesc);
    Trace.println("getDesktopPath(" + str + ").exists() = " + (str == null ? "N/A" : new StringBuffer().append("").append(new File(str).exists()).toString()), TraceLevel.TEMP);
    bool1 = str == null ? true : new File(str).exists();
    
    str = getStartMenuPath(paramLaunchDesc);
    Trace.println("startMenuInstalled(" + str + ").exists() = " + (str == null ? "N/A" : new StringBuffer().append("").append(new File(str).exists()).toString()), TraceLevel.TEMP);
    bool2 = str == null ? true : new File(str).exists();
    
    return (bool1) && (bool2);
  }
  
  private String getInstallName(LaunchDesc paramLaunchDesc)
  {
    String str = paramLaunchDesc.getInformation().getTitle().trim();
    return getName(str);
  }
  
  private String getName(String paramString)
  {
    if (paramString.length() > 32) {
      paramString = paramString.substring(0, 32);
    }
    return paramString;
  }
  
  private String getDesktopName(LaunchDesc paramLaunchDesc)
  {
    return ResourceManager.getString("install.desktopShortcutName", getInstallName(paramLaunchDesc));
  }
  
  private String getStartMenuName(LaunchDesc paramLaunchDesc)
  {
    String str = ResourceManager.getString("install.startMenuShortcutName", getInstallName(paramLaunchDesc));
    
    return str;
  }
  
  private String getDesktopPath(LaunchDesc paramLaunchDesc)
  {
    String str1 = getDesktopPath();
    if (str1 != null)
    {
      String str2 = getDesktopName(paramLaunchDesc);
      if (str2 != null) {
        str1 = str1 + str2;
      }
      if (str1.length() > 192) {
        str1 = str1.substring(0, 192);
      }
      str1 = str1 + ".lnk";
    }
    return str1;
  }
  
  private String getStartMenuPath(LaunchDesc paramLaunchDesc)
  {
    String str1 = getSubMenuPath(paramLaunchDesc);
    if (str1 != null)
    {
      String str2 = getStartMenuName(paramLaunchDesc);
      if (str2 != null) {
        str1 = str1 + str2;
      }
      if (str1.length() > 192) {
        str1 = str1.substring(0, 192);
      }
      str1 = str1 + ".lnk";
    }
    return str1;
  }
  
  private String getRCPath(LaunchDesc paramLaunchDesc, String paramString)
  {
    String str = getSubMenuPath(paramLaunchDesc);
    if (str != null)
    {
      str = str + paramString;
      if (str.length() > 192) {
        str = str.substring(0, 192);
      }
      str = str + ".lnk";
    }
    return str;
  }
  
  private String getUninstallPath(LaunchDesc paramLaunchDesc)
  {
    String str1 = getSubMenuPath(paramLaunchDesc);
    if (str1 != null)
    {
      String str2 = "uninstall  " + getStartMenuName(paramLaunchDesc);
      str1 = str1 + str2;
      if (str1.length() > 192) {
        str1 = str1.substring(0, 192);
      }
      str1 = str1 + ".lnk";
    }
    return str1;
  }
  
  private String getSubMenuPath(LaunchDesc paramLaunchDesc)
  {
    String str1 = getStartMenuPath();
    if (str1 != null)
    {
      String str2 = getSubMenuDir(paramLaunchDesc);
      if (str2 != null) {
        str1 = str1 + str2 + File.separator;
      }
    }
    return str1;
  }
  
  private String getSubMenuDir(LaunchDesc paramLaunchDesc)
  {
    Object localObject = getStartMenuName(paramLaunchDesc);
    ShortcutDesc localShortcutDesc = paramLaunchDesc.getInformation().getShortcut();
    if (localShortcutDesc != null)
    {
      String str = localShortcutDesc.getSubmenu();
      if (str != null) {
        localObject = str;
      }
    }
    if ((localObject != null) && 
      (((String)localObject).equalsIgnoreCase("startup"))) {
      localObject = "Startup";
    }
    return (String)localObject;
  }
  
  private String getDesktopPath()
  {
    loadPathsIfNecessary();
    return this._desktopPath;
  }
  
  private String getStartMenuPath()
  {
    loadPathsIfNecessary();
    return this._startMenuPath;
  }
  
  private void loadPathsIfNecessary()
  {
    int i = -2147483647;
    String str = "";
    if (Globals.isSystemCache())
    {
      i = -2147483646;
      str = "Common ";
    }
    if (!this._loadedPaths)
    {
      this._desktopPath = WinRegistry.getString(i, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", str + "Desktop");
      if ((this._desktopPath != null) && (this._desktopPath.length() > 0) && (this._desktopPath.charAt(this._desktopPath.length() - 1) != '\\')) {
        this._desktopPath += '\\';
      }
      this._startMenuPath = WinRegistry.getString(i, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", str + "Programs");
      if ((this._startMenuPath != null) && (this._startMenuPath.length() > 0) && (this._startMenuPath.charAt(this._startMenuPath.length() - 1) != '\\')) {
        this._startMenuPath += '\\';
      }
      this._loadedPaths = true;
      
      Trace.println("Start path: " + this._startMenuPath + " desktop " + this._desktopPath, TraceLevel.TEMP);
    }
  }
  
  private boolean uninstallShortcut(String paramString)
  {
    File localFile = new File(paramString);
    if (localFile.exists()) {
      return localFile.delete();
    }
    return true;
  }
  
  private int installWrapper(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7)
  {
    Trace.println("installshortcut with args:", TraceLevel.TEMP);
    Trace.println("    path: " + paramString1, TraceLevel.TEMP);
    Trace.println("    name: " + paramString2, TraceLevel.TEMP);
    Trace.println("    desc: " + paramString3, TraceLevel.TEMP);
    Trace.println("    appP: " + paramString4, TraceLevel.TEMP);
    Trace.println("    args: " + paramString5, TraceLevel.TEMP);
    Trace.println("    dir : " + paramString6, TraceLevel.TEMP);
    Trace.println("    icon: " + paramString7, TraceLevel.TEMP);
    Trace.flush();
    
    return Config.getInstance().installShortcut(paramString1, paramString2, paramString3, paramString4, paramString5, paramString6, paramString7);
  }
  
  public void associationCompleted() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\WinInstallHandler.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */