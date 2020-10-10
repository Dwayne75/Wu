package com.sun.xml.bind.v2;

import com.sun.xml.bind.Util;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ClassFactory
{
  private static final Class[] emptyClass = new Class[0];
  private static final Object[] emptyObject = new Object[0];
  private static final Logger logger = Util.getClassLogger();
  private static final ThreadLocal<Map<Class, WeakReference<Constructor>>> tls = new ThreadLocal()
  {
    public Map<Class, WeakReference<Constructor>> initialValue()
    {
      return new WeakHashMap();
    }
  };
  
  public static <T> T create0(Class<T> clazz)
    throws IllegalAccessException, InvocationTargetException, InstantiationException
  {
    Map<Class, WeakReference<Constructor>> m = (Map)tls.get();
    Constructor<T> cons = null;
    WeakReference<Constructor> consRef = (WeakReference)m.get(clazz);
    if (consRef != null) {
      cons = (Constructor)consRef.get();
    }
    if (cons == null)
    {
      try
      {
        cons = clazz.getDeclaredConstructor(emptyClass);
      }
      catch (NoSuchMethodException e)
      {
        logger.log(Level.INFO, "No default constructor found on " + clazz, e);
        NoSuchMethodError exp;
        NoSuchMethodError exp;
        if ((clazz.getDeclaringClass() != null) && (!Modifier.isStatic(clazz.getModifiers()))) {
          exp = new NoSuchMethodError(Messages.NO_DEFAULT_CONSTRUCTOR_IN_INNER_CLASS.format(new Object[] { clazz.getName() }));
        } else {
          exp = new NoSuchMethodError(e.getMessage());
        }
        exp.initCause(e);
        throw exp;
      }
      int classMod = clazz.getModifiers();
      if ((!Modifier.isPublic(classMod)) || (!Modifier.isPublic(cons.getModifiers()))) {
        try
        {
          cons.setAccessible(true);
        }
        catch (SecurityException e)
        {
          logger.log(Level.FINE, "Unable to make the constructor of " + clazz + " accessible", e);
          throw e;
        }
      }
      m.put(clazz, new WeakReference(cons));
    }
    return (T)cons.newInstance(emptyObject);
  }
  
  public static <T> T create(Class<T> clazz)
  {
    try
    {
      return (T)create0(clazz);
    }
    catch (InstantiationException e)
    {
      logger.log(Level.INFO, "failed to create a new instance of " + clazz, e);
      throw new InstantiationError(e.toString());
    }
    catch (IllegalAccessException e)
    {
      logger.log(Level.INFO, "failed to create a new instance of " + clazz, e);
      throw new IllegalAccessError(e.toString());
    }
    catch (InvocationTargetException e)
    {
      Throwable target = e.getTargetException();
      if ((target instanceof RuntimeException)) {
        throw ((RuntimeException)target);
      }
      if ((target instanceof Error)) {
        throw ((Error)target);
      }
      throw new IllegalStateException(target);
    }
  }
  
  public static Object create(Method method)
  {
    Throwable errorMsg;
    try
    {
      return method.invoke(null, emptyObject);
    }
    catch (InvocationTargetException ive)
    {
      Throwable target = ive.getTargetException();
      if ((target instanceof RuntimeException)) {
        throw ((RuntimeException)target);
      }
      if ((target instanceof Error)) {
        throw ((Error)target);
      }
      throw new IllegalStateException(target);
    }
    catch (IllegalAccessException e)
    {
      logger.log(Level.INFO, "failed to create a new instance of " + method.getReturnType().getName(), e);
      throw new IllegalAccessError(e.toString());
    }
    catch (IllegalArgumentException iae)
    {
      logger.log(Level.INFO, "failed to create a new instance of " + method.getReturnType().getName(), iae);
      errorMsg = iae;
    }
    catch (NullPointerException npe)
    {
      logger.log(Level.INFO, "failed to create a new instance of " + method.getReturnType().getName(), npe);
      errorMsg = npe;
    }
    catch (ExceptionInInitializerError eie)
    {
      logger.log(Level.INFO, "failed to create a new instance of " + method.getReturnType().getName(), eie);
      errorMsg = eie;
    }
    NoSuchMethodError exp = new NoSuchMethodError(errorMsg.getMessage());
    exp.initCause(errorMsg);
    throw exp;
  }
  
  public static <T> Class<? extends T> inferImplClass(Class<T> fieldType, Class[] knownImplClasses)
  {
    if (!fieldType.isInterface()) {
      return fieldType;
    }
    for (Class<?> impl : knownImplClasses) {
      if (fieldType.isAssignableFrom(impl)) {
        return impl.asSubclass(fieldType);
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\ClassFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */