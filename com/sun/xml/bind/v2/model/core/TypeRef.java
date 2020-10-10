package com.sun.xml.bind.v2.model.core;

import javax.xml.namespace.QName;

public abstract interface TypeRef<T, C>
  extends NonElementRef<T, C>
{
  public abstract QName getTagName();
  
  public abstract boolean isNillable();
  
  public abstract String getDefaultValue();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\TypeRef.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */