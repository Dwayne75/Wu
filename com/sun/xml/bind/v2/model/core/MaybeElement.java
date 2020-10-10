package com.sun.xml.bind.v2.model.core;

import javax.xml.namespace.QName;

public abstract interface MaybeElement<T, C>
  extends NonElement<T, C>
{
  public abstract boolean isElement();
  
  public abstract QName getElementName();
  
  public abstract Element<T, C> asElement();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\MaybeElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */