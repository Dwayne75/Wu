package com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class RuntimeEnumConstantImpl
  extends EnumConstantImpl<Type, Class, Field, Method>
{
  public RuntimeEnumConstantImpl(RuntimeEnumLeafInfoImpl owner, String name, String lexical, EnumConstantImpl<Type, Class, Field, Method> next)
  {
    super(owner, name, lexical, next);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\RuntimeEnumConstantImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */