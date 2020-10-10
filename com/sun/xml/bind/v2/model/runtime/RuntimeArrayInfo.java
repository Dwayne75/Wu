package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.ArrayInfo;
import java.lang.reflect.Type;

public abstract interface RuntimeArrayInfo
  extends ArrayInfo<Type, Class>, RuntimeNonElement
{
  public abstract Class getType();
  
  public abstract RuntimeNonElement getItemType();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\runtime\RuntimeArrayInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */