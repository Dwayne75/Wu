package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.runtime.Transducer;
import java.lang.reflect.Type;

public abstract interface RuntimeNonElement
  extends NonElement<Type, Class>, RuntimeTypeInfo
{
  public abstract <V> Transducer<V> getTransducer();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\runtime\RuntimeNonElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */