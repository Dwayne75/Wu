package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Locator;

public class AttributeUseImpl
  extends ComponentImpl
  implements XSAttributeUse
{
  private final Ref.Attribute att;
  private final String defaultValue;
  private final String fixedValue;
  private final ValidationContext context;
  private final boolean required;
  
  public AttributeUseImpl(SchemaImpl owner, AnnotationImpl ann, Locator loc, Ref.Attribute _decl, String def, String fixed, ValidationContext _context, boolean req)
  {
    super(owner, ann, loc);
    
    this.att = _decl;
    this.defaultValue = def;
    this.fixedValue = fixed;
    this.context = _context;
    this.required = req;
  }
  
  public XSAttributeDecl getDecl()
  {
    return this.att.getAttribute();
  }
  
  public String getDefaultValue()
  {
    if (this.defaultValue != null) {
      return this.defaultValue;
    }
    return getDecl().getDefaultValue();
  }
  
  public String getFixedValue()
  {
    if (this.fixedValue != null) {
      return this.fixedValue;
    }
    return getDecl().getFixedValue();
  }
  
  public ValidationContext getContext()
  {
    if ((this.fixedValue != null) || (this.defaultValue != null)) {
      return this.context;
    }
    return getDecl().getContext();
  }
  
  public boolean isRequired()
  {
    return this.required;
  }
  
  public Object apply(XSFunction f)
  {
    return f.attributeUse(this);
  }
  
  public void visit(XSVisitor v)
  {
    v.attributeUse(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\AttributeUseImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */