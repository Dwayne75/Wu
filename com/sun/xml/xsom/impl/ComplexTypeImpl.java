package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.impl.parser.DelayedRef;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.impl.scd.Iterators.Filter;
import com.sun.xml.xsom.impl.scd.Iterators.Union;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.xml.sax.Locator;

public class ComplexTypeImpl
  extends AttributesHolder
  implements XSComplexType, Ref.ComplexType
{
  private int derivationMethod;
  private Ref.Type baseType;
  
  public ComplexTypeImpl(SchemaDocumentImpl _parent, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, String _name, boolean _anonymous, boolean _abstract, int _derivationMethod, Ref.Type _base, int _final, int _block, boolean _mixed)
  {
    super(_parent, _annon, _loc, _fa, _name, _anonymous);
    if (_base == null) {
      throw new IllegalArgumentException();
    }
    this._abstract = _abstract;
    this.derivationMethod = _derivationMethod;
    this.baseType = _base;
    this.finalValue = _final;
    this.blockValue = _block;
    this.mixed = _mixed;
  }
  
  public XSComplexType asComplexType()
  {
    return this;
  }
  
  public boolean isDerivedFrom(XSType t)
  {
    XSType x = this;
    for (;;)
    {
      if (t == x) {
        return true;
      }
      XSType s = x.getBaseType();
      if (s == x) {
        return false;
      }
      x = s;
    }
  }
  
  public XSSimpleType asSimpleType()
  {
    return null;
  }
  
  public final boolean isSimpleType()
  {
    return false;
  }
  
  public final boolean isComplexType()
  {
    return true;
  }
  
  public int getDerivationMethod()
  {
    return this.derivationMethod;
  }
  
  public XSType getBaseType()
  {
    return this.baseType.getType();
  }
  
  public void redefine(ComplexTypeImpl ct)
  {
    if ((this.baseType instanceof DelayedRef)) {
      ((DelayedRef)this.baseType).redefine(ct);
    } else {
      this.baseType = ct;
    }
    ct.redefinedBy = this;
    this.redefiningCount = ((short)(ct.redefiningCount + 1));
  }
  
  private short redefiningCount = 0;
  private ComplexTypeImpl redefinedBy = null;
  private XSElementDecl scope;
  private final boolean _abstract;
  private WildcardImpl localAttWildcard;
  private final int finalValue;
  private final int blockValue;
  private Ref.ContentType contentType;
  private XSContentType explicitContent;
  private final boolean mixed;
  
  public XSComplexType getRedefinedBy()
  {
    return this.redefinedBy;
  }
  
  public int getRedefinedCount()
  {
    int i = 0;
    for (ComplexTypeImpl ct = this.redefinedBy; ct != null; ct = ct.redefinedBy) {
      i++;
    }
    return i;
  }
  
  public XSElementDecl getScope()
  {
    return this.scope;
  }
  
  public void setScope(XSElementDecl _scope)
  {
    this.scope = _scope;
  }
  
  public boolean isAbstract()
  {
    return this._abstract;
  }
  
  public void setWildcard(WildcardImpl wc)
  {
    this.localAttWildcard = wc;
  }
  
  public XSWildcard getAttributeWildcard()
  {
    WildcardImpl complete = this.localAttWildcard;
    
    Iterator itr = iterateAttGroups();
    while (itr.hasNext())
    {
      WildcardImpl w = (WildcardImpl)((XSAttGroupDecl)itr.next()).getAttributeWildcard();
      if (w != null) {
        if (complete == null) {
          complete = w;
        } else {
          complete = complete.union(this.ownerDocument, w);
        }
      }
    }
    if (getDerivationMethod() == 2) {
      return complete;
    }
    WildcardImpl base = null;
    XSType baseType = getBaseType();
    if (baseType.asComplexType() != null) {
      base = (WildcardImpl)baseType.asComplexType().getAttributeWildcard();
    }
    if (complete == null) {
      return base;
    }
    if (base == null) {
      return complete;
    }
    return complete.union(this.ownerDocument, base);
  }
  
  public boolean isFinal(int derivationMethod)
  {
    return (this.finalValue & derivationMethod) != 0;
  }
  
  public boolean isSubstitutionProhibited(int method)
  {
    return (this.blockValue & method) != 0;
  }
  
  public void setContentType(Ref.ContentType v)
  {
    this.contentType = v;
  }
  
  public XSContentType getContentType()
  {
    return this.contentType.getContentType();
  }
  
  public void setExplicitContent(XSContentType v)
  {
    this.explicitContent = v;
  }
  
  public XSContentType getExplicitContent()
  {
    return this.explicitContent;
  }
  
  public boolean isMixed()
  {
    return this.mixed;
  }
  
  public XSAttributeUse getAttributeUse(String nsURI, String localName)
  {
    UName name = new UName(nsURI, localName);
    if (this.prohibitedAtts.contains(name)) {
      return null;
    }
    XSAttributeUse o = (XSAttributeUse)this.attributes.get(name);
    if (o == null)
    {
      Iterator itr = iterateAttGroups();
      while ((itr.hasNext()) && (o == null)) {
        o = ((XSAttGroupDecl)itr.next()).getAttributeUse(nsURI, localName);
      }
    }
    if (o == null)
    {
      XSType base = getBaseType();
      if (base.asComplexType() != null) {
        o = base.asComplexType().getAttributeUse(nsURI, localName);
      }
    }
    return o;
  }
  
  public Iterator<XSAttributeUse> iterateAttributeUses()
  {
    XSComplexType baseType = getBaseType().asComplexType();
    if (baseType == null) {
      return super.iterateAttributeUses();
    }
    new Iterators.Union(new Iterators.Filter(baseType.iterateAttributeUses())
    {
      protected boolean matches(XSAttributeUse value)
      {
        XSAttributeDecl u = value.getDecl();
        UName n = new UName(u.getTargetNamespace(), u.getName());
        return !ComplexTypeImpl.this.prohibitedAtts.contains(n);
      }
    }, super.iterateAttributeUses());
  }
  
  public XSType[] listSubstitutables()
  {
    return Util.listSubstitutables(this);
  }
  
  public void visit(XSVisitor visitor)
  {
    visitor.complexType(this);
  }
  
  public <T> T apply(XSFunction<T> function)
  {
    return (T)function.complexType(this);
  }
  
  public XSComplexType getType()
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\ComplexTypeImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */