package com.sun.xml.bind.v2.model.nav;

import com.sun.xml.bind.v2.runtime.Location;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;

public final class ReflectionNavigator
  implements Navigator<Type, Class, Field, Method>
{
  public Class getSuperClass(Class clazz)
  {
    if (clazz == Object.class) {
      return null;
    }
    Class sc = clazz.getSuperclass();
    if (sc == null) {
      sc = Object.class;
    }
    return sc;
  }
  
  private static final TypeVisitor<Type, Class> baseClassFinder = new TypeVisitor()
  {
    public Type onClass(Class c, Class sup)
    {
      if (sup == c) {
        return sup;
      }
      Type sc = c.getGenericSuperclass();
      if (sc != null)
      {
        Type r = (Type)visit(sc, sup);
        if (r != null) {
          return r;
        }
      }
      for (Type i : c.getGenericInterfaces())
      {
        Type r = (Type)visit(i, sup);
        if (r != null) {
          return r;
        }
      }
      return null;
    }
    
    public Type onParameterizdType(ParameterizedType p, Class sup)
    {
      Class raw = (Class)p.getRawType();
      if (raw == sup) {
        return p;
      }
      Type r = raw.getGenericSuperclass();
      if (r != null) {
        r = (Type)visit(bind(r, raw, p), sup);
      }
      if (r != null) {
        return r;
      }
      for (Type i : raw.getGenericInterfaces())
      {
        r = (Type)visit(bind(i, raw, p), sup);
        if (r != null) {
          return r;
        }
      }
      return null;
    }
    
    public Type onGenericArray(GenericArrayType g, Class sup)
    {
      return null;
    }
    
    public Type onVariable(TypeVariable v, Class sup)
    {
      return (Type)visit(v.getBounds()[0], sup);
    }
    
    public Type onWildcard(WildcardType w, Class sup)
    {
      return null;
    }
    
    private Type bind(Type t, GenericDeclaration decl, ParameterizedType args)
    {
      return (Type)ReflectionNavigator.binder.visit(t, new ReflectionNavigator.BinderArg(decl, args.getActualTypeArguments()));
    }
  };
  
  private static class BinderArg
  {
    final TypeVariable[] params;
    final Type[] args;
    
    BinderArg(TypeVariable[] params, Type[] args)
    {
      this.params = params;
      this.args = args;
      assert (params.length == args.length);
    }
    
    public BinderArg(GenericDeclaration decl, Type[] args)
    {
      this(decl.getTypeParameters(), args);
    }
    
    Type replace(TypeVariable v)
    {
      for (int i = 0; i < this.params.length; i++) {
        if (this.params[i].equals(v)) {
          return this.args[i];
        }
      }
      return v;
    }
  }
  
  private static final TypeVisitor<Type, BinderArg> binder = new TypeVisitor()
  {
    public Type onClass(Class c, ReflectionNavigator.BinderArg args)
    {
      return c;
    }
    
    public Type onParameterizdType(ParameterizedType p, ReflectionNavigator.BinderArg args)
    {
      Type[] params = p.getActualTypeArguments();
      
      boolean different = false;
      for (int i = 0; i < params.length; i++)
      {
        Type t = params[i];
        params[i] = ((Type)visit(t, args));
        different |= t != params[i];
      }
      Type newOwner = p.getOwnerType();
      if (newOwner != null) {
        newOwner = (Type)visit(newOwner, args);
      }
      different |= p.getOwnerType() != newOwner;
      if (!different) {
        return p;
      }
      return new ParameterizedTypeImpl((Class)p.getRawType(), params, newOwner);
    }
    
    public Type onGenericArray(GenericArrayType g, ReflectionNavigator.BinderArg types)
    {
      Type c = (Type)visit(g.getGenericComponentType(), types);
      if (c == g.getGenericComponentType()) {
        return g;
      }
      return new GenericArrayTypeImpl(c);
    }
    
    public Type onVariable(TypeVariable v, ReflectionNavigator.BinderArg types)
    {
      return types.replace(v);
    }
    
    public Type onWildcard(WildcardType w, ReflectionNavigator.BinderArg types)
    {
      Type[] lb = w.getLowerBounds();
      Type[] ub = w.getUpperBounds();
      boolean diff = false;
      for (int i = 0; i < lb.length; i++)
      {
        Type t = lb[i];
        lb[i] = ((Type)visit(t, types));
        diff |= t != lb[i];
      }
      for (int i = 0; i < ub.length; i++)
      {
        Type t = ub[i];
        ub[i] = ((Type)visit(t, types));
        diff |= t != ub[i];
      }
      if (!diff) {
        return w;
      }
      return new WildcardTypeImpl(lb, ub);
    }
  };
  
  public Type getBaseClass(Type t, Class sup)
  {
    return (Type)baseClassFinder.visit(t, sup);
  }
  
  public String getClassName(Class clazz)
  {
    return clazz.getName();
  }
  
  public String getTypeName(Type type)
  {
    if ((type instanceof Class))
    {
      Class c = (Class)type;
      if (c.isArray()) {
        return getTypeName(c.getComponentType()) + "[]";
      }
      return c.getName();
    }
    return type.toString();
  }
  
  public String getClassShortName(Class clazz)
  {
    return clazz.getSimpleName();
  }
  
  public Collection<? extends Field> getDeclaredFields(Class clazz)
  {
    return Arrays.asList(clazz.getDeclaredFields());
  }
  
  public Field getDeclaredField(Class clazz, String fieldName)
  {
    try
    {
      return clazz.getDeclaredField(fieldName);
    }
    catch (NoSuchFieldException e) {}
    return null;
  }
  
  public Collection<? extends Method> getDeclaredMethods(Class clazz)
  {
    return Arrays.asList(clazz.getDeclaredMethods());
  }
  
  public Class getDeclaringClassForField(Field field)
  {
    return field.getDeclaringClass();
  }
  
  public Class getDeclaringClassForMethod(Method method)
  {
    return method.getDeclaringClass();
  }
  
  public Type getFieldType(Field field)
  {
    return fix(field.getGenericType());
  }
  
  public String getFieldName(Field field)
  {
    return field.getName();
  }
  
  public String getMethodName(Method method)
  {
    return method.getName();
  }
  
  public Type getReturnType(Method method)
  {
    return fix(method.getGenericReturnType());
  }
  
  public Type[] getMethodParameters(Method method)
  {
    return method.getGenericParameterTypes();
  }
  
  public boolean isStaticMethod(Method method)
  {
    return Modifier.isStatic(method.getModifiers());
  }
  
  public boolean isSubClassOf(Type sub, Type sup)
  {
    return erasure(sup).isAssignableFrom(erasure(sub));
  }
  
  public Class ref(Class c)
  {
    return c;
  }
  
  public Class use(Class c)
  {
    return c;
  }
  
  public Class asDecl(Type t)
  {
    return erasure(t);
  }
  
  public Class asDecl(Class c)
  {
    return c;
  }
  
  private static final TypeVisitor<Class, Void> eraser = new TypeVisitor()
  {
    public Class onClass(Class c, Void _)
    {
      return c;
    }
    
    public Class onParameterizdType(ParameterizedType p, Void _)
    {
      return (Class)visit(p.getRawType(), null);
    }
    
    public Class onGenericArray(GenericArrayType g, Void _)
    {
      return Array.newInstance((Class)visit(g.getGenericComponentType(), null), 0).getClass();
    }
    
    public Class onVariable(TypeVariable v, Void _)
    {
      return (Class)visit(v.getBounds()[0], null);
    }
    
    public Class onWildcard(WildcardType w, Void _)
    {
      return (Class)visit(w.getUpperBounds()[0], null);
    }
  };
  
  public <T> Class<T> erasure(Type t)
  {
    return (Class)eraser.visit(t, null);
  }
  
  public boolean isAbstract(Class clazz)
  {
    return Modifier.isAbstract(clazz.getModifiers());
  }
  
  public boolean isFinal(Class clazz)
  {
    return Modifier.isFinal(clazz.getModifiers());
  }
  
  public Type createParameterizedType(Class rawType, Type... arguments)
  {
    return new ParameterizedTypeImpl(rawType, arguments, null);
  }
  
  public boolean isArray(Type t)
  {
    if ((t instanceof Class))
    {
      Class c = (Class)t;
      return c.isArray();
    }
    if ((t instanceof GenericArrayType)) {
      return true;
    }
    return false;
  }
  
  public boolean isArrayButNotByteArray(Type t)
  {
    if ((t instanceof Class))
    {
      Class c = (Class)t;
      return (c.isArray()) && (c != byte[].class);
    }
    if ((t instanceof GenericArrayType))
    {
      t = ((GenericArrayType)t).getGenericComponentType();
      return t != Byte.TYPE;
    }
    return false;
  }
  
  public Type getComponentType(Type t)
  {
    if ((t instanceof Class))
    {
      Class c = (Class)t;
      return c.getComponentType();
    }
    if ((t instanceof GenericArrayType)) {
      return ((GenericArrayType)t).getGenericComponentType();
    }
    throw new IllegalArgumentException();
  }
  
  public Type getTypeArgument(Type type, int i)
  {
    if ((type instanceof ParameterizedType))
    {
      ParameterizedType p = (ParameterizedType)type;
      return fix(p.getActualTypeArguments()[i]);
    }
    throw new IllegalArgumentException();
  }
  
  public boolean isParameterizedType(Type type)
  {
    return type instanceof ParameterizedType;
  }
  
  public boolean isPrimitive(Type type)
  {
    if ((type instanceof Class))
    {
      Class c = (Class)type;
      return c.isPrimitive();
    }
    return false;
  }
  
  public Type getPrimitive(Class primitiveType)
  {
    assert (primitiveType.isPrimitive());
    return primitiveType;
  }
  
  public Location getClassLocation(final Class clazz)
  {
    new Location()
    {
      public String toString()
      {
        return clazz.getName();
      }
    };
  }
  
  public Location getFieldLocation(final Field field)
  {
    new Location()
    {
      public String toString()
      {
        return field.toString();
      }
    };
  }
  
  public Location getMethodLocation(final Method method)
  {
    new Location()
    {
      public String toString()
      {
        return method.toString();
      }
    };
  }
  
  public boolean hasDefaultConstructor(Class c)
  {
    try
    {
      c.getDeclaredConstructor(new Class[0]);
      return true;
    }
    catch (NoSuchMethodException e) {}
    return false;
  }
  
  public boolean isStaticField(Field field)
  {
    return Modifier.isStatic(field.getModifiers());
  }
  
  public boolean isPublicMethod(Method method)
  {
    return Modifier.isPublic(method.getModifiers());
  }
  
  public boolean isPublicField(Field field)
  {
    return Modifier.isPublic(field.getModifiers());
  }
  
  public boolean isEnum(Class c)
  {
    return Enum.class.isAssignableFrom(c);
  }
  
  public Field[] getEnumConstants(Class clazz)
  {
    try
    {
      Object[] values = clazz.getEnumConstants();
      Field[] fields = new Field[values.length];
      for (int i = 0; i < values.length; i++) {
        fields[i] = clazz.getField(((Enum)values[i]).name());
      }
      return fields;
    }
    catch (NoSuchFieldException e)
    {
      throw new NoSuchFieldError(e.getMessage());
    }
  }
  
  public Type getVoidType()
  {
    return Void.class;
  }
  
  public String getPackageName(Class clazz)
  {
    String name = clazz.getName();
    int idx = name.lastIndexOf('.');
    if (idx < 0) {
      return "";
    }
    return name.substring(0, idx);
  }
  
  public Class findClass(String className, Class referencePoint)
  {
    try
    {
      ClassLoader cl = referencePoint.getClassLoader();
      if (cl == null) {
        cl = ClassLoader.getSystemClassLoader();
      }
      return cl.loadClass(className);
    }
    catch (ClassNotFoundException e) {}
    return null;
  }
  
  public boolean isBridgeMethod(Method method)
  {
    return method.isBridge();
  }
  
  public boolean isOverriding(Method method, Class base)
  {
    String name = method.getName();
    Class[] params = method.getParameterTypes();
    while (base != null)
    {
      try
      {
        if (base.getDeclaredMethod(name, params) != null) {
          return true;
        }
      }
      catch (NoSuchMethodException e) {}
      base = base.getSuperclass();
    }
    return false;
  }
  
  public boolean isInterface(Class clazz)
  {
    return clazz.isInterface();
  }
  
  public boolean isTransient(Field f)
  {
    return Modifier.isTransient(f.getModifiers());
  }
  
  public boolean isInnerClass(Class clazz)
  {
    return (clazz.getEnclosingClass() != null) && (!Modifier.isStatic(clazz.getModifiers()));
  }
  
  private Type fix(Type t)
  {
    if (!(t instanceof GenericArrayType)) {
      return t;
    }
    GenericArrayType gat = (GenericArrayType)t;
    if ((gat.getGenericComponentType() instanceof Class))
    {
      Class c = (Class)gat.getGenericComponentType();
      return Array.newInstance(c, 0).getClass();
    }
    return t;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\nav\ReflectionNavigator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */