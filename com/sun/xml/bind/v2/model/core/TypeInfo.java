package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.annotation.Locatable;

public abstract interface TypeInfo<T, C>
  extends Locatable
{
  public abstract T getType();
  
  public abstract boolean canBeReferencedByIDREF();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\TypeInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */