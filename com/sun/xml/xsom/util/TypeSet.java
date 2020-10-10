package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSType;

public abstract class TypeSet
{
  public abstract boolean contains(XSType paramXSType);
  
  public static TypeSet intersection(TypeSet a, final TypeSet b)
  {
    new TypeSet()
    {
      public boolean contains(XSType type)
      {
        return (this.val$a.contains(type)) && (b.contains(type));
      }
    };
  }
  
  public static TypeSet union(TypeSet a, final TypeSet b)
  {
    new TypeSet()
    {
      public boolean contains(XSType type)
      {
        return (this.val$a.contains(type)) || (b.contains(type));
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\util\TypeSet.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */