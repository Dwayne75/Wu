package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public abstract interface RuntimeElementPropertyInfo
  extends ElementPropertyInfo<Type, Class>, RuntimePropertyInfo
{
  public abstract Collection<? extends RuntimeTypeInfo> ref();
  
  public abstract List<? extends RuntimeTypeRef> getTypes();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\runtime\RuntimeElementPropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */