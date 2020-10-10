package com.sun.xml.bind;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.Accessor.FieldReflection;
import com.sun.xml.bind.v2.runtime.reflect.Accessor.GetterOnlyReflection;
import com.sun.xml.bind.v2.runtime.reflect.Accessor.GetterSetterReflection;
import com.sun.xml.bind.v2.runtime.reflect.Accessor.ReadOnlyFieldReflection;
import com.sun.xml.bind.v2.runtime.reflect.Accessor.SetterOnlyReflection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AccessorFactoryImpl
  implements AccessorFactory
{
  private static AccessorFactoryImpl instance = new AccessorFactoryImpl();
  
  public static AccessorFactoryImpl getInstance()
  {
    return instance;
  }
  
  public Accessor createFieldAccessor(Class bean, Field field, boolean readOnly)
  {
    return readOnly ? new Accessor.ReadOnlyFieldReflection(field) : new Accessor.FieldReflection(field);
  }
  
  public Accessor createPropertyAccessor(Class bean, Method getter, Method setter)
  {
    if (getter == null) {
      return new Accessor.SetterOnlyReflection(setter);
    }
    if (setter == null) {
      return new Accessor.GetterOnlyReflection(getter);
    }
    return new Accessor.GetterSetterReflection(getter, setter);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\AccessorFactoryImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */