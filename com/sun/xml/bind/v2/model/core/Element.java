package com.sun.xml.bind.v2.model.core;

import javax.xml.namespace.QName;

public abstract interface Element<T, C>
  extends TypeInfo<T, C>
{
  public abstract QName getElementName();
  
  public abstract Element<T, C> getSubstitutionHead();
  
  public abstract ClassInfo<T, C> getScope();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\Element.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */