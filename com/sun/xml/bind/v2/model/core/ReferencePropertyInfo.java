package com.sun.xml.bind.v2.model.core;

import java.util.Collection;
import java.util.Set;
import javax.xml.namespace.QName;

public abstract interface ReferencePropertyInfo<T, C>
  extends PropertyInfo<T, C>
{
  public abstract Set<? extends Element<T, C>> getElements();
  
  public abstract Collection<? extends TypeInfo<T, C>> ref();
  
  public abstract QName getXmlName();
  
  public abstract boolean isCollectionNillable();
  
  public abstract boolean isCollectionRequired();
  
  public abstract boolean isMixed();
  
  public abstract WildcardMode getWildcard();
  
  public abstract C getDOMHandler();
  
  public abstract Adapter<T, C> getAdapter();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\ReferencePropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */