package javax.xml.bind;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

class ContextFinder
{
  private static final Logger logger = Logger.getLogger("javax.xml.bind");
  private static final String PLATFORM_DEFAULT_FACTORY_CLASS = "com.sun.xml.bind.v2.ContextFactory";
  
  static
  {
    try
    {
      if (AccessController.doPrivileged(new GetPropertyAction("jaxb.debug")) != null)
      {
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
      }
    }
    catch (Throwable t) {}
  }
  
  private static void handleInvocationTargetException(InvocationTargetException x)
    throws JAXBException
  {
    Throwable t = x.getTargetException();
    if (t != null)
    {
      if ((t instanceof JAXBException)) {
        throw ((JAXBException)t);
      }
      if ((t instanceof RuntimeException)) {
        throw ((RuntimeException)t);
      }
      if ((t instanceof Error)) {
        throw ((Error)t);
      }
    }
  }
  
  private static JAXBException handleClassCastException(Class originalType, Class targetType)
  {
    URL targetTypeURL = which(targetType);
    
    return new JAXBException(Messages.format("JAXBContext.IllegalCast", originalType.getClassLoader().getResource("javax/xml/bind/JAXBContext.class"), targetTypeURL));
  }
  
  static JAXBContext newInstance(String contextPath, String className, ClassLoader classLoader, Map properties)
    throws JAXBException
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
      Object context = null;
      try
      {
        Method m = spiClass.getMethod("createContext", new Class[] { String.class, ClassLoader.class, Map.class });
        
        context = m.invoke(null, new Object[] { contextPath, classLoader, properties });
      }
      catch (NoSuchMethodException e) {}
      if (context == null)
      {
        Method m = spiClass.getMethod("createContext", new Class[] { String.class, ClassLoader.class });
        
        context = m.invoke(null, new Object[] { contextPath, classLoader });
      }
      if (!(context instanceof JAXBContext)) {
        handleClassCastException(context.getClass(), JAXBContext.class);
      }
      return (JAXBContext)context;
    }
    catch (ClassNotFoundException x)
    {
      throw new JAXBException(Messages.format("ContextFinder.ProviderNotFound", className), x);
    }
    catch (InvocationTargetException x)
    {
      handleInvocationTargetException(x);
      
      Throwable e = x;
      if (x.getTargetException() != null) {
        e = x.getTargetException();
      }
      throw new JAXBException(Messages.format("ContextFinder.CouldNotInstantiate", className, e), e);
    }
    catch (RuntimeException x)
    {
      throw x;
    }
    catch (Exception x)
    {
      throw new JAXBException(Messages.format("ContextFinder.CouldNotInstantiate", className, x), x);
    }
  }
  
  static JAXBContext newInstance(Class[] classes, Map properties, String className)
    throws JAXBException
  {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Class spi;
    try
    {
      logger.fine("Trying to load " + className);
      Class spi;
      if (cl != null) {
        spi = cl.loadClass(className);
      } else {
        spi = Class.forName(className);
      }
    }
    catch (ClassNotFoundException e)
    {
      throw new JAXBException(e);
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("loaded " + className + " from " + which(spi));
    }
    Method m;
    try
    {
      m = spi.getMethod("createContext", new Class[] { Class[].class, Map.class });
    }
    catch (NoSuchMethodException e)
    {
      throw new JAXBException(e);
    }
    try
    {
      Object context = m.invoke(null, new Object[] { classes, properties });
      if (!(context instanceof JAXBContext)) {
        throw handleClassCastException(context.getClass(), JAXBContext.class);
      }
      return (JAXBContext)context;
    }
    catch (IllegalAccessException e)
    {
      throw new JAXBException(e);
    }
    catch (InvocationTargetException e)
    {
      handleInvocationTargetException(e);
      
      Throwable x = e;
      if (e.getTargetException() != null) {
        x = e.getTargetException();
      }
      throw new JAXBException(x);
    }
  }
  
  static JAXBContext find(String factoryId, String contextPath, ClassLoader classLoader, Map properties)
    throws JAXBException
  {
    String jaxbContextFQCN = JAXBContext.class.getName();
    
    StringTokenizer packages = new StringTokenizer(contextPath, ":");
    if (!packages.hasMoreTokens()) {
      throw new JAXBException(Messages.format("ContextFinder.NoPackageInContextPath"));
    }
    logger.fine("Searching jaxb.properties");
    while (packages.hasMoreTokens())
    {
      String packageName = packages.nextToken(":").replace('.', '/');
      
      StringBuilder propFileName = new StringBuilder().append(packageName).append("/jaxb.properties");
      
      Properties props = loadJAXBProperties(classLoader, propFileName.toString());
      if (props != null)
      {
        if (props.containsKey(factoryId))
        {
          String factoryClassName = props.getProperty(factoryId);
          return newInstance(contextPath, factoryClassName, classLoader, properties);
        }
        throw new JAXBException(Messages.format("ContextFinder.MissingProperty", packageName, factoryId));
      }
    }
    logger.fine("Searching the system property");
    
    String factoryClassName = (String)AccessController.doPrivileged(new GetPropertyAction(jaxbContextFQCN));
    if (factoryClassName != null) {
      return newInstance(contextPath, factoryClassName, classLoader, properties);
    }
    logger.fine("Searching META-INF/services");
    try
    {
      StringBuilder resource = new StringBuilder().append("META-INF/services/").append(jaxbContextFQCN);
      InputStream resourceStream = classLoader.getResourceAsStream(resource.toString());
      if (resourceStream != null)
      {
        BufferedReader r = new BufferedReader(new InputStreamReader(resourceStream, "UTF-8"));
        factoryClassName = r.readLine().trim();
        r.close();
        return newInstance(contextPath, factoryClassName, classLoader, properties);
      }
      logger.fine("Unable to load:" + resource.toString());
    }
    catch (UnsupportedEncodingException e)
    {
      throw new JAXBException(e);
    }
    catch (IOException e)
    {
      throw new JAXBException(e);
    }
    logger.fine("Trying to create the platform default provider");
    return newInstance(contextPath, "com.sun.xml.bind.v2.ContextFactory", classLoader, properties);
  }
  
  static JAXBContext find(Class[] classes, Map properties)
    throws JAXBException
  {
    String jaxbContextFQCN = JAXBContext.class.getName();
    for (Class c : classes)
    {
      ClassLoader classLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
      {
        public ClassLoader run()
        {
          return this.val$c.getClassLoader();
        }
      });
      Package pkg = c.getPackage();
      if (pkg != null)
      {
        String packageName = pkg.getName().replace('.', '/');
        
        String resourceName = packageName + "/jaxb.properties";
        logger.fine("Trying to locate " + resourceName);
        Properties props = loadJAXBProperties(classLoader, resourceName);
        if (props == null)
        {
          logger.fine("  not found");
        }
        else
        {
          logger.fine("  found");
          if (props.containsKey("javax.xml.bind.context.factory"))
          {
            String factoryClassName = props.getProperty("javax.xml.bind.context.factory").trim();
            return newInstance(classes, properties, factoryClassName);
          }
          throw new JAXBException(Messages.format("ContextFinder.MissingProperty", packageName, "javax.xml.bind.context.factory"));
        }
      }
    }
    logger.fine("Checking system property " + jaxbContextFQCN);
    String factoryClassName = (String)AccessController.doPrivileged(new GetPropertyAction(jaxbContextFQCN));
    if (factoryClassName != null)
    {
      logger.fine("  found " + factoryClassName);
      return newInstance(classes, properties, factoryClassName);
    }
    logger.fine("  not found");
    
    logger.fine("Checking META-INF/services");
    try
    {
      String resource = "META-INF/services/" + jaxbContextFQCN;
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      URL resourceURL;
      URL resourceURL;
      if (classLoader == null) {
        resourceURL = ClassLoader.getSystemResource(resource);
      } else {
        resourceURL = classLoader.getResource(resource);
      }
      if (resourceURL != null)
      {
        logger.fine("Reading " + resourceURL);
        BufferedReader r = new BufferedReader(new InputStreamReader(resourceURL.openStream(), "UTF-8"));
        factoryClassName = r.readLine().trim();
        return newInstance(classes, properties, factoryClassName);
      }
      logger.fine("Unable to find: " + resource);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new JAXBException(e);
    }
    catch (IOException e)
    {
      throw new JAXBException(e);
    }
    logger.fine("Trying to create the platform default provider");
    return newInstance(classes, properties, "com.sun.xml.bind.v2.ContextFactory");
  }
  
  private static Properties loadJAXBProperties(ClassLoader classLoader, String propFileName)
    throws JAXBException
  {
    Properties props = null;
    try
    {
      URL url;
      URL url;
      if (classLoader == null) {
        url = ClassLoader.getSystemResource(propFileName);
      } else {
        url = classLoader.getResource(propFileName);
      }
      if (url != null)
      {
        logger.fine("loading props from " + url);
        props = new Properties();
        InputStream is = url.openStream();
        props.load(is);
        is.close();
      }
    }
    catch (IOException ioe)
    {
      logger.log(Level.FINE, "Unable to load " + propFileName, ioe);
      throw new JAXBException(ioe.toString(), ioe);
    }
    return props;
  }
  
  static URL which(Class clazz, ClassLoader loader)
  {
    String classnameAsResource = clazz.getName().replace('.', '/') + ".class";
    if (loader == null) {
      loader = ClassLoader.getSystemClassLoader();
    }
    return loader.getResource(classnameAsResource);
  }
  
  static URL which(Class clazz)
  {
    return which(clazz, clazz.getClassLoader());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\ContextFinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */