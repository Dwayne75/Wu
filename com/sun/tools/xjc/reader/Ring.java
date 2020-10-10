package com.sun.tools.xjc.reader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class Ring
{
  private final Map<Class, Object> components = new HashMap();
  private static final ThreadLocal<Ring> instances = new ThreadLocal()
  {
    public Ring initialValue()
    {
      return new Ring(null);
    }
  };
  
  public static <T> void add(Class<T> clazz, T instance)
  {
    assert (!get().components.containsKey(clazz));
    get().components.put(clazz, instance);
  }
  
  public static <T> void add(T o)
  {
    add(o.getClass(), o);
  }
  
  public static <T> T get(Class<T> key)
  {
    T t = get().components.get(key);
    if (t == null) {
      try
      {
        Constructor<T> c = key.getDeclaredConstructor(new Class[0]);
        c.setAccessible(true);
        t = c.newInstance(new Object[0]);
        if (!get().components.containsKey(key)) {
          add(key, t);
        }
      }
      catch (InstantiationException e)
      {
        throw new Error(e);
      }
      catch (IllegalAccessException e)
      {
        throw new Error(e);
      }
      catch (NoSuchMethodException e)
      {
        throw new Error(e);
      }
      catch (InvocationTargetException e)
      {
        throw new Error(e);
      }
    }
    assert (t != null);
    return t;
  }
  
  public static Ring get()
  {
    return (Ring)instances.get();
  }
  
  public static Ring begin()
  {
    Ring r = (Ring)instances.get();
    instances.set(new Ring());
    return r;
  }
  
  public static void end(Ring old)
  {
    instances.set(old);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\Ring.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */