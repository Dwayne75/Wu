package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;

public abstract interface XSSimpleTypeFunction
{
  public abstract Object listSimpleType(XSListSimpleType paramXSListSimpleType);
  
  public abstract Object unionSimpleType(XSUnionSimpleType paramXSUnionSimpleType);
  
  public abstract Object restrictionSimpleType(XSRestrictionSimpleType paramXSRestrictionSimpleType);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\visitor\XSSimpleTypeFunction.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */