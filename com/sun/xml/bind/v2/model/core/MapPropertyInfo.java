package com.sun.xml.bind.v2.model.core;

import javax.xml.namespace.QName;

public abstract interface MapPropertyInfo<T, C>
  extends PropertyInfo<T, C>
{
  public abstract QName getXmlName();
  
  public abstract boolean isCollectionNillable();
  
  public abstract NonElement<T, C> getKeyType();
  
  public abstract NonElement<T, C> getValueType();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\MapPropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */