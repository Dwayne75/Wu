package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.runtime.Transducer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class RuntimeArrayInfoImpl
  extends ArrayInfoImpl<Type, Class, Field, Method>
  implements RuntimeArrayInfo
{
  RuntimeArrayInfoImpl(RuntimeModelBuilder builder, Locatable upstream, Class arrayType)
  {
    super(builder, upstream, arrayType);
  }
  
  public Class getType()
  {
    return (Class)super.getType();
  }
  
  public RuntimeNonElement getItemType()
  {
    return (RuntimeNonElement)super.getItemType();
  }
  
  public <V> Transducer<V> getTransducer()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\RuntimeArrayInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */