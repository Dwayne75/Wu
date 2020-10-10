package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import java.util.Iterator;
import java.util.Set;
import org.xml.sax.Locator;

public class UnionSimpleTypeImpl
  extends SimpleTypeImpl
  implements XSUnionSimpleType
{
  private final Ref.SimpleType[] memberTypes;
  
  public UnionSimpleTypeImpl(SchemaDocumentImpl _parent, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, String _name, boolean _anonymous, Set<XSVariety> finalSet, Ref.SimpleType[] _members)
  {
    super(_parent, _annon, _loc, _fa, _name, _anonymous, finalSet, _parent.getSchema().parent.anySimpleType);
    
    this.memberTypes = _members;
  }
  
  public XSSimpleType getMember(int idx)
  {
    return this.memberTypes[idx].getType();
  }
  
  public int getMemberSize()
  {
    return this.memberTypes.length;
  }
  
  public Iterator<XSSimpleType> iterator()
  {
    new Iterator()
    {
      int idx = 0;
      
      public boolean hasNext()
      {
        return this.idx < UnionSimpleTypeImpl.this.memberTypes.length;
      }
      
      public XSSimpleType next()
      {
        return UnionSimpleTypeImpl.this.memberTypes[(this.idx++)].getType();
      }
      
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  public void visit(XSSimpleTypeVisitor visitor)
  {
    visitor.unionSimpleType(this);
  }
  
  public Object apply(XSSimpleTypeFunction function)
  {
    return function.unionSimpleType(this);
  }
  
  public XSUnionSimpleType getBaseUnionType()
  {
    return this;
  }
  
  public XSFacet getFacet(String name)
  {
    return null;
  }
  
  public XSVariety getVariety()
  {
    return XSVariety.LIST;
  }
  
  public XSSimpleType getPrimitiveType()
  {
    return null;
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\UnionSimpleTypeImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */