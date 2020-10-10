package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.runtime.Transducer;
import java.lang.reflect.Type;

final class RuntimeAnyTypeImpl
  extends AnyTypeImpl<Type, Class>
  implements RuntimeNonElement
{
  private RuntimeAnyTypeImpl()
  {
    super(Navigator.REFLECTION);
  }
  
  public <V> Transducer<V> getTransducer()
  {
    return null;
  }
  
  static final RuntimeNonElement theInstance = new RuntimeAnyTypeImpl();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\RuntimeAnyTypeImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */