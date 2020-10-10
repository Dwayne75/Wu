package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.visitor.XSContentTypeFunction;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

public abstract class SimpleTypeImpl
  extends DeclarationImpl
  implements XSSimpleType, ContentTypeImpl, Ref.SimpleType
{
  private Ref.SimpleType baseType;
  
  SimpleTypeImpl(SchemaImpl _parent, AnnotationImpl _annon, Locator _loc, String _name, boolean _anonymous, Ref.SimpleType _baseType)
  {
    super(_parent, _annon, _loc, _parent.getTargetNamespace(), _name, _anonymous);
    
    this.baseType = _baseType;
  }
  
  public XSType[] listSubstitutables()
  {
    return Util.listSubstitutables(this);
  }
  
  public void redefine(SimpleTypeImpl st)
  {
    this.baseType = st;
  }
  
  public XSType getBaseType()
  {
    return this.baseType.getSimpleType();
  }
  
  public XSSimpleType getSimpleBaseType()
  {
    return this.baseType.getSimpleType();
  }
  
  public final int getDerivationMethod()
  {
    return 2;
  }
  
  public final XSSimpleType asSimpleType()
  {
    return this;
  }
  
  public final XSComplexType asComplexType()
  {
    return null;
  }
  
  public final boolean isSimpleType()
  {
    return true;
  }
  
  public final boolean isComplexType()
  {
    return false;
  }
  
  public final XSParticle asParticle()
  {
    return null;
  }
  
  public final XSContentType asEmpty()
  {
    return null;
  }
  
  public boolean isRestriction()
  {
    return false;
  }
  
  public boolean isList()
  {
    return false;
  }
  
  public boolean isUnion()
  {
    return false;
  }
  
  public XSRestrictionSimpleType asRestriction()
  {
    return null;
  }
  
  public XSListSimpleType asList()
  {
    return null;
  }
  
  public XSUnionSimpleType asUnion()
  {
    return null;
  }
  
  public final void visit(XSVisitor visitor)
  {
    visit((XSSimpleTypeVisitor)visitor);
  }
  
  public final void visit(XSContentTypeVisitor visitor)
  {
    visit((XSSimpleTypeVisitor)visitor);
  }
  
  public final Object apply(XSFunction function)
  {
    return function.simpleType(this);
  }
  
  public final Object apply(XSContentTypeFunction function)
  {
    return function.simpleType(this);
  }
  
  public XSType getType()
  {
    return this;
  }
  
  public XSContentType getContentType()
  {
    return this;
  }
  
  public XSSimpleType getSimpleType()
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\SimpleTypeImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */