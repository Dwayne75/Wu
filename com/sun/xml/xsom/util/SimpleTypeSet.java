package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSType;
import java.util.Set;

public class SimpleTypeSet
  extends TypeSet
{
  private final Set typeSet;
  
  public SimpleTypeSet(Set s)
  {
    this.typeSet = s;
  }
  
  public boolean contains(XSType type)
  {
    return this.typeSet.contains(type);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\util\SimpleTypeSet.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */