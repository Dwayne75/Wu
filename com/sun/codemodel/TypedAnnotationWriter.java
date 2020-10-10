package com.sun.codemodel;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

class TypedAnnotationWriter<A extends Annotation, W extends JAnnotationWriter<A>>
  implements InvocationHandler, JAnnotationWriter<A>
{
  private final JAnnotationUse use;
  private final Class<A> annotation;
  private final Class<W> writerType;
  private Map<String, JAnnotationArrayMember> arrays;
  
  public TypedAnnotationWriter(Class<A> annotation, Class<W> writer, JAnnotationUse use)
  {
    this.annotation = annotation;
    this.writerType = writer;
    this.use = use;
  }
  
  public JAnnotationUse getAnnotationUse()
  {
    return this.use;
  }
  
  public Class<A> getAnnotationType()
  {
    return this.annotation;
  }
  
  public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable
  {
    if (method.getDeclaringClass() == JAnnotationWriter.class) {
      try
      {
        return method.invoke(this, args);
      }
      catch (InvocationTargetException e)
      {
        throw e.getTargetException();
      }
    }
    String name = method.getName();
    Object arg = null;
    if ((args != null) && (args.length > 0)) {
      arg = args[0];
    }
    Method m = this.annotation.getDeclaredMethod(name, new Class[0]);
    Class<?> rt = m.getReturnType();
    if (rt.isArray()) {
      return addArrayValue(proxy, name, rt.getComponentType(), method.getReturnType(), arg);
    }
    if (Annotation.class.isAssignableFrom(rt))
    {
      Class<? extends Annotation> r = rt;
      return new TypedAnnotationWriter(r, method.getReturnType(), this.use.annotationParam(name, r)).createProxy();
    }
    if ((arg instanceof JType))
    {
      JType targ = (JType)arg;
      checkType(Class.class, rt);
      if (m.getDefaultValue() != null) {
        if (targ.equals(targ.owner().ref((Class)m.getDefaultValue()))) {
          return proxy;
        }
      }
      this.use.param(name, targ);
      return proxy;
    }
    checkType(arg.getClass(), rt);
    if ((m.getDefaultValue() != null) && (m.getDefaultValue().equals(arg))) {
      return proxy;
    }
    if ((arg instanceof String))
    {
      this.use.param(name, (String)arg);
      return proxy;
    }
    if ((arg instanceof Boolean))
    {
      this.use.param(name, ((Boolean)arg).booleanValue());
      return proxy;
    }
    if ((arg instanceof Integer))
    {
      this.use.param(name, ((Integer)arg).intValue());
      return proxy;
    }
    if ((arg instanceof Class))
    {
      this.use.param(name, (Class)arg);
      return proxy;
    }
    if ((arg instanceof Enum))
    {
      this.use.param(name, (Enum)arg);
      return proxy;
    }
    throw new IllegalArgumentException("Unable to handle this method call " + method.toString());
  }
  
  private Object addArrayValue(Object proxy, String name, Class itemType, Class expectedReturnType, Object arg)
  {
    if (this.arrays == null) {
      this.arrays = new HashMap();
    }
    JAnnotationArrayMember m = (JAnnotationArrayMember)this.arrays.get(name);
    if (m == null)
    {
      m = this.use.paramArray(name);
      this.arrays.put(name, m);
    }
    if (Annotation.class.isAssignableFrom(itemType))
    {
      Class<? extends Annotation> r = itemType;
      if (!JAnnotationWriter.class.isAssignableFrom(expectedReturnType)) {
        throw new IllegalArgumentException("Unexpected return type " + expectedReturnType);
      }
      return new TypedAnnotationWriter(r, expectedReturnType, m.annotate(r)).createProxy();
    }
    if ((arg instanceof JType))
    {
      checkType(Class.class, itemType);
      m.param((JType)arg);
      return proxy;
    }
    checkType(arg.getClass(), itemType);
    if ((arg instanceof String))
    {
      m.param((String)arg);
      return proxy;
    }
    if ((arg instanceof Boolean))
    {
      m.param(((Boolean)arg).booleanValue());
      return proxy;
    }
    if ((arg instanceof Integer))
    {
      m.param(((Integer)arg).intValue());
      return proxy;
    }
    if ((arg instanceof Class))
    {
      m.param((Class)arg);
      return proxy;
    }
    throw new IllegalArgumentException("Unable to handle this method call ");
  }
  
  private void checkType(Class actual, Class expected)
  {
    if ((expected == actual) || (expected.isAssignableFrom(actual))) {
      return;
    }
    if (expected == JCodeModel.boxToPrimitive.get(actual)) {
      return;
    }
    throw new IllegalArgumentException("Expected " + expected + " but found " + actual);
  }
  
  private W createProxy()
  {
    return (JAnnotationWriter)Proxy.newProxyInstance(this.writerType.getClassLoader(), new Class[] { this.writerType }, this);
  }
  
  static <W extends JAnnotationWriter<?>> W create(Class<W> w, JAnnotatable annotatable)
  {
    Class<? extends Annotation> a = findAnnotationType(w);
    return new TypedAnnotationWriter(a, w, annotatable.annotate(a)).createProxy();
  }
  
  private static Class<? extends Annotation> findAnnotationType(Class clazz)
  {
    for (Type t : clazz.getGenericInterfaces())
    {
      if ((t instanceof ParameterizedType))
      {
        ParameterizedType p = (ParameterizedType)t;
        if (p.getRawType() == JAnnotationWriter.class) {
          return (Class)p.getActualTypeArguments()[0];
        }
      }
      if ((t instanceof Class))
      {
        Class<? extends Annotation> r = findAnnotationType((Class)t);
        if (r != null) {
          return r;
        }
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\TypedAnnotationWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */