package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

final class Injector
{
  private static final Map<ClassLoader, WeakReference<Injector>> injectors;
  private static final Logger logger;
  
  static Class inject(ClassLoader cl, String className, byte[] image)
  {
    Injector injector = get(cl);
    if (injector != null) {
      return injector.inject(className, image);
    }
    return null;
  }
  
  static Class find(ClassLoader cl, String className)
  {
    Injector injector = get(cl);
    if (injector != null) {
      return injector.find(className);
    }
    return null;
  }
  
  private static Injector get(ClassLoader cl)
  {
    Injector injector = null;
    WeakReference<Injector> wr = (WeakReference)injectors.get(cl);
    if (wr != null) {
      injector = (Injector)wr.get();
    }
    if (injector == null) {
      try
      {
        injectors.put(cl, new WeakReference(injector = new Injector(cl)));
      }
      catch (SecurityException e)
      {
        logger.log(Level.FINE, "Unable to set up a back-door for the injector", e);
        return null;
      }
    }
    return injector;
  }
  
  private final Map<String, Class> classes = new HashMap();
  private final ClassLoader parent;
  private final boolean loadable;
  private static final Method defineClass;
  private static final Method resolveClass;
  
  static
  {
    injectors = Collections.synchronizedMap(new WeakHashMap());
    
    logger = Util.getClassLogger();
    try
    {
      defineClass = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, Integer.TYPE, Integer.TYPE });
      resolveClass = ClassLoader.class.getDeclaredMethod("resolveClass", new Class[] { Class.class });
    }
    catch (NoSuchMethodException e)
    {
      throw new NoSuchMethodError(e.getMessage());
    }
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Void run()
      {
        Injector.defineClass.setAccessible(true);
        Injector.resolveClass.setAccessible(true);
        return null;
      }
    });
  }
  
  private Injector(ClassLoader parent)
  {
    this.parent = parent;
    assert (parent != null);
    
    boolean loadable = false;
    try
    {
      loadable = parent.loadClass(Accessor.class.getName()) == Accessor.class;
    }
    catch (ClassNotFoundException e) {}
    this.loadable = loadable;
  }
  
  private synchronized Class inject(String className, byte[] image)
  {
    if (!this.loadable) {
      return null;
    }
    Class c = (Class)this.classes.get(className);
    if (c == null)
    {
      try
      {
        c = (Class)defineClass.invoke(this.parent, new Object[] { className.replace('/', '.'), image, Integer.valueOf(0), Integer.valueOf(image.length) });
        resolveClass.invoke(this.parent, new Object[] { c });
      }
      catch (IllegalAccessException e)
      {
        logger.log(Level.FINE, "Unable to inject " + className, e);
        return null;
      }
      catch (InvocationTargetException e)
      {
        logger.log(Level.FINE, "Unable to inject " + className, e);
        return null;
      }
      catch (SecurityException e)
      {
        logger.log(Level.FINE, "Unable to inject " + className, e);
        return null;
      }
      catch (LinkageError e)
      {
        logger.log(Level.FINE, "Unable to inject " + className, e);
        return null;
      }
      this.classes.put(className, c);
    }
    return c;
  }
  
  private synchronized Class find(String className)
  {
    return (Class)this.classes.get(className);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\Injector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */