package com.sun.javaws.util;

import com.sun.deploy.config.Config;
import com.sun.deploy.net.proxy.DynamicProxyManager;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.ConsoleController;
import com.sun.deploy.util.ConsoleWindow;
import com.sun.javaws.Globals;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class JavawsConsoleController
  implements ConsoleController
{
  private static ConsoleWindow console = null;
  private static JavawsConsoleController jcc = null;
  
  public static JavawsConsoleController getInstance()
  {
    if (jcc == null) {
      if (Globals.isJavaVersionAtLeast14()) {
        jcc = new JavawsConsoleController14();
      } else {
        jcc = new JavawsConsoleController();
      }
    }
    return jcc;
  }
  
  public void setLogger(Logger paramLogger) {}
  
  public void setConsole(ConsoleWindow paramConsoleWindow)
  {
    if (console == null) {
      console = paramConsoleWindow;
    }
  }
  
  public ConsoleWindow getConsole()
  {
    return console;
  }
  
  public boolean isIconifiedOnClose()
  {
    return false;
  }
  
  public boolean isDoubleBuffered()
  {
    return true;
  }
  
  public boolean isDumpStackSupported()
  {
    return false;
  }
  
  public String dumpAllStacks()
  {
    return null;
  }
  
  public ThreadGroup getMainThreadGroup()
  {
    return Thread.currentThread().getThreadGroup();
  }
  
  public boolean isSecurityPolicyReloadSupported()
  {
    return false;
  }
  
  public void reloadSecurityPolicy() {}
  
  public boolean isProxyConfigReloadSupported()
  {
    return true;
  }
  
  public void reloadProxyConfig() {}
  
  public boolean isDumpClassLoaderSupported()
  {
    return false;
  }
  
  public String dumpClassLoaders()
  {
    return null;
  }
  
  public boolean isClearClassLoaderSupported()
  {
    return false;
  }
  
  public void clearClassLoaders() {}
  
  public boolean isLoggingSupported()
  {
    return false;
  }
  
  public boolean toggleLogging()
  {
    return false;
  }
  
  public boolean isJCovSupported()
  {
    return false;
  }
  
  public boolean dumpJCovData()
  {
    return false;
  }
  
  public String getProductName()
  {
    return ResourceManager.getString("product.javaws.name", "1.5.0_04");
  }
  
  public void invokeLater(Runnable paramRunnable)
  {
    SwingUtilities.invokeLater(paramRunnable);
  }
  
  public static void showConsoleIfEnable()
  {
    if (Config.getProperty("deployment.console.startup.mode").equals("SHOW")) {
      console.showConsole(true);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\util\JavawsConsoleController.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */