package com.sun.xml.bind.v2.model.core;

import java.util.Collection;

public abstract interface ElementInfo<T, C>
  extends Element<T, C>
{
  public abstract ElementPropertyInfo<T, C> getProperty();
  
  public abstract NonElement<T, C> getContentType();
  
  public abstract T getContentInMemoryType();
  
  public abstract T getType();
  
  public abstract ElementInfo<T, C> getSubstitutionHead();
  
  public abstract Collection<? extends ElementInfo<T, C>> getSubstitutionMembers();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\ElementInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */