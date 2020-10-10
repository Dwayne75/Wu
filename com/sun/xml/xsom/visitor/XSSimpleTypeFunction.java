package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;

public abstract interface XSSimpleTypeFunction<T>
{
  public abstract T listSimpleType(XSListSimpleType paramXSListSimpleType);
  
  public abstract T unionSimpleType(XSUnionSimpleType paramXSUnionSimpleType);
  
  public abstract T restrictionSimpleType(XSRestrictionSimpleType paramXSRestrictionSimpleType);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\visitor\XSSimpleTypeFunction.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */