package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Locator;

public class AttributeDeclImpl
  extends DeclarationImpl
  implements XSAttributeDecl, Ref.Attribute
{
  private final Ref.SimpleType type;
  private final ValidationContext context;
  private final String defaultValue;
  private final String fixedValue;
  
  public AttributeDeclImpl(SchemaImpl owner, String _targetNamespace, String _name, AnnotationImpl _annon, Locator _loc, boolean _anonymous, String _defValue, String _fixedValue, ValidationContext _context, Ref.SimpleType _type)
  {
    super(owner, _annon, _loc, _targetNamespace, _name, _anonymous);
    if (_name == null) {
      throw new IllegalArgumentException();
    }
    this.defaultValue = _defValue;
    this.fixedValue = _fixedValue;
    this.context = _context;
    this.type = _type;
  }
  
  public XSSimpleType getType()
  {
    return this.type.getSimpleType();
  }
  
  public ValidationContext getContext()
  {
    return this.context;
  }
  
  public String getDefaultValue()
  {
    return this.defaultValue;
  }
  
  public String getFixedValue()
  {
    return this.fixedValue;
  }
  
  public void visit(XSVisitor visitor)
  {
    visitor.attributeDecl(this);
  }
  
  public Object apply(XSFunction function)
  {
    return function.attributeDecl(this);
  }
  
  public XSAttributeDecl getAttribute()
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\AttributeDeclImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */