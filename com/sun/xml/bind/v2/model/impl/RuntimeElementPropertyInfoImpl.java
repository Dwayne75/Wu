package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import javax.xml.namespace.QName;

class RuntimeElementPropertyInfoImpl
  extends ElementPropertyInfoImpl<Type, Class, Field, Method>
  implements RuntimeElementPropertyInfo
{
  private final Accessor acc;
  
  RuntimeElementPropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type, Class, Field, Method> seed)
  {
    super(classInfo, seed);
    Accessor rawAcc = ((RuntimeClassInfoImpl.RuntimePropertySeed)seed).getAccessor();
    if ((getAdapter() != null) && (!isCollection())) {
      rawAcc = rawAcc.adapt(getAdapter());
    }
    this.acc = rawAcc;
  }
  
  public Accessor getAccessor()
  {
    return this.acc;
  }
  
  public boolean elementOnlyContent()
  {
    return true;
  }
  
  public List<? extends RuntimeTypeInfo> ref()
  {
    return super.ref();
  }
  
  protected RuntimeTypeRefImpl createTypeRef(QName name, Type type, boolean isNillable, String defaultValue)
  {
    return new RuntimeTypeRefImpl(this, name, type, isNillable, defaultValue);
  }
  
  public List<RuntimeTypeRefImpl> getTypes()
  {
    return super.getTypes();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\RuntimeElementPropertyInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */