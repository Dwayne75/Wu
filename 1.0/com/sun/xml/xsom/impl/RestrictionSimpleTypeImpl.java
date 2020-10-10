package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import java.util.Iterator;
import java.util.Vector;
import org.xml.sax.Locator;

public class RestrictionSimpleTypeImpl
  extends SimpleTypeImpl
  implements XSRestrictionSimpleType
{
  public RestrictionSimpleTypeImpl(SchemaImpl _parent, AnnotationImpl _annon, Locator _loc, String _name, boolean _anonymous, Ref.SimpleType _baseType)
  {
    super(_parent, _annon, _loc, _name, _anonymous, _baseType);
  }
  
  private final Vector facets = new Vector();
  
  public void addFacet(XSFacet facet)
  {
    this.facets.add(facet);
  }
  
  public Iterator iterateDeclaredFacets()
  {
    return this.facets.iterator();
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\RestrictionSimpleTypeImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */