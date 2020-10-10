package org.fourthline.cling.support.shared;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class NewPlatformApple
{
  public static void setup(ShutdownHandler shutdownHandler, String appName)
    throws Exception
  {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
    System.setProperty("apple.awt.showGrowBox", "true");
    
    Class appClass = Class.forName("com.apple.eawt.Application");
    Object application = appClass.newInstance();
    Class listenerClass = Class.forName("com.apple.eawt.ApplicationListener");
    Method addAppListmethod = appClass.getDeclaredMethod("addApplicationListener", new Class[] { listenerClass });
    
    Class adapterClass = Class.forName("com.apple.eawt.ApplicationAdapter");
    Object listener = AppListenerProxy.newInstance(adapterClass.newInstance(), shutdownHandler);
    addAppListmethod.invoke(application, new Object[] { listener });
  }
  
  static class AppListenerProxy
    implements InvocationHandler
  {
    private ShutdownHandler shutdownHandler;
    private Object object;
    
    public static Object newInstance(Object obj, ShutdownHandler shutdownHandler)
    {
      return Proxy.newProxyInstance(obj
        .getClass().getClassLoader(), obj
        .getClass().getInterfaces(), new AppListenerProxy(obj, shutdownHandler));
    }
    
    private AppListenerProxy(Object obj, ShutdownHandler shutdownHandler)
    {
      this.object = obj;
      this.shutdownHandler = shutdownHandler;
    }
    
    public Object invoke(Object proxy, Method m, Object[] args)
      throws Throwable
    {
      Object result = null;
      try
      {
        if ("handleQuit".equals(m.getName()))
        {
          if (this.shutdownHandler != null) {
            this.shutdownHandler.shutdown();
          }
        }
        else {
          result = m.invoke(this.object, args);
        }
      }
      catch (Exception localException) {}
      return result;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\shared\NewPlatformApple.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */