package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;
import java.lang.reflect.Type;

public abstract interface RuntimeValuePropertyInfo
  extends ValuePropertyInfo<Type, Class>, RuntimePropertyInfo, RuntimeNonElementRef
{
  public abstract RuntimeNonElement getTarget();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\runtime\RuntimeValuePropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */