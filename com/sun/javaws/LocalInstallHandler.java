package com.sun.javaws;

import com.sun.deploy.association.Action;
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
import com.sun.javaws.cache.Cache;
import com.sun.javaws.jnl.AssociationDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.RContentDesc;
import com.sun.javaws.jnl.ShortcutDesc;
import com.sun.javaws.ui.DesktopIntegration;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;

public abstract class LocalInstallHandler
{
  private static LocalInstallHandler _installHandler;
  
  public static synchronized LocalInstallHandler getInstance()
  {
    if (_installHandler == null) {
      _installHandler = LocalInstallHandlerFactory.newInstance();
    }
    return _installHandler;
  }
  
  public abstract void install(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties);
  
  public abstract void uninstall(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean);
  
  public abstract boolean isLocalInstallSupported();
  
  public abstract boolean isAssociationSupported();
  
  public abstract void associationCompleted();
  
  public abstract String getAssociationOpenCommand(String paramString);
  
  public abstract String getAssociationPrintCommand(String paramString);
  
  public abstract void registerAssociationInternal(Association paramAssociation)
    throws AssociationAlreadyRegisteredException, RegisterFailedException;
  
  public abstract void unregisterAssociationInternal(Association paramAssociation)
    throws AssociationNotRegisteredException, RegisterFailedException;
  
  public abstract String getDefaultIconPath();
  
  private String getJnlpLocation(LaunchDesc paramLaunchDesc)
  {
    File localFile = null;
    try
    {
      localFile = Cache.getCachedLaunchedFile(paramLaunchDesc.getCanonicalHome());
    }
    catch (IOException localIOException) {}
    String str;
    if (localFile != null) {
      str = localFile.getAbsolutePath();
    } else {
      str = paramLaunchDesc.getLocation().toString();
    }
    return str;
  }
  
  private boolean promptUserAssociation(LaunchDesc paramLaunchDesc, Association paramAssociation, boolean paramBoolean1, String paramString, boolean paramBoolean2, Frame paramFrame)
  {
    if (paramBoolean2) {
      return true;
    }
    String str1 = "";
    String str2 = paramAssociation.getMimeType();
    ArrayList localArrayList = (ArrayList)paramAssociation.getFileExtList();
    String str3 = "";
    if (localArrayList != null)
    {
      Iterator localIterator = localArrayList.iterator();
      while (localIterator.hasNext())
      {
        str3 = str3 + localIterator.next();
        if (localIterator.hasNext()) {
          str3 = str3 + ", ";
        }
      }
    }
    if (paramBoolean1)
    {
      str1 = ResourceManager.getString("javaws.association.dialog.existAsk") + "\n\n";
      if (str3 != "") {
        str1 = str1 + ResourceManager.getString("javaws.association.dialog.ext", str3) + "\n";
      }
      if (str2 != null) {
        str1 = str1 + ResourceManager.getString("javaws.association.dialog.mime", str2) + "\n";
      }
      if (paramString == null) {
        str1 = str1 + "\n" + ResourceManager.getString("javaws.association.dialog.exist");
      } else {
        str1 = str1 + "\n" + ResourceManager.getString("javaws.association.dialog.exist.command", paramString);
      }
      str1 = str1 + "\n" + ResourceManager.getString("javaws.association.dialog.askReplace", paramLaunchDesc.getInformation().getTitle());
    }
    else
    {
      str1 = ResourceManager.getString("javaws.association.dialog.ask", paramLaunchDesc.getInformation().getTitle()) + "\n";
      if (str3 != "") {
        str1 = str1 + ResourceManager.getString("javaws.association.dialog.ext", str3) + "\n";
      }
      if (str2 != null) {
        str1 = str1 + ResourceManager.getString("javaws.association.dialog.mime", str2) + "\n";
      }
    }
    int i = 1;
    if (!paramBoolean2) {
      i = DialogFactory.showConfirmDialog(paramFrame, str1, ResourceManager.getString("javaws.association.dialog.title"));
    }
    if (i == 0) {
      return true;
    }
    return false;
  }
  
  private String getOpenActionCommand(Association paramAssociation)
  {
    Action localAction = paramAssociation.getActionByVerb("open");
    String str = null;
    if (localAction != null) {
      str = localAction.getCommand();
    }
    return str;
  }
  
  private boolean registerAssociation(LaunchDesc paramLaunchDesc, String paramString1, String paramString2, boolean paramBoolean, Frame paramFrame)
  {
    AssociationService localAssociationService = new AssociationService();
    Association localAssociation1 = new Association();
    boolean bool = false;
    Association localAssociation2 = null;
    String str1 = "";
    String str2 = null;
    if (paramString1 != null)
    {
      localObject1 = new StringTokenizer(paramString1);
      while (((StringTokenizer)localObject1).hasMoreTokens())
      {
        localObject2 = "." + ((StringTokenizer)localObject1).nextToken();
        Trace.println("associate with ext: " + (String)localObject2, TraceLevel.BASIC);
        if (str1 == "") {
          str1 = (String)localObject2 + " file";
        }
        localAssociation2 = localAssociationService.getFileExtensionAssociation((String)localObject2);
        if (localAssociation2 != null)
        {
          Trace.println("associate with ext: " + (String)localObject2 + " already EXIST", TraceLevel.BASIC);
          if (str2 == null) {
            str2 = getOpenActionCommand(localAssociation2);
          }
          bool = true;
        }
        localAssociation1.addFileExtension((String)localObject2);
      }
    }
    if (paramString2 != null)
    {
      Trace.println("associate with mime: " + paramString2, TraceLevel.BASIC);
      localAssociation2 = localAssociationService.getMimeTypeAssociation(paramString2);
      if (localAssociation2 != null)
      {
        Trace.println("associate with mime: " + paramString2 + " already EXIST", TraceLevel.BASIC);
        if (str2 == null) {
          str2 = getOpenActionCommand(localAssociation2);
        }
        bool = true;
      }
      localAssociation1.setMimeType(paramString2);
    }
    localAssociation1.setName(paramLaunchDesc.getInformation().getTitle());
    localAssociation1.setDescription(str1);
    Object localObject1 = IcoEncoder.getIconPath(paramLaunchDesc);
    if (localObject1 == null) {
      localObject1 = getDefaultIconPath();
    }
    localAssociation1.setIconFileName((String)localObject1);
    
    String str3 = getJnlpLocation(paramLaunchDesc);
    
    String str4 = getAssociationOpenCommand(str3);
    
    String str5 = getAssociationPrintCommand(str3);
    
    Trace.println("register OPEN using: " + str4, TraceLevel.BASIC);
    Object localObject2 = new Action("open", str4, "open the file");
    localAssociation1.addAction((Action)localObject2);
    if (str5 != null)
    {
      Trace.println("register PRINT using: " + str5, TraceLevel.BASIC);
      localObject2 = new Action("print", str5, "print the file");
      localAssociation1.addAction((Action)localObject2);
    }
    try
    {
      if (!Globals.createAssoc()) {
        switch (Config.getAssociationValue())
        {
        case 0: 
          return false;
        case 1: 
          if (bool) {
            return false;
          }
          break;
        case 2: 
          if (!promptUserAssociation(paramLaunchDesc, localAssociation1, bool, str2, paramBoolean, paramFrame)) {
            return false;
          }
          break;
        case 3: 
          if ((bool) && 
            (!promptUserAssociation(paramLaunchDesc, localAssociation1, bool, str2, paramBoolean, paramFrame))) {
            return false;
          }
          break;
        default: 
          if (!promptUserAssociation(paramLaunchDesc, localAssociation1, bool, str2, paramBoolean, paramFrame)) {
            return false;
          }
          break;
        }
      }
      registerAssociationInternal(localAssociation1);
    }
    catch (AssociationAlreadyRegisteredException localAssociationAlreadyRegisteredException1)
    {
      try
      {
        unregisterAssociationInternal(localAssociation1);
        registerAssociationInternal(localAssociation1);
      }
      catch (AssociationNotRegisteredException localAssociationNotRegisteredException)
      {
        Trace.ignoredException(localAssociationNotRegisteredException);
        return false;
      }
      catch (AssociationAlreadyRegisteredException localAssociationAlreadyRegisteredException2)
      {
        Trace.ignoredException(localAssociationAlreadyRegisteredException2);
        return false;
      }
      catch (RegisterFailedException localRegisterFailedException2)
      {
        Trace.ignoredException(localRegisterFailedException2);
        return false;
      }
    }
    catch (RegisterFailedException localRegisterFailedException1)
    {
      Trace.ignoredException(localRegisterFailedException1);
      return false;
    }
    return true;
  }
  
  private void unregisterAssociation(LaunchDesc paramLaunchDesc, String paramString1, String paramString2)
  {
    AssociationService localAssociationService = new AssociationService();
    Association localAssociation = null;
    if (paramString2 != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString2);
      while (localStringTokenizer.hasMoreTokens())
      {
        String str = "." + localStringTokenizer.nextToken();
        
        localAssociation = localAssociationService.getFileExtensionAssociation(str);
        if (localAssociation != null)
        {
          localAssociation.setName(paramLaunchDesc.getInformation().getTitle());
          
          Trace.println("remove association with ext: " + str, TraceLevel.BASIC);
          try
          {
            unregisterAssociationInternal(localAssociation);
          }
          catch (AssociationNotRegisteredException localAssociationNotRegisteredException2)
          {
            Trace.ignoredException(localAssociationNotRegisteredException2);
          }
          catch (RegisterFailedException localRegisterFailedException2)
          {
            Trace.ignoredException(localRegisterFailedException2);
          }
        }
      }
    }
    if (paramString1 != null)
    {
      localAssociation = localAssociationService.getMimeTypeAssociation(paramString1);
      if (localAssociation != null)
      {
        localAssociation.setName(paramLaunchDesc.getInformation().getTitle());
        
        Trace.println("remove association with mime: " + paramString1, TraceLevel.BASIC);
        try
        {
          unregisterAssociationInternal(localAssociation);
        }
        catch (AssociationNotRegisteredException localAssociationNotRegisteredException1)
        {
          Trace.ignoredException(localAssociationNotRegisteredException1);
        }
        catch (RegisterFailedException localRegisterFailedException1)
        {
          Trace.ignoredException(localRegisterFailedException1);
        }
      }
    }
  }
  
  public void removeAssociations(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
    if (isAssociationSupported())
    {
      AssociationDesc[] arrayOfAssociationDesc = paramLocalApplicationProperties.getAssociations();
      if (arrayOfAssociationDesc != null)
      {
        for (int i = 0; i < arrayOfAssociationDesc.length; i++)
        {
          String str1 = arrayOfAssociationDesc[i].getExtensions();
          String str2 = arrayOfAssociationDesc[i].getMimeType();
          
          unregisterAssociation(paramLaunchDesc, str2, str1);
        }
        paramLocalApplicationProperties.setAssociations(null);
        associationCompleted();
      }
    }
  }
  
  public void createAssociations(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean, Frame paramFrame)
  {
    if (Config.getAssociationValue() == 0) {
      return;
    }
    if (isAssociationSupported())
    {
      AssociationDesc[] arrayOfAssociationDesc = paramLaunchDesc.getInformation().getAssociations();
      for (int i = 0; i < arrayOfAssociationDesc.length; i++)
      {
        String str1 = arrayOfAssociationDesc[i].getExtensions();
        String str2 = arrayOfAssociationDesc[i].getMimeType();
        if (registerAssociation(paramLaunchDesc, str1, str2, paramBoolean, paramFrame))
        {
          paramLocalApplicationProperties.addAssociation(arrayOfAssociationDesc[i]);
          save(paramLocalApplicationProperties);
        }
      }
      associationCompleted();
    }
  }
  
  public void installFromLaunch(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean, Frame paramFrame)
  {
    ShortcutDesc localShortcutDesc = paramLaunchDesc.getInformation().getShortcut();
    if ((localShortcutDesc != null) && 
      (!localShortcutDesc.getDesktop()) && (!localShortcutDesc.getMenu())) {
      return;
    }
    if ((paramBoolean) && 
      (Globals.createShortcut()))
    {
      doInstall(paramLaunchDesc, paramLocalApplicationProperties);
      return;
    }
    switch (Config.getShortcutValue())
    {
    case 0: 
      return;
    case 1: 
      doInstall(paramLaunchDesc, paramLocalApplicationProperties);
      return;
    case 4: 
      if (localShortcutDesc != null) {
        doInstall(paramLaunchDesc, paramLocalApplicationProperties);
      }
      return;
    case 3: 
      if (localShortcutDesc == null) {
        return;
      }
      break;
    }
    if (paramLocalApplicationProperties.getAskedForInstall()) {
      return;
    }
    if (paramBoolean) {
      return;
    }
    showDialog(paramLaunchDesc, paramLocalApplicationProperties, paramFrame);
  }
  
  private void showDialog(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, Frame paramFrame)
  {
    int i = DesktopIntegration.showDTIDialog(paramFrame, paramLaunchDesc);
    switch (i)
    {
    case 1: 
      doInstall(paramLaunchDesc, paramLocalApplicationProperties);
      break;
    case 0: 
      paramLocalApplicationProperties.setAskedForInstall(true);
      break;
    default: 
      paramLocalApplicationProperties.setAskedForInstall(false);
    }
  }
  
  public void doInstall(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
    install(paramLaunchDesc, paramLocalApplicationProperties);
    paramLocalApplicationProperties.setAskedForInstall(true);
    RContentDesc[] arrayOfRContentDesc = paramLaunchDesc.getInformation().getRelatedContent();
    if (arrayOfRContentDesc != null) {
      for (int i = 0; i < arrayOfRContentDesc.length; i++)
      {
        URL localURL = arrayOfRContentDesc[i].getHref();
        if ((!"jar".equals(localURL.getProtocol())) && (localURL.toString().endsWith(".jnlp"))) {
          try
          {
            Main.importApp(localURL.toString());
          }
          catch (Exception localException)
          {
            Trace.ignoredException(localException);
          }
        }
      }
    }
  }
  
  public static boolean shouldInstallOverExisting(LaunchDesc paramLaunchDesc)
  {
    int[] arrayOfInt = { 1 };
    Runnable local1 = new Runnable()
    {
      private final int[] val$result;
      private final LaunchDesc val$ld;
      
      public void run()
      {
        this.val$result[0] = DialogFactory.showConfirmDialog(ResourceManager.getString("install.alreadyInstalled", this.val$ld.getInformation().getTitle()), ResourceManager.getString("install.alreadyInstalledTitle"));
      }
    };
    if (!Globals.isSilentMode()) {
      invokeRunnable(local1);
    }
    return arrayOfInt[0] == 0;
  }
  
  public static void invokeRunnable(Runnable paramRunnable)
  {
    if (SwingUtilities.isEventDispatchThread()) {
      paramRunnable.run();
    } else {
      try
      {
        SwingUtilities.invokeAndWait(paramRunnable);
      }
      catch (InterruptedException localInterruptedException) {}catch (InvocationTargetException localInvocationTargetException) {}
    }
  }
  
  public static void save(LocalApplicationProperties paramLocalApplicationProperties)
  {
    try
    {
      paramLocalApplicationProperties.store();
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
  }
  
  public boolean addUninstallShortcut()
  {
    if ((Config.getBooleanProperty("deployment.javaws.uninstall.shortcut")) && (!Globals.isSystemCache())) {
      return true;
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\LocalInstallHandler.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */