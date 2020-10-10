package com.sun.xml.bind.v2.model.core;

public abstract interface EnumLeafInfo<T, C>
  extends LeafInfo<T, C>
{
  public abstract C getClazz();
  
  public abstract NonElement<T, C> getBaseType();
  
  public abstract Iterable<? extends EnumConstant> getConstants();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\EnumLeafInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */