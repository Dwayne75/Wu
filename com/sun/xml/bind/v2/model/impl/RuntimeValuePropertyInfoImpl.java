package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

final class RuntimeValuePropertyInfoImpl
  extends ValuePropertyInfoImpl<Type, Class, Field, Method>
  implements RuntimeValuePropertyInfo
{
  RuntimeValuePropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type, Class, Field, Method> seed)
  {
    super(classInfo, seed);
  }
  
  public boolean elementOnlyContent()
  {
    return false;
  }
  
  public RuntimePropertyInfo getSource()
  {
    return (RuntimePropertyInfo)super.getSource();
  }
  
  public RuntimeNonElement getTarget()
  {
    return (RuntimeNonElement)super.getTarget();
  }
  
  public List<? extends RuntimeNonElement> ref()
  {
    return super.ref();
  }
  
  public void link()
  {
    getTransducer();
    super.link();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\RuntimeValuePropertyInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */