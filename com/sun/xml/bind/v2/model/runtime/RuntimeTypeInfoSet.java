package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import javax.xml.namespace.QName;

public abstract interface RuntimeTypeInfoSet
  extends TypeInfoSet<Type, Class, Field, Method>
{
  public abstract Map<Class, ? extends RuntimeArrayInfo> arrays();
  
  public abstract Map<Class, ? extends RuntimeClassInfo> beans();
  
  public abstract Map<Type, ? extends RuntimeBuiltinLeafInfo> builtins();
  
  public abstract Map<Class, ? extends RuntimeEnumLeafInfo> enums();
  
  public abstract RuntimeNonElement getTypeInfo(Type paramType);
  
  public abstract RuntimeNonElement getAnyTypeInfo();
  
  public abstract RuntimeNonElement getClassInfo(Class paramClass);
  
  public abstract RuntimeElementInfo getElementInfo(Class paramClass, QName paramQName);
  
  public abstract Map<QName, ? extends RuntimeElementInfo> getElementMappings(Class paramClass);
  
  public abstract Iterable<? extends RuntimeElementInfo> getAllElements();
  
  public abstract ReflectionNavigator getNavigator();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\runtime\RuntimeTypeInfoSet.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */