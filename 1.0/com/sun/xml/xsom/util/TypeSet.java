package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSType;

public abstract class TypeSet
{
  public abstract boolean contains(XSType paramXSType);
  
  public static TypeSet intersection(TypeSet a, TypeSet b)
  {
    return new TypeSet.1(a, b);
  }
  
  public static TypeSet union(TypeSet a, TypeSet b)
  {
    return new TypeSet.2(a, b);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\util\TypeSet.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */