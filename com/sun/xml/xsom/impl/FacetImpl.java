package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XmlString;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

public class FacetImpl
  extends ComponentImpl
  implements XSFacet
{
  private final String name;
  private final XmlString value;
  private boolean fixed;
  
  public FacetImpl(SchemaDocumentImpl owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, String _name, XmlString _value, boolean _fixed)
  {
    super(owner, _annon, _loc, _fa);
    
    this.name = _name;
    this.value = _value;
    this.fixed = _fixed;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public XmlString getValue()
  {
    return this.value;
  }
  
  public boolean isFixed()
  {
    return this.fixed;
  }
  
  public void visit(XSVisitor visitor)
  {
    visitor.facet(this);
  }
  
  public Object apply(XSFunction function)
  {
    return function.facet(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\FacetImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */