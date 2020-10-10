package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.runtime.Location;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class LocatableAnnotation
  implements InvocationHandler, Locatable, Location
{
  private final Annotation core;
  private final Locatable upstream;
  
  public static <A extends Annotation> A create(A annotation, Locatable parentSourcePos)
  {
    if (annotation == null) {
      return null;
    }
    Class<? extends Annotation> type = annotation.annotationType();
    if (quicks.containsKey(type)) {
      return ((Quick)quicks.get(type)).newInstance(parentSourcePos, annotation);
    }
    ClassLoader cl = LocatableAnnotation.class.getClassLoader();
    try
    {
      Class loadableT = Class.forName(type.getName(), false, cl);
      if (loadableT != type) {
        return annotation;
      }
      return (Annotation)Proxy.newProxyInstance(cl, new Class[] { type, Locatable.class }, new LocatableAnnotation(annotation, parentSourcePos));
    }
    catch (ClassNotFoundException e)
    {
      return annotation;
    }
    catch (IllegalArgumentException e) {}
    return annotation;
  }
  
  LocatableAnnotation(Annotation core, Locatable upstream)
  {
    this.core = core;
    this.upstream = upstream;
  }
  
  public Locatable getUpstream()
  {
    return this.upstream;
  }
  
  public Location getLocation()
  {
    return this;
  }
  
  public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable
  {
    try
    {
      if (method.getDeclaringClass() == Locatable.class) {
        return method.invoke(this, args);
      }
      if (Modifier.isStatic(method.getModifiers())) {
        throw new IllegalArgumentException();
      }
      return method.invoke(this.core, args);
    }
    catch (InvocationTargetException e)
    {
      if (e.getTargetException() != null) {
        throw e.getTargetException();
      }
      throw e;
    }
  }
  
  public String toString()
  {
    return this.core.toString();
  }
  
  private static final Map<Class, Quick> quicks = new HashMap();
  
  static
  {
    for (Quick q : Init.getAll()) {
      quicks.put(q.annotationType(), q);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\LocatableAnnotation.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */