package com.sun.xml.bind.v2.model.core;

import java.util.Set;

public abstract interface RegistryInfo<T, C>
{
  public abstract Set<TypeInfo<T, C>> getReferences();
  
  public abstract C getClazz();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\RegistryInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */