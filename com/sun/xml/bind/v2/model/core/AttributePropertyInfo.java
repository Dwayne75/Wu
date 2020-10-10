package com.sun.xml.bind.v2.model.core;

import javax.xml.namespace.QName;

public abstract interface AttributePropertyInfo<T, C>
  extends PropertyInfo<T, C>, NonElementRef<T, C>
{
  public abstract NonElement<T, C> getTarget();
  
  public abstract boolean isRequired();
  
  public abstract QName getXmlName();
  
  public abstract Adapter<T, C> getAdapter();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\AttributePropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */