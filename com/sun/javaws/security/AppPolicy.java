package com.sun.javaws.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.security.BadCertificateDialog;
import com.sun.deploy.security.CeilingPolicy;
import com.sun.deploy.security.TrustDecider;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.Globals;
import com.sun.javaws.Main;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.jnlp.JNLPClassLoader;
import java.awt.AWTPermission;
import java.io.File;
import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.AccessControlException;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.PropertyPermission;

public class AppPolicy
{
  private String _host = null;
  private File _extensionDir = null;
  private static AppPolicy _instance = null;
  
  public static AppPolicy getInstance()
  {
    return _instance;
  }
  
  public static AppPolicy createInstance(String paramString)
  {
    if (_instance == null) {
      _instance = new AppPolicy(paramString);
    }
    return _instance;
  }
  
  private AppPolicy(String paramString)
  {
    this._host = paramString;
    
    this._extensionDir = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "ext");
  }
  
  public void addPermissions(PermissionCollection paramPermissionCollection, CodeSource paramCodeSource)
  {
    Trace.println("Permission requested for: " + paramCodeSource.getLocation(), TraceLevel.SECURITY);
    
    JARDesc localJARDesc = JNLPClassLoader.getInstance().getJarDescFromFileURL(paramCodeSource.getLocation());
    if (localJARDesc == null) {
      return;
    }
    LaunchDesc localLaunchDesc = localJARDesc.getParent().getParent();
    int i = localLaunchDesc.getSecurityModel();
    if (i != 0)
    {
      grantUnrestrictedAccess(localLaunchDesc, paramCodeSource);
      if (i == 1) {
        CeilingPolicy.addTrustedPermissions(paramPermissionCollection);
      } else {
        addJ2EEApplicationClientPermissionsObject(paramPermissionCollection);
      }
    }
    if (!paramPermissionCollection.implies(new AllPermission())) {
      addSandboxPermissionsObject(paramPermissionCollection, localLaunchDesc.getLaunchType() == 2);
    }
    if (!localLaunchDesc.arePropsSet())
    {
      Properties localProperties = localLaunchDesc.getResources().getResourceProperties();
      Enumeration localEnumeration = localProperties.keys();
      while (localEnumeration.hasMoreElements())
      {
        String str1 = (String)localEnumeration.nextElement();
        String str2 = localProperties.getProperty(str1);
        PropertyPermission localPropertyPermission = new PropertyPermission(str1, "write");
        if (paramPermissionCollection.implies(localPropertyPermission)) {
          System.setProperty(str1, str2);
        } else {
          Trace.ignoredException(new AccessControlException("access denied " + localPropertyPermission, localPropertyPermission));
        }
      }
      localLaunchDesc.setPropsSet(true);
    }
  }
  
  private void setUnrestrictedProps(LaunchDesc paramLaunchDesc)
  {
    if (!paramLaunchDesc.arePropsSet())
    {
      Properties localProperties = paramLaunchDesc.getResources().getResourceProperties();
      Enumeration localEnumeration = localProperties.keys();
      while (localEnumeration.hasMoreElements())
      {
        String str = (String)localEnumeration.nextElement();
        System.setProperty(str, localProperties.getProperty(str));
      }
      paramLaunchDesc.setPropsSet(true);
    }
  }
  
  public void grantUnrestrictedAccess(LaunchDesc paramLaunchDesc, CodeSource paramCodeSource)
  {
    int i = 0;
    String str;
    switch (paramLaunchDesc.getLaunchType())
    {
    case 1: 
    default: 
      str = "trustdecider.code.type.application";
      break;
    case 2: 
      str = "trustdecider.code.type.applet";
      break;
    case 3: 
      str = "trustdecider.code.type.extension";
      break;
    case 4: 
      str = "trustdecider.code.type.installer";
    }
    try
    {
      if ((Globals.isSecureMode()) || (TrustDecider.isAllPermissionGranted(paramCodeSource, str)))
      {
        setUnrestrictedProps(paramLaunchDesc);
        return;
      }
      Trace.println("We were not granted permission, exiting", TraceLevel.SECURITY);
    }
    catch (Exception localException)
    {
      BadCertificateDialog.show(paramCodeSource, str, localException);
    }
    Main.systemExit(-1);
  }
  
  private void addJ2EEApplicationClientPermissionsObject(PermissionCollection paramPermissionCollection)
  {
    Trace.println("Creating J2EE-application-client-permisisons object", TraceLevel.SECURITY);
    
    paramPermissionCollection.add(new AWTPermission("accessClipboard"));
    paramPermissionCollection.add(new AWTPermission("accessEventQueue"));
    paramPermissionCollection.add(new AWTPermission("showWindowWithoutWarningBanner"));
    
    paramPermissionCollection.add(new RuntimePermission("exitVM"));
    paramPermissionCollection.add(new RuntimePermission("loadLibrary"));
    paramPermissionCollection.add(new RuntimePermission("queuePrintJob"));
    
    paramPermissionCollection.add(new SocketPermission("*", "connect"));
    paramPermissionCollection.add(new SocketPermission("localhost:1024-", "accept,listen"));
    
    paramPermissionCollection.add(new FilePermission("*", "read,write"));
    
    paramPermissionCollection.add(new PropertyPermission("*", "read"));
  }
  
  private void addSandboxPermissionsObject(PermissionCollection paramPermissionCollection, boolean paramBoolean)
  {
    Trace.println("Add sandbox permissions", TraceLevel.SECURITY);
    
    paramPermissionCollection.add(new PropertyPermission("java.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vendor", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vendor.url", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.class.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("os.name", "read"));
    paramPermissionCollection.add(new PropertyPermission("os.arch", "read"));
    paramPermissionCollection.add(new PropertyPermission("os.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("file.separator", "read"));
    paramPermissionCollection.add(new PropertyPermission("path.separator", "read"));
    paramPermissionCollection.add(new PropertyPermission("line.separator", "read"));
    
    paramPermissionCollection.add(new PropertyPermission("java.specification.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.specification.vendor", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.specification.name", "read"));
    
    paramPermissionCollection.add(new PropertyPermission("java.vm.specification.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.specification.vendor", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.specification.name", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.vendor", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.name", "read"));
    
    paramPermissionCollection.add(new PropertyPermission("javawebstart.version", "read"));
    
    paramPermissionCollection.add(new RuntimePermission("exitVM"));
    paramPermissionCollection.add(new RuntimePermission("stopThread"));
    
    String str = "Java " + (paramBoolean ? "Applet" : "Application") + " Window";
    if (Config.getBooleanProperty("deployment.security.sandbox.awtwarningwindow")) {
      System.setProperty("awt.appletWarning", str);
    } else {
      paramPermissionCollection.add(new AWTPermission("showWindowWithoutWarningBanner"));
    }
    paramPermissionCollection.add(new SocketPermission("localhost:1024-", "listen"));
    
    paramPermissionCollection.add(new SocketPermission(this._host, "connect, accept"));
    
    paramPermissionCollection.add(new PropertyPermission("jnlp.*", "read,write"));
    paramPermissionCollection.add(new PropertyPermission("javaws.*", "read,write"));
    
    String[] arrayOfString = Config.getSecureProperties();
    for (int i = 0; i < arrayOfString.length; i++) {
      paramPermissionCollection.add(new PropertyPermission(arrayOfString[i], "read,write"));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\security\AppPolicy.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */