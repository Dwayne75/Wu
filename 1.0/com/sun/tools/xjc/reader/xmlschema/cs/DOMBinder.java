package com.sun.tools.xjc.reader.xmlschema.cs;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.ext.DOMItemFactory;
import com.sun.tools.xjc.grammar.ext.DOMItemFactory.UndefinedNameException;
import com.sun.tools.xjc.reader.xmlschema.AbstractXSFunctionImpl;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.WildcardNameClassBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXDom;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;

class DOMBinder
  extends AbstractXSFunctionImpl
{
  private final BGMBuilder builder;
  private final ClassSelector selector;
  private final ExpressionPool pool;
  
  DOMBinder(ClassSelector _selector)
  {
    this.selector = _selector;
    this.builder = this.selector.builder;
    this.pool = this.builder.pool;
  }
  
  public Expression bind(XSComponent sc)
  {
    return (Expression)sc.apply(this);
  }
  
  public TypeItem bind(XSElementDecl sc)
  {
    return (TypeItem)sc.apply(this);
  }
  
  public Object particle(XSParticle p)
  {
    BIXDom c = (BIXDom)this.builder.getBindInfo(p).get(BIXDom.NAME);
    if (c == null) {
      return null;
    }
    return new DOMBinder.Builder(this, c).particle(p);
  }
  
  private Expression bindTerm(XSTerm t)
  {
    BIXDom c = (BIXDom)this.builder.getBindInfo(t).get(BIXDom.NAME);
    if (c == null) {
      return null;
    }
    return (Expression)t.apply(new DOMBinder.Builder(this, c));
  }
  
  public Object wildcard(XSWildcard wc)
  {
    Expression exp = bindTerm(wc);
    if (exp != null) {
      return exp;
    }
    if (((wc.getMode() == 3) || (wc.getMode() == 1)) && (this.builder.getGlobalBinding().smartWildcardDefaultBinding)) {
      try
      {
        return DOMItemFactory.getInstance("W3C").create(WildcardNameClassBuilder.build(wc), this.builder.grammar, wc.getLocator());
      }
      catch (DOMItemFactory.UndefinedNameException e)
      {
        e.printStackTrace();
        throw new JAXBAssertionError();
      }
    }
    return null;
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    return bindTerm(decl);
  }
  
  public Object modelGroup(XSModelGroup group)
  {
    return bindTerm(group);
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    return bindTerm(decl);
  }
  
  public Object attGroupDecl(XSAttGroupDecl decl)
  {
    return null;
  }
  
  public Object attributeDecl(XSAttributeDecl decl)
  {
    return null;
  }
  
  public Object attributeUse(XSAttributeUse use)
  {
    return null;
  }
  
  public Object complexType(XSComplexType type)
  {
    return null;
  }
  
  public Object simpleType(XSSimpleType simpleType)
  {
    return null;
  }
  
  public Object empty(XSContentType empty)
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\cs\DOMBinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */