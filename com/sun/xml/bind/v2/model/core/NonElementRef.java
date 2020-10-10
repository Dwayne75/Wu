package com.sun.xml.bind.v2.model.core;

public abstract interface NonElementRef<T, C>
{
  public abstract NonElement<T, C> getTarget();
  
  public abstract PropertyInfo<T, C> getSource();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\NonElementRef.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */