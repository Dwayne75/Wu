package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import org.xml.sax.Locator;

public class UnionSimpleTypeImpl
  extends SimpleTypeImpl
  implements XSUnionSimpleType
{
  private final Ref.SimpleType[] memberTypes;
  
  public UnionSimpleTypeImpl(SchemaImpl _parent, AnnotationImpl _annon, Locator _loc, String _name, boolean _anonymous, Ref.SimpleType[] _members)
  {
    super(_parent, _annon, _loc, _name, _anonymous, _parent.parent.anySimpleType);
    
    this.memberTypes = _members;
  }
  
  public XSSimpleType getMember(int idx)
  {
    return this.memberTypes[idx].getSimpleType();
  }
  
  public int getMemberSize()
  {
    return this.memberTypes.length;
  }
  
  public void visit(XSSimpleTypeVisitor visitor)
  {
    visitor.unionSimpleType(this);
  }
  
  public Object apply(XSSimpleTypeFunction function)
  {
    return function.unionSimpleType(this);
  }
  
  public XSFacet getFacet(String name)
  {
    return null;
  }
  
  public XSVariety getVariety()
  {
    return XSVariety.LIST;
  }
  
  public boolean isUnion()
  {
    return true;
  }
  
  public XSUnionSimpleType asUnion()
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\UnionSimpleTypeImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */