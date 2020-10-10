package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JDefinedClass;
import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.ext.WildcardItem;
import com.sun.tools.xjc.grammar.xducer.NilTransducer;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.cs.ClassSelector;
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
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import javax.xml.bind.Element;

public class TypeBuilder
  extends AbstractXSFunctionImpl
  implements BGMBuilder.ParticleHandler
{
  private final BGMBuilder builder;
  private final ExpressionPool pool;
  
  TypeBuilder(BGMBuilder _builder)
  {
    this.builder = _builder;
    this.pool = this.builder.pool;
  }
  
  public final Expression build(XSComponent sc)
  {
    return (Expression)sc.apply(this);
  }
  
  public Object attGroupDecl(XSAttGroupDecl agd)
  {
    Expression exp = this.builder.selector.bindToType(agd);
    if (exp != null) {
      return exp;
    }
    _assert(false);
    return null;
  }
  
  public Object attributeDecl(XSAttributeDecl decl)
  {
    return _attributeDecl(decl);
  }
  
  public AttributeExp _attributeDecl(XSAttributeDecl decl)
  {
    this.builder.simpleTypeBuilder.refererStack.push(decl);
    
    AttributeExp exp = (AttributeExp)this.pool.createAttribute(new SimpleNameClass(decl.getTargetNamespace(), decl.getName()), this.builder.simpleTypeBuilder.build(decl.getType()));
    
    this.builder.simpleTypeBuilder.refererStack.pop();
    
    return exp;
  }
  
  public Object attributeUse(XSAttributeUse use)
  {
    Expression exp = this.builder.selector.bindToType(use);
    if (exp != null) {
      return exp;
    }
    _assert(false);
    return null;
  }
  
  public Object complexType(XSComplexType type)
  {
    return this.builder.selector.bindToType(type);
  }
  
  public Object simpleType(XSSimpleType type)
  {
    Expression exp = this.builder.selector.bindToType(type);
    if (exp != null) {
      return exp;
    }
    return this.builder.simpleTypeBuilder.build(type);
  }
  
  public Object particle(XSParticle p)
  {
    Expression exp = this.builder.selector.bindToType(p);
    if (exp != null) {
      return exp;
    }
    return this.builder.processMinMax(build(p.getTerm()), p);
  }
  
  public Object empty(XSContentType empty)
  {
    Expression exp = this.builder.selector.bindToType(empty);
    if (exp != null) {
      return exp;
    }
    return Expression.epsilon;
  }
  
  public Object wildcard(XSWildcard wc)
  {
    Expression exp = this.builder.selector.bindToType(wc);
    if (exp != null) {
      return exp;
    }
    return new WildcardItem(this.builder.grammar.codeModel, wc);
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    Expression exp = this.builder.selector.bindToType(decl);
    if (exp != null) {
      return exp;
    }
    this.builder.selector.pushClassFactory(new PrefixedJClassFactoryImpl(this.builder, decl));
    
    exp = build(decl.getModelGroup());
    
    this.builder.selector.popClassFactory();
    
    return exp;
  }
  
  public Object modelGroup(XSModelGroup mg)
  {
    Expression exp = this.builder.selector.bindToType(mg);
    if (exp != null) {
      return exp;
    }
    return this.builder.applyRecursively(mg, this);
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    Expression exp = Expression.nullSet;
    for (Iterator itr = decl.getSubstitutables().iterator(); itr.hasNext();)
    {
      XSElementDecl e = (XSElementDecl)itr.next();
      if (!e.isAbstract()) {
        exp = this.pool.createChoice(exp, elementDeclWithoutSubstGroup(e));
      }
    }
    return exp;
  }
  
  private TypeItem elementDeclWithoutSubstGroup(XSElementDecl decl)
  {
    TypeItem ti = this.builder.selector.bindToType(decl);
    if (ti != null) {
      return ti;
    }
    JDefinedClass cls = this.builder.selector.getClassFactory().create(this.builder.getNameConverter().toClassName(decl.getName()), decl.getLocator());
    
    cls._implements(Element.class);
    
    ClassItem ci = this.builder.grammar.createClassItem(cls, Expression.epsilon, decl.getLocator());
    
    this.builder.selector.queueBuild(decl, ci);
    return ci;
  }
  
  protected ElementPattern elementDeclFlat(XSElementDecl decl)
  {
    this.builder.selector.bindToType(decl);
    
    Expression type = this.builder.selector.bindToType(decl.getType());
    Expression body;
    Expression body;
    if (type != null)
    {
      body = type;
    }
    else
    {
      this.builder.simpleTypeBuilder.refererStack.push(decl);
      
      body = this.builder.typeBuilder.build(decl.getType());
      
      this.builder.simpleTypeBuilder.refererStack.pop();
    }
    if (decl.isNillable()) {
      body = this.pool.createChoice(buildXsiNilExpForProperty(), body);
    }
    if ((decl.getType().isComplexType()) && (this.builder.getGlobalBinding().isTypeSubstitutionSupportEnabled())) {
      body = this.pool.createChoice(body, this.builder.getTypeSubstitutionList(decl.getType().asComplexType(), false));
    } else {
      body = this.pool.createSequence(body, this.builder.createXsiTypeExp(decl));
    }
    SimpleNameClass name = new SimpleNameClass(decl.getTargetNamespace(), decl.getName());
    
    return new ElementPattern(name, body);
  }
  
  private Expression buildXsiNilExpForProperty()
  {
    return new AttributeExp(new SimpleNameClass("http://www.w3.org/2001/XMLSchema-instance", "nil"), this.builder.grammar.createPrimitiveItem(new NilTransducer(this.builder.grammar.codeModel), StringType.theInstance, this.pool.createValue(BooleanType.theInstance, Boolean.TRUE), null));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\TypeBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */