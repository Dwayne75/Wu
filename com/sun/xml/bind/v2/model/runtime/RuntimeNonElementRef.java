package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.NonElementRef;
import com.sun.xml.bind.v2.runtime.Transducer;
import java.lang.reflect.Type;

public abstract interface RuntimeNonElementRef
  extends NonElementRef<Type, Class>
{
  public abstract RuntimeNonElement getTarget();
  
  public abstract RuntimePropertyInfo getSource();
  
  public abstract Transducer getTransducer();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\runtime\RuntimeNonElementRef.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */