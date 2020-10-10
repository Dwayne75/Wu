package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class Util
{
  private static XSType[] listDirectSubstitutables(XSType _this)
  {
    ArrayList r = new ArrayList();
    
    Iterator itr = ((SchemaImpl)_this.getOwnerSchema()).parent.iterateTypes();
    while (itr.hasNext())
    {
      XSType t = (XSType)itr.next();
      if (t.getBaseType() == _this) {
        r.add(t);
      }
    }
    return (XSType[])r.toArray(new XSType[r.size()]);
  }
  
  public static XSType[] listSubstitutables(XSType _this)
  {
    Set substitables = new HashSet();
    buildSubstitutables(_this, substitables);
    return (XSType[])substitables.toArray(new XSType[substitables.size()]);
  }
  
  public static void buildSubstitutables(XSType _this, Set substitutables)
  {
    if (_this.isLocal()) {
      return;
    }
    buildSubstitutables(_this, _this, substitutables);
  }
  
  private static void buildSubstitutables(XSType head, XSType _this, Set substitutables)
  {
    if (!isSubstitutable(head, _this)) {
      return;
    }
    if (substitutables.add(_this))
    {
      XSType[] child = listDirectSubstitutables(_this);
      for (int i = 0; i < child.length; i++) {
        buildSubstitutables(head, child[i], substitutables);
      }
    }
  }
  
  private static boolean isSubstitutable(XSType _base, XSType derived)
  {
    if (_base.isComplexType())
    {
      XSComplexType base = _base.asComplexType();
      for (; base != derived; derived = derived.getBaseType()) {
        if (base.isSubstitutionProhibited(derived.getDerivationMethod())) {
          return false;
        }
      }
      return true;
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\Util.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */