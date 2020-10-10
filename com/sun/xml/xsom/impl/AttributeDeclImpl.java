package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XmlString;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

public class AttributeDeclImpl
  extends DeclarationImpl
  implements XSAttributeDecl, Ref.Attribute
{
  private final Ref.SimpleType type;
  private final XmlString defaultValue;
  private final XmlString fixedValue;
  
  public AttributeDeclImpl(SchemaDocumentImpl owner, String _targetNamespace, String _name, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, boolean _anonymous, XmlString _defValue, XmlString _fixedValue, Ref.SimpleType _type)
  {
    super(owner, _annon, _loc, _fa, _targetNamespace, _name, _anonymous);
    if (_name == null) {
      throw new IllegalArgumentException();
    }
    this.defaultValue = _defValue;
    this.fixedValue = _fixedValue;
    this.type = _type;
  }
  
  public XSSimpleType getType()
  {
    return this.type.getType();
  }
  
  public XmlString getDefaultValue()
  {
    return this.defaultValue;
  }
  
  public XmlString getFixedValue()
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\AttributeDeclImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */