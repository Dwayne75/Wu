package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.MapPropertyInfo;
import java.lang.reflect.Type;

public abstract interface RuntimeMapPropertyInfo
  extends RuntimePropertyInfo, MapPropertyInfo<Type, Class>
{
  public abstract RuntimeNonElement getKeyType();
  
  public abstract RuntimeNonElement getValueType();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\runtime\RuntimeMapPropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */