package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.TypeRef;
import java.lang.reflect.Type;

public abstract interface RuntimeTypeRef
  extends TypeRef<Type, Class>, RuntimeNonElementRef
{
  public abstract RuntimeNonElement getTarget();
  
  public abstract RuntimePropertyInfo getSource();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\runtime\RuntimeTypeRef.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */