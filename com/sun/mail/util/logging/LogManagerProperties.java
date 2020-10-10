package com.sun.mail.util.logging;

import java.io.ObjectStreamException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import javax.mail.Authenticator;

final class LogManagerProperties
  extends Properties
{
  private static final long serialVersionUID = -2239983349056806252L;
  private static final LogManager LOG_MANAGER = LogManager.getLogManager();
  private final String prefix;
  
  static LogManager getLogManager()
  {
    return LOG_MANAGER;
  }
  
  static String toLanguageTag(Locale locale)
  {
    String l = locale.getLanguage();
    String c = locale.getCountry();
    String v = locale.getVariant();
    char[] b = new char[l.length() + c.length() + v.length() + 2];
    int count = l.length();
    l.getChars(0, count, b, 0);
    if ((c.length() != 0) || ((l.length() != 0) && (v.length() != 0)))
    {
      b[count] = '-';
      count++;
      c.getChars(0, c.length(), b, count);
      count += c.length();
    }
    if ((v.length() != 0) && ((l.length() != 0) || (c.length() != 0)))
    {
      b[count] = '-';
      count++;
      v.getChars(0, v.length(), b, count);
      count += v.length();
    }
    return String.valueOf(b, 0, count);
  }
  
  static Filter newFilter(String name)
    throws Exception
  {
    return (Filter)newObjectFrom(name, Filter.class);
  }
  
  static Formatter newFormatter(String name)
    throws Exception
  {
    return (Formatter)newObjectFrom(name, Formatter.class);
  }
  
  static Comparator newComparator(String name)
    throws Exception
  {
    return (Comparator)newObjectFrom(name, Comparator.class);
  }
  
  static ErrorManager newErrorManager(String name)
    throws Exception
  {
    return (ErrorManager)newObjectFrom(name, ErrorManager.class);
  }
  
  static Authenticator newAuthenticator(String name)
    throws Exception
  {
    return (Authenticator)newObjectFrom(name, Authenticator.class);
  }
  
  private static Object newObjectFrom(String name, Class type)
    throws Exception
  {
    try
    {
      Class clazz = findClass(name);
      if (type.isAssignableFrom(clazz)) {
        try
        {
          return clazz.getConstructor((Class[])null).newInstance((Object[])null);
        }
        catch (InvocationTargetException ITE)
        {
          throw paramOrError(ITE);
        }
      }
      throw new ClassCastException(clazz.getName() + " cannot be cast to " + type.getName());
    }
    catch (NoClassDefFoundError NCDFE)
    {
      throw new ClassNotFoundException(NCDFE.toString(), NCDFE);
    }
    catch (ExceptionInInitializerError EIIE)
    {
      if ((EIIE.getCause() instanceof Error)) {
        throw EIIE;
      }
      throw new InvocationTargetException(EIIE);
    }
  }
  
  private static Exception paramOrError(InvocationTargetException ite)
  {
    Throwable cause = ite.getCause();
    if ((cause != null) && (
      ((cause instanceof VirtualMachineError)) || ((cause instanceof ThreadDeath)))) {
      throw ((Error)cause);
    }
    return ite;
  }
  
  private static Class findClass(String name)
    throws ClassNotFoundException
  {
    ClassLoader[] loaders = getClassLoaders();
    assert (loaders.length == 2) : loaders.length;
    Class clazz;
    if (loaders[0] != null) {
      try
      {
        clazz = Class.forName(name, false, loaders[0]);
      }
      catch (ClassNotFoundException tryContext)
      {
        Class clazz = tryLoad(name, loaders[1]);
      }
    } else {
      clazz = tryLoad(name, loaders[1]);
    }
    return clazz;
  }
  
  private static Class tryLoad(String name, ClassLoader l)
    throws ClassNotFoundException
  {
    if (l != null) {
      return Class.forName(name, false, l);
    }
    return Class.forName(name);
  }
  
  private static ClassLoader[] getClassLoaders()
  {
    (ClassLoader[])AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        ClassLoader[] loaders = new ClassLoader[2];
        try
        {
          loaders[0] = ClassLoader.getSystemClassLoader();
        }
        catch (SecurityException ignore)
        {
          loaders[0] = null;
        }
        try
        {
          loaders[1] = Thread.currentThread().getContextClassLoader();
        }
        catch (SecurityException ignore)
        {
          loaders[1] = null;
        }
        return loaders;
      }
    });
  }
  
  LogManagerProperties(Properties parent, String prefix)
  {
    super(parent);
    parent.isEmpty();
    if (prefix == null) {
      throw new NullPointerException();
    }
    this.prefix = prefix;
    
    super.isEmpty();
  }
  
  public synchronized Object clone()
  {
    return exportCopy(this.defaults);
  }
  
  public synchronized String getProperty(String key)
  {
    String value = this.defaults.getProperty(key);
    if (value == null)
    {
      LogManager manager = getLogManager();
      if (key.length() > 0) {
        value = manager.getProperty(this.prefix + '.' + key);
      }
      if (value == null) {
        value = manager.getProperty(key);
      }
      if (value != null)
      {
        super.put(key, value);
      }
      else
      {
        Object v = super.get(key);
        value = (v instanceof String) ? (String)v : null;
      }
    }
    return value;
  }
  
  public String getProperty(String key, String def)
  {
    String value = getProperty(key);
    return value == null ? def : value;
  }
  
  public Object get(Object key)
  {
    if ((key instanceof String)) {
      return getProperty((String)key);
    }
    return super.get(key);
  }
  
  public synchronized Object put(Object key, Object value)
  {
    Object def = preWrite(key);
    Object man = super.put(key, value);
    return man == null ? def : man;
  }
  
  public Object setProperty(String key, String value)
  {
    return put(key, value);
  }
  
  public boolean containsKey(Object key)
  {
    if ((key instanceof String)) {
      return getProperty((String)key) != null;
    }
    return super.containsKey(key);
  }
  
  public synchronized Object remove(Object key)
  {
    Object def = preWrite(key);
    Object man = super.remove(key);
    return man == null ? def : man;
  }
  
  public Enumeration propertyNames()
  {
    if (!$assertionsDisabled) {
      throw new AssertionError();
    }
    return super.propertyNames();
  }
  
  public boolean equals(Object o)
  {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof Properties)) {
      return false;
    }
    if (!$assertionsDisabled) {
      throw new AssertionError(this.prefix);
    }
    return super.equals(o);
  }
  
  public int hashCode()
  {
    if (!$assertionsDisabled) {
      throw new AssertionError(this.prefix.hashCode());
    }
    return super.hashCode();
  }
  
  private Object preWrite(Object key)
  {
    assert (Thread.holdsLock(this));
    Object value;
    Object value;
    if (((key instanceof String)) && (!super.containsKey(key))) {
      value = getProperty((String)key);
    } else {
      value = null;
    }
    return value;
  }
  
  private Properties exportCopy(Properties parent)
  {
    Thread.holdsLock(this);
    Properties child = new Properties(parent);
    child.putAll(this);
    return child;
  }
  
  private synchronized Object writeReplace()
    throws ObjectStreamException
  {
    if (!$assertionsDisabled) {
      throw new AssertionError();
    }
    return exportCopy((Properties)this.defaults.clone());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\logging\LogManagerProperties.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */