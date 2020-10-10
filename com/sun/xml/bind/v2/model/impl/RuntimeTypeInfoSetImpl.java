package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import javax.xml.namespace.QName;

final class RuntimeTypeInfoSetImpl
  extends TypeInfoSetImpl<Type, Class, Field, Method>
  implements RuntimeTypeInfoSet
{
  public RuntimeTypeInfoSetImpl(AnnotationReader<Type, Class, Field, Method> reader)
  {
    super(Navigator.REFLECTION, reader, RuntimeBuiltinLeafInfoImpl.LEAVES);
  }
  
  protected RuntimeNonElement createAnyType()
  {
    return RuntimeAnyTypeImpl.theInstance;
  }
  
  public ReflectionNavigator getNavigator()
  {
    return (ReflectionNavigator)super.getNavigator();
  }
  
  public RuntimeNonElement getTypeInfo(Type type)
  {
    return (RuntimeNonElement)super.getTypeInfo(type);
  }
  
  public RuntimeNonElement getAnyTypeInfo()
  {
    return (RuntimeNonElement)super.getAnyTypeInfo();
  }
  
  public RuntimeNonElement getClassInfo(Class clazz)
  {
    return (RuntimeNonElement)super.getClassInfo(clazz);
  }
  
  public Map<Class, RuntimeClassInfoImpl> beans()
  {
    return super.beans();
  }
  
  public Map<Type, RuntimeBuiltinLeafInfoImpl<?>> builtins()
  {
    return super.builtins();
  }
  
  public Map<Class, RuntimeEnumLeafInfoImpl<?, ?>> enums()
  {
    return super.enums();
  }
  
  public Map<Class, RuntimeArrayInfoImpl> arrays()
  {
    return super.arrays();
  }
  
  public RuntimeElementInfoImpl getElementInfo(Class scope, QName name)
  {
    return (RuntimeElementInfoImpl)super.getElementInfo(scope, name);
  }
  
  public Map<QName, RuntimeElementInfoImpl> getElementMappings(Class scope)
  {
    return super.getElementMappings(scope);
  }
  
  public Iterable<RuntimeElementInfoImpl> getAllElements()
  {
    return super.getAllElements();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\RuntimeTypeInfoSetImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */