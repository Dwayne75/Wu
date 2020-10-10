package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import java.lang.reflect.Type;
import java.util.Set;

public abstract interface RuntimeReferencePropertyInfo
  extends ReferencePropertyInfo<Type, Class>, RuntimePropertyInfo
{
  public abstract Set<? extends RuntimeElement> getElements();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\runtime\RuntimeReferencePropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */