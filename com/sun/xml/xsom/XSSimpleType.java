package com.sun.xml.xsom;

import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;

public abstract interface XSSimpleType
  extends XSType, XSContentType
{
  public abstract XSSimpleType getSimpleBaseType();
  
  public abstract XSVariety getVariety();
  
  public abstract XSSimpleType getPrimitiveType();
  
  public abstract boolean isPrimitive();
  
  public abstract XSListSimpleType getBaseListType();
  
  public abstract XSUnionSimpleType getBaseUnionType();
  
  public abstract boolean isFinal(XSVariety paramXSVariety);
  
  public abstract XSSimpleType getRedefinedBy();
  
  public abstract XSFacet getFacet(String paramString);
  
  public abstract void visit(XSSimpleTypeVisitor paramXSSimpleTypeVisitor);
  
  public abstract <T> T apply(XSSimpleTypeFunction<T> paramXSSimpleTypeFunction);
  
  public abstract boolean isRestriction();
  
  public abstract boolean isList();
  
  public abstract boolean isUnion();
  
  public abstract XSRestrictionSimpleType asRestriction();
  
  public abstract XSListSimpleType asList();
  
  public abstract XSUnionSimpleType asUnion();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSSimpleType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */