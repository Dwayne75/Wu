package com.sun.xml.bind.v2.model.core;

public abstract interface ValuePropertyInfo<T, C>
  extends PropertyInfo<T, C>, NonElementRef<T, C>
{
  public abstract Adapter<T, C> getAdapter();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\ValuePropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */