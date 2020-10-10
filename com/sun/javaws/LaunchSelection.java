package com.sun.javaws;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.jnl.ExtensionDesc;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.PackageDesc;
import com.sun.javaws.jnl.PropertyDesc;
import com.sun.javaws.jnl.ResourceVisitor;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.util.VersionID;
import com.sun.javaws.util.VersionString;
import java.io.File;
import java.net.URL;
import java.util.StringTokenizer;

public class LaunchSelection
{
  static JREInfo selectJRE(LaunchDesc paramLaunchDesc)
  {
    JREDesc[] arrayOfJREDesc = new JREDesc[1];
    JREInfo[] arrayOfJREInfo = new JREInfo[1];
    
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    localResourcesDesc.visit(new ResourceVisitor()
    {
      private final JREInfo[] val$selectedJRE;
      private final JREDesc[] val$selectedJREDesc;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc) {}
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc) {}
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc)
      {
        if (this.val$selectedJRE[0] == null) {
          LaunchSelection.handleJREDesc(paramAnonymousJREDesc, this.val$selectedJRE, this.val$selectedJREDesc);
        }
      }
    });
    arrayOfJREDesc[0].markAsSelected();
    
    localResourcesDesc.addNested(arrayOfJREDesc[0].getNestedResources());
    return arrayOfJREInfo[0];
  }
  
  public static JREInfo selectJRE(URL paramURL, String paramString)
  {
    JREInfo[] arrayOfJREInfo = JREInfo.get();
    if (arrayOfJREInfo == null) {
      return null;
    }
    VersionString localVersionString = new VersionString(paramString);
    for (int i = 0; i < arrayOfJREInfo.length; i++) {
      if (arrayOfJREInfo[i].isOsInfoMatch(Config.getOSName(), Config.getOSArch())) {
        if (arrayOfJREInfo[i].isEnabled()) {
          if (paramURL == null ? isPlatformMatch(arrayOfJREInfo[i], localVersionString) : isProductMatch(arrayOfJREInfo[i], paramURL, localVersionString)) {
            return arrayOfJREInfo[i];
          }
        }
      }
    }
    return null;
  }
  
  private static void handleJREDesc(JREDesc paramJREDesc, JREInfo[] paramArrayOfJREInfo, JREDesc[] paramArrayOfJREDesc)
  {
    URL localURL = paramJREDesc.getHref();
    String str = paramJREDesc.getVersion();
    
    StringTokenizer localStringTokenizer = new StringTokenizer(str, " ", false);
    int i = localStringTokenizer.countTokens();
    if (i > 0)
    {
      String[] arrayOfString = new String[i];
      for (int j = 0; j < i; j++) {
        arrayOfString[j] = localStringTokenizer.nextToken();
      }
      matchJRE(paramJREDesc, arrayOfString, paramArrayOfJREInfo, paramArrayOfJREDesc);
      if (paramArrayOfJREInfo[0] != null) {}
    }
  }
  
  private static void matchJRE(JREDesc paramJREDesc, String[] paramArrayOfString, JREInfo[] paramArrayOfJREInfo, JREDesc[] paramArrayOfJREDesc)
  {
    URL localURL = paramJREDesc.getHref();
    
    JREInfo[] arrayOfJREInfo = JREInfo.get();
    if (arrayOfJREInfo == null) {
      return;
    }
    for (int i = 0; i < paramArrayOfString.length; i++)
    {
      VersionString localVersionString = new VersionString(paramArrayOfString[i]);
      for (int j = 0; j < arrayOfJREInfo.length; j++) {
        if (arrayOfJREInfo[j].isOsInfoMatch(Config.getOSName(), Config.getOSArch())) {
          if (arrayOfJREInfo[j].isEnabled())
          {
            boolean bool1 = localURL == null ? isPlatformMatch(arrayOfJREInfo[j], localVersionString) : isProductMatch(arrayOfJREInfo[j], localURL, localVersionString);
            
            boolean bool2 = JnlpxArgs.getJVMCommand().equals(new File(arrayOfJREInfo[j].getPath()));
            
            boolean bool3 = JnlpxArgs.isCurrentRunningJREHeap(paramJREDesc.getMinHeap(), paramJREDesc.getMaxHeap());
            if ((bool1) && (bool2) && (bool3))
            {
              Trace.println("LaunchSelection: findJRE: Match on current JRE", TraceLevel.BASIC);
              
              paramArrayOfJREInfo[0] = arrayOfJREInfo[j];
              paramArrayOfJREDesc[0] = paramJREDesc;
              return;
            }
            if (bool1)
            {
              Trace.print("LaunchSelection: findJRE: No match on current JRE because ", TraceLevel.BASIC);
              if (!bool1) {
                Trace.print("versions dont match, ", TraceLevel.BASIC);
              }
              if (!bool2) {
                Trace.print("paths dont match, ", TraceLevel.BASIC);
              }
              if (!bool3) {
                Trace.print("heap sizes dont match", TraceLevel.BASIC);
              }
              Trace.println("", TraceLevel.BASIC);
              
              VersionID localVersionID1 = new VersionID(arrayOfJREInfo[j].getProduct());
              VersionID localVersionID2 = null;
              if (paramArrayOfJREInfo[0] != null) {
                localVersionID2 = new VersionID(paramArrayOfJREInfo[0].getProduct());
              }
              if ((localVersionID2 == null) || (localVersionID1.isGreaterThan(localVersionID2)))
              {
                paramArrayOfJREInfo[0] = arrayOfJREInfo[j];
                paramArrayOfJREDesc[0] = paramJREDesc;
              }
            }
          }
        }
      }
    }
    if (paramArrayOfJREDesc[0] == null) {
      paramArrayOfJREDesc[0] = paramJREDesc;
    }
  }
  
  private static boolean isPlatformMatch(JREInfo paramJREInfo, VersionString paramVersionString)
  {
    String str = paramJREInfo.getProduct();
    int i;
    if ((str != null) && (str.indexOf('-') != -1) && (!str.startsWith("1.2")) && (!isInstallJRE(paramJREInfo))) {
      i = 0;
    } else {
      i = 1;
    }
    if (new File(paramJREInfo.getPath()).exists()) {
      return (paramVersionString.contains(paramJREInfo.getPlatform())) && (i != 0);
    }
    return false;
  }
  
  private static boolean isProductMatch(JREInfo paramJREInfo, URL paramURL, VersionString paramVersionString)
  {
    if (new File(paramJREInfo.getPath()).exists()) {
      return (paramJREInfo.getLocation().equals(paramURL.toString())) && (paramVersionString.contains(paramJREInfo.getProduct()));
    }
    return false;
  }
  
  private static boolean isInstallJRE(JREInfo paramJREInfo)
  {
    File localFile1 = new File(Config.getJavaHome());
    File localFile2 = new File(paramJREInfo.getPath());
    File localFile3 = localFile2.getParentFile();
    return localFile1.equals(localFile3.getParentFile());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\LaunchSelection.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */