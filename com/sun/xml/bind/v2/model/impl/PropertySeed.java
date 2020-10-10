package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.annotation.Locatable;

abstract interface PropertySeed<T, C, F, M>
  extends Locatable, AnnotationSource
{
  public abstract String getName();
  
  public abstract T getRawType();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\PropertySeed.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */