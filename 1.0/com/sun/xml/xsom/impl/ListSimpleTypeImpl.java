package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import org.xml.sax.Locator;

public class ListSimpleTypeImpl
  extends SimpleTypeImpl
  implements XSListSimpleType
{
  private final Ref.SimpleType itemType;
  
  public ListSimpleTypeImpl(SchemaImpl _parent, AnnotationImpl _annon, Locator _loc, String _name, boolean _anonymous, Ref.SimpleType _itemType)
  {
    super(_parent, _annon, _loc, _name, _anonymous, _parent.parent.anySimpleType);
    
    this.itemType = _itemType;
  }
  
  public XSSimpleType getItemType()
  {
    return this.itemType.getSimpleType();
  }
  
  public void visit(XSSimpleTypeVisitor visitor)
  {
    visitor.listSimpleType(this);
  }
  
  public Object apply(XSSimpleTypeFunction function)
  {
    return function.listSimpleType(this);
  }
  
  public XSFacet getFacet(String name)
  {
    return null;
  }
  
  public XSVariety getVariety()
  {
    return XSVariety.LIST;
  }
  
  public boolean isList()
  {
    return true;
  }
  
  public XSListSimpleType asList()
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\ListSimpleTypeImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */