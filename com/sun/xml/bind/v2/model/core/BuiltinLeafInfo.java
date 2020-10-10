package com.sun.xml.bind.v2.model.core;

import javax.xml.namespace.QName;

public abstract interface BuiltinLeafInfo<T, C>
  extends LeafInfo<T, C>
{
  public abstract QName getTypeName();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\BuiltinLeafInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */