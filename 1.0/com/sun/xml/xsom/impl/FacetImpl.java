package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Locator;

public class FacetImpl
  extends ComponentImpl
  implements XSFacet
{
  private final String name;
  private final String value;
  private final ValidationContext context;
  private boolean fixed;
  
  public FacetImpl(SchemaImpl owner, AnnotationImpl _annon, Locator _loc, String _name, String _value, ValidationContext _context, boolean _fixed)
  {
    super(owner, _annon, _loc);
    
    this.name = _name;
    this.value = _value;
    this.context = _context;
    this.fixed = _fixed;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String getValue()
  {
    return this.value;
  }
  
  public ValidationContext getContext()
  {
    return this.context;
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\FacetImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */