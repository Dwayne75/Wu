package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.xml.sax.Locator;

public class RestrictionSimpleTypeImpl
  extends SimpleTypeImpl
  implements XSRestrictionSimpleType
{
  public RestrictionSimpleTypeImpl(SchemaDocumentImpl _parent, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, String _name, boolean _anonymous, Set<XSVariety> finalSet, Ref.SimpleType _baseType)
  {
    super(_parent, _annon, _loc, _fa, _name, _anonymous, finalSet, _baseType);
  }
  
  private final List<XSFacet> facets = new ArrayList();
  
  public void addFacet(XSFacet facet)
  {
    this.facets.add(facet);
  }
  
  public Iterator<XSFacet> iterateDeclaredFacets()
  {
    return this.facets.iterator();
  }
  
  public Collection<? extends XSFacet> getDeclaredFacets()
  {
    return this.facets;
  }
  
  public XSFacet getDeclaredFacet(String name)
  {
    int len = this.facets.size();
    for (int i = 0; i < len; i++)
    {
      XSFacet f = (XSFacet)this.facets.get(i);
      if (f.getName().equals(name)) {
        return f;
      }
    }
    return null;
  }
  
  public List<XSFacet> getDeclaredFacets(String name)
  {
    List<XSFacet> r = new ArrayList();
    for (XSFacet f : this.facets) {
      if (f.getName().equals(name)) {
        r.add(f);
      }
    }
    return r;
  }
  
  public XSFacet getFacet(String name)
  {
    XSFacet f = getDeclaredFacet(name);
    if (f != null) {
      return f;
    }
    return getSimpleBaseType().getFacet(name);
  }
  
  public XSVariety getVariety()
  {
    return getSimpleBaseType().getVariety();
  }
  
  public XSSimpleType getPrimitiveType()
  {
    if (isPrimitive()) {
      return this;
    }
    return getSimpleBaseType().getPrimitiveType();
  }
  
  public boolean isPrimitive()
  {
    return getSimpleBaseType() == getOwnerSchema().getRoot().anySimpleType;
  }
  
  public void visit(XSSimpleTypeVisitor visitor)
  {
    visitor.restrictionSimpleType(this);
  }
  
  public Object apply(XSSimpleTypeFunction function)
  {
    return function.restrictionSimpleType(this);
  }
  
  public boolean isRestriction()
  {
    return true;
  }
  
  public XSRestrictionSimpleType asRestriction()
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\RestrictionSimpleTypeImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */