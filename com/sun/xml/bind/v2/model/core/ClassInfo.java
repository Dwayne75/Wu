package com.sun.xml.bind.v2.model.core;

import java.util.List;

public abstract interface ClassInfo<T, C>
  extends MaybeElement<T, C>
{
  public abstract ClassInfo<T, C> getBaseClass();
  
  public abstract C getClazz();
  
  public abstract String getName();
  
  public abstract List<? extends PropertyInfo<T, C>> getProperties();
  
  public abstract boolean hasValueProperty();
  
  public abstract PropertyInfo<T, C> getProperty(String paramString);
  
  public abstract boolean hasProperties();
  
  public abstract boolean isAbstract();
  
  public abstract boolean isOrdered();
  
  public abstract boolean isFinal();
  
  public abstract boolean hasSubClasses();
  
  public abstract boolean hasAttributeWildcard();
  
  public abstract boolean inheritsAttributeWildcard();
  
  public abstract boolean declaresAttributeWildcard();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\ClassInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */