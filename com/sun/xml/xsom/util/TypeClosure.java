package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSType;

public class TypeClosure
  extends TypeSet
{
  private final TypeSet typeSet;
  
  public TypeClosure(TypeSet typeSet)
  {
    this.typeSet = typeSet;
  }
  
  public boolean contains(XSType type)
  {
    if (this.typeSet.contains(type)) {
      return true;
    }
    XSType baseType = type.getBaseType();
    if (baseType == null) {
      return false;
    }
    return contains(baseType);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\util\TypeClosure.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */