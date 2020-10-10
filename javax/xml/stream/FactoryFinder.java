package javax.xml.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Properties;

class FactoryFinder
{
  private static boolean debug = false;
  
  static
  {
    try
    {
      debug = System.getProperty("xml.stream.debug") != null;
    }
    catch (Exception x) {}
  }
  
  private static void debugPrintln(String msg)
  {
    if (debug) {
      System.err.println("STREAM: " + msg);
    }
  }
  
  private static ClassLoader findClassLoader()
    throws FactoryConfigurationError
  {
    ClassLoader classLoader;
    try
    {
      Class clazz = Class.forName(FactoryFinder.class.getName() + "$ClassLoaderFinderConcrete");
      
      ClassLoaderFinder clf = (ClassLoaderFinder)clazz.newInstance();
      classLoader = clf.getContextClassLoader();
    }
    catch (LinkageError le)
    {
      classLoader = FactoryFinder.class.getClassLoader();
    }
    catch (ClassNotFoundException x)
    {
      classLoader = FactoryFinder.class.getClassLoader();
    }
    catch (Exception x)
    {
      throw new FactoryConfigurationError(x.toString(), x);
    }
    return classLoader;
  }
  
  private static Object newInstance(String className, ClassLoader classLoader)
    throws FactoryConfigurationError
  {
    try
    {
      Class spiClass;
      Class spiClass;
      if (classLoader == null) {
        spiClass = Class.forName(className);
      } else {
        spiClass = classLoader.loadClass(className);
      }
      return spiClass.newInstance();
    }
    catch (ClassNotFoundException x)
    {
      throw new FactoryConfigurationError("Provider " + className + " not found", x);
    }
    catch (Exception x)
    {
      throw new FactoryConfigurationError("Provider " + className + " could not be instantiated: " + x, x);
    }
  }
  
  static Object find(String factoryId)
    throws FactoryConfigurationError
  {
    return find(factoryId, null);
  }
  
  static Object find(String factoryId, String fallbackClassName)
    throws FactoryConfigurationError
  {
    ClassLoader classLoader = findClassLoader();
    return find(factoryId, fallbackClassName, classLoader);
  }
  
  static Object find(String factoryId, String fallbackClassName, ClassLoader classLoader)
    throws FactoryConfigurationError
  {
    try
    {
      String systemProp = System.getProperty(factoryId);
      if (systemProp != null)
      {
        debugPrintln("found system property" + systemProp);
        return newInstance(systemProp, classLoader);
      }
    }
    catch (SecurityException se) {}
    try
    {
      String javah = System.getProperty("java.home");
      String configFile = javah + File.separator + "lib" + File.separator + "jaxp.properties";
      
      File f = new File(configFile);
      if (f.exists())
      {
        Properties props = new Properties();
        props.load(new FileInputStream(f));
        String factoryClassName = props.getProperty(factoryId);
        debugPrintln("found java.home property " + factoryClassName);
        return newInstance(factoryClassName, classLoader);
      }
    }
    catch (Exception ex)
    {
      if (debug) {
        ex.printStackTrace();
      }
    }
    String serviceId = "META-INF/services/" + factoryId;
    try
    {
      InputStream is = null;
      if (classLoader == null) {
        is = ClassLoader.getSystemResourceAsStream(serviceId);
      } else {
        is = classLoader.getResourceAsStream(serviceId);
      }
      if (is != null)
      {
        debugPrintln("found " + serviceId);
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        
        String factoryClassName = rd.readLine();
        rd.close();
        if ((factoryClassName != null) && (!"".equals(factoryClassName)))
        {
          debugPrintln("loaded from services: " + factoryClassName);
          return newInstance(factoryClassName, classLoader);
        }
      }
    }
    catch (Exception ex)
    {
      if (debug) {
        ex.printStackTrace();
      }
    }
    if (fallbackClassName == null) {
      throw new FactoryConfigurationError("Provider for " + factoryId + " cannot be found", null);
    }
    debugPrintln("loaded from fallback value: " + fallbackClassName);
    return newInstance(fallbackClassName, classLoader);
  }
  
  static class ClassLoaderFinderConcrete
    extends FactoryFinder.ClassLoaderFinder
  {
    ClassLoaderFinderConcrete()
    {
      super();
    }
    
    ClassLoader getContextClassLoader()
    {
      return Thread.currentThread().getContextClassLoader();
    }
  }
  
  private static abstract class ClassLoaderFinder
  {
    abstract ClassLoader getContextClassLoader();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\FactoryFinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */