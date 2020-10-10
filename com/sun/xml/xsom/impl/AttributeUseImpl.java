package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XmlString;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

public class AttributeUseImpl
  extends ComponentImpl
  implements XSAttributeUse
{
  private final Ref.Attribute att;
  private final XmlString defaultValue;
  private final XmlString fixedValue;
  private final boolean required;
  
  public AttributeUseImpl(SchemaDocumentImpl owner, AnnotationImpl ann, Locator loc, ForeignAttributesImpl fa, Ref.Attribute _decl, XmlString def, XmlString fixed, boolean req)
  {
    super(owner, ann, loc, fa);
    
    this.att = _decl;
    this.defaultValue = def;
    this.fixedValue = fixed;
    this.required = req;
  }
  
  public XSAttributeDecl getDecl()
  {
    return this.att.getAttribute();
  }
  
  public XmlString getDefaultValue()
  {
    if (this.defaultValue != null) {
      return this.defaultValue;
    }
    return getDecl().getDefaultValue();
  }
  
  public XmlString getFixedValue()
  {
    if (this.fixedValue != null) {
      return this.fixedValue;
    }
    return getDecl().getFixedValue();
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\AttributeUseImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */