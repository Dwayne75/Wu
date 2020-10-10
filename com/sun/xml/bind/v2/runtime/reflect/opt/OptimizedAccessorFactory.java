package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.v2.bytecode.ClassTailor;
import com.sun.xml.bind.v2.runtime.RuntimeUtil;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class OptimizedAccessorFactory
{
  private static final Logger logger = ;
  private static final String fieldTemplateName;
  private static final String methodTemplateName;
  
  static
  {
    String s = FieldAccessor_Byte.class.getName();
    fieldTemplateName = s.substring(0, s.length() - "Byte".length()).replace('.', '/');
    
    s = MethodAccessor_Byte.class.getName();
    methodTemplateName = s.substring(0, s.length() - "Byte".length()).replace('.', '/');
  }
  
  public static final <B, V> Accessor<B, V> get(Method getter, Method setter)
  {
    if (getter.getParameterTypes().length != 0) {
      return null;
    }
    Class<?>[] sparams = setter.getParameterTypes();
    if (sparams.length != 1) {
      return null;
    }
    if (sparams[0] != getter.getReturnType()) {
      return null;
    }
    if (setter.getReturnType() != Void.TYPE) {
      return null;
    }
    if (getter.getDeclaringClass() != setter.getDeclaringClass()) {
      return null;
    }
    if ((Modifier.isPrivate(getter.getModifiers())) || (Modifier.isPrivate(setter.getModifiers()))) {
      return null;
    }
    Class t = sparams[0];
    String typeName = t.getName().replace('.', '_');
    
    String newClassName = ClassTailor.toVMClassName(getter.getDeclaringClass()) + "$JaxbAccessorM_" + getter.getName() + '_' + setter.getName() + '_' + typeName;
    Class opt;
    Class opt;
    if (t.isPrimitive()) {
      opt = AccessorInjector.prepare(getter.getDeclaringClass(), methodTemplateName + ((Class)RuntimeUtil.primitiveToBox.get(t)).getSimpleName(), newClassName, new String[] { ClassTailor.toVMClassName(Bean.class), ClassTailor.toVMClassName(getter.getDeclaringClass()), "get_" + t.getName(), getter.getName(), "set_" + t.getName(), setter.getName() });
    } else {
      opt = AccessorInjector.prepare(getter.getDeclaringClass(), methodTemplateName + "Ref", newClassName, new String[] { ClassTailor.toVMClassName(Bean.class), ClassTailor.toVMClassName(getter.getDeclaringClass()), ClassTailor.toVMClassName(Ref.class), ClassTailor.toVMClassName(t), "()" + ClassTailor.toVMTypeName(Ref.class), "()" + ClassTailor.toVMTypeName(t), '(' + ClassTailor.toVMTypeName(Ref.class) + ")V", '(' + ClassTailor.toVMTypeName(t) + ")V", "get_ref", getter.getName(), "set_ref", setter.getName() });
    }
    if (opt == null) {
      return null;
    }
    Accessor<B, V> acc = instanciate(opt);
    if (acc != null) {
      logger.log(Level.FINE, "Using optimized Accessor for " + getter + " and " + setter);
    }
    return acc;
  }
  
  public static final <B, V> Accessor<B, V> get(Field field)
  {
    int mods = field.getModifiers();
    if ((Modifier.isPrivate(mods)) || (Modifier.isFinal(mods))) {
      return null;
    }
    String newClassName = ClassTailor.toVMClassName(field.getDeclaringClass()) + "$JaxbAccessorF_" + field.getName();
    Class opt;
    Class opt;
    if (field.getType().isPrimitive()) {
      opt = AccessorInjector.prepare(field.getDeclaringClass(), fieldTemplateName + ((Class)RuntimeUtil.primitiveToBox.get(field.getType())).getSimpleName(), newClassName, new String[] { ClassTailor.toVMClassName(Bean.class), ClassTailor.toVMClassName(field.getDeclaringClass()), "f_" + field.getType().getName(), field.getName() });
    } else {
      opt = AccessorInjector.prepare(field.getDeclaringClass(), fieldTemplateName + "Ref", newClassName, new String[] { ClassTailor.toVMClassName(Bean.class), ClassTailor.toVMClassName(field.getDeclaringClass()), ClassTailor.toVMClassName(Ref.class), ClassTailor.toVMClassName(field.getType()), ClassTailor.toVMTypeName(Ref.class), ClassTailor.toVMTypeName(field.getType()), "f_ref", field.getName() });
    }
    if (opt == null) {
      return null;
    }
    Accessor<B, V> acc = instanciate(opt);
    if (acc != null) {
      logger.log(Level.FINE, "Using optimized Accessor for " + field);
    }
    return acc;
  }
  
  private static <B, V> Accessor<B, V> instanciate(Class opt)
  {
    try
    {
      return (Accessor)opt.newInstance();
    }
    catch (InstantiationException e)
    {
      logger.log(Level.INFO, "failed to load an optimized Accessor", e);
    }
    catch (IllegalAccessException e)
    {
      logger.log(Level.INFO, "failed to load an optimized Accessor", e);
    }
    catch (SecurityException e)
    {
      logger.log(Level.INFO, "failed to load an optimized Accessor", e);
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\OptimizedAccessorFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */