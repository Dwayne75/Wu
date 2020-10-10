package com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class RuntimeInlineAnnotationReader
  extends AbstractInlineAnnotationReaderImpl<Type, Class, Field, Method>
  implements RuntimeAnnotationReader
{
  public <A extends Annotation> A getFieldAnnotation(Class<A> annotation, Field field, Locatable srcPos)
  {
    return LocatableAnnotation.create(field.getAnnotation(annotation), srcPos);
  }
  
  public boolean hasFieldAnnotation(Class<? extends Annotation> annotationType, Field field)
  {
    return field.isAnnotationPresent(annotationType);
  }
  
  public boolean hasClassAnnotation(Class clazz, Class<? extends Annotation> annotationType)
  {
    return clazz.isAnnotationPresent(annotationType);
  }
  
  public Annotation[] getAllFieldAnnotations(Field field, Locatable srcPos)
  {
    Annotation[] r = field.getAnnotations();
    for (int i = 0; i < r.length; i++) {
      r[i] = LocatableAnnotation.create(r[i], srcPos);
    }
    return r;
  }
  
  public <A extends Annotation> A getMethodAnnotation(Class<A> annotation, Method method, Locatable srcPos)
  {
    return LocatableAnnotation.create(method.getAnnotation(annotation), srcPos);
  }
  
  public boolean hasMethodAnnotation(Class<? extends Annotation> annotation, Method method)
  {
    return method.isAnnotationPresent(annotation);
  }
  
  public Annotation[] getAllMethodAnnotations(Method method, Locatable srcPos)
  {
    Annotation[] r = method.getAnnotations();
    for (int i = 0; i < r.length; i++) {
      r[i] = LocatableAnnotation.create(r[i], srcPos);
    }
    return r;
  }
  
  public <A extends Annotation> A getMethodParameterAnnotation(Class<A> annotation, Method method, int paramIndex, Locatable srcPos)
  {
    Annotation[] pa = method.getParameterAnnotations()[paramIndex];
    for (Annotation a : pa) {
      if (a.annotationType() == annotation) {
        return LocatableAnnotation.create(a, srcPos);
      }
    }
    return null;
  }
  
  public <A extends Annotation> A getClassAnnotation(Class<A> a, Class clazz, Locatable srcPos)
  {
    return LocatableAnnotation.create(clazz.getAnnotation(a), srcPos);
  }
  
  private final Map<Class<? extends Annotation>, Map<Package, Annotation>> packageCache = new HashMap();
  
  public <A extends Annotation> A getPackageAnnotation(Class<A> a, Class clazz, Locatable srcPos)
  {
    Package p = clazz.getPackage();
    if (p == null) {
      return null;
    }
    Map<Package, Annotation> cache = (Map)this.packageCache.get(a);
    if (cache == null)
    {
      cache = new HashMap();
      this.packageCache.put(a, cache);
    }
    if (cache.containsKey(p)) {
      return (Annotation)cache.get(p);
    }
    A ann = LocatableAnnotation.create(p.getAnnotation(a), srcPos);
    cache.put(p, ann);
    return ann;
  }
  
  public Class getClassValue(Annotation a, String name)
  {
    try
    {
      return (Class)a.annotationType().getMethod(name, new Class[0]).invoke(a, new Object[0]);
    }
    catch (IllegalAccessException e)
    {
      throw new IllegalAccessError(e.getMessage());
    }
    catch (InvocationTargetException e)
    {
      throw new InternalError(e.getMessage());
    }
    catch (NoSuchMethodException e)
    {
      throw new NoSuchMethodError(e.getMessage());
    }
  }
  
  public Class[] getClassArrayValue(Annotation a, String name)
  {
    try
    {
      return (Class[])a.annotationType().getMethod(name, new Class[0]).invoke(a, new Object[0]);
    }
    catch (IllegalAccessException e)
    {
      throw new IllegalAccessError(e.getMessage());
    }
    catch (InvocationTargetException e)
    {
      throw new InternalError(e.getMessage());
    }
    catch (NoSuchMethodException e)
    {
      throw new NoSuchMethodError(e.getMessage());
    }
  }
  
  protected String fullName(Method m)
  {
    return m.getDeclaringClass().getName() + '#' + m.getName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\RuntimeInlineAnnotationReader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */