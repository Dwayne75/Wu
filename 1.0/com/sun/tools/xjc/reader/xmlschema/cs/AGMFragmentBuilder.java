package com.sun.tools.xjc.reader.xmlschema.cs;

import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ext.WildcardItem;
import com.sun.tools.xjc.reader.xmlschema.AbstractXSFunctionImpl;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.DatatypeBuilder;
import com.sun.tools.xjc.reader.xmlschema.SimpleTypeBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import java.util.Iterator;
import java.util.Set;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

final class AGMFragmentBuilder
  extends AbstractXSFunctionImpl
{
  private final BGMBuilder builder;
  private final ExpressionPool pool;
  private XSComponent root;
  private ClassItem superClass;
  
  AGMFragmentBuilder(BGMBuilder builder)
  {
    this.builder = builder;
    this.pool = builder.pool;
  }
  
  public Expression build(XSComponent sc, ClassItem owner)
  {
    this.superClass = findSuperClass(owner);
    this.root = sc;
    return (Expression)sc.apply(this);
  }
  
  public Object attGroupDecl(XSAttGroupDecl decl)
  {
    return attributeContainer(decl);
  }
  
  private Expression attributeContainer(XSAttContainer cont)
  {
    Expression exp = Expression.epsilon;
    for (Iterator itr = cont.iterateAttributeUses(); itr.hasNext();) {
      exp = this.pool.createSequence(exp, recurse((XSAttributeUse)itr.next()));
    }
    return exp;
  }
  
  public Object attributeDecl(XSAttributeDecl decl)
  {
    return attribute(decl, decl.getFixedValue(), decl.getContext());
  }
  
  private Expression attribute(XSAttributeDecl decl, String fixedValue, ValidationContext context)
  {
    SimpleNameClass name = new SimpleNameClass(decl.getTargetNamespace(), decl.getName());
    Datatype dt = this.builder.simpleTypeBuilder.datatypeBuilder.build(decl.getType());
    if (fixedValue != null)
    {
      Object value = dt.createValue(fixedValue, context);
      return this.pool.createAttribute(name, this.pool.createValue(dt, null, value));
    }
    return this.pool.createAttribute(name, this.pool.createData(dt, null));
  }
  
  public Object attributeUse(XSAttributeUse use)
  {
    Expression e = attribute(use.getDecl(), use.getFixedValue(), use.getContext());
    if (use.isRequired()) {
      return e;
    }
    return this.pool.createOptional(e);
  }
  
  public Object complexType(XSComplexType type)
  {
    XSContentType content = type.getContentType();
    Expression body = recurse(content);
    if (type.isMixed()) {
      body = this.pool.createMixed(body);
    }
    body = this.pool.createSequence(body, attributeContainer(type));
    return body;
  }
  
  public Object empty(XSContentType empty)
  {
    return Expression.epsilon;
  }
  
  public Object particle(XSParticle particle)
  {
    XSTerm t = particle.getTerm();
    Expression exp;
    Expression exp;
    if (this.builder.particlesWithGlobalElementSkip.contains(particle))
    {
      XSElementDecl e = t.asElementDecl();
      Expression exp;
      if (e.isAbstract())
      {
        exp = Expression.nullSet;
      }
      else
      {
        ElementPattern ep = _elementDecl(e);
        Expression exp;
        if (e.getType().isComplexType()) {
          exp = this.pool.createChoice(this.builder.selector.bindToType(e), ep);
        } else {
          exp = ep;
        }
      }
    }
    else
    {
      exp = recurse(t);
    }
    if (t.isElementDecl()) {
      exp = this.pool.createChoice(exp, this.builder.getSubstitionGroupList(t.asElementDecl()));
    }
    return this.builder.processMinMax(exp, particle);
  }
  
  public Object simpleType(XSSimpleType simpleType)
  {
    return this.pool.createData(this.builder.simpleTypeBuilder.datatypeBuilder.build(simpleType));
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    if (decl.isAbstract()) {
      return Expression.nullSet;
    }
    return _elementDecl(decl);
  }
  
  private ElementPattern _elementDecl(XSElementDecl decl)
  {
    Expression body = recurse(decl.getType(), this.root == decl);
    if (((decl.getType() instanceof XSComplexType)) && (this.builder.getGlobalBinding().isTypeSubstitutionSupportEnabled()))
    {
      if (decl.getType().asComplexType().isAbstract()) {
        body = Expression.nullSet;
      }
      body = this.pool.createChoice(body, this.builder.getTypeSubstitutionList((XSComplexType)decl.getType(), true));
    }
    else
    {
      body = this.pool.createSequence(body, this.builder.createXsiTypeExp(decl));
    }
    if (decl.isNillable()) {
      body = this.pool.createChoice(this.pool.createAttribute(new SimpleNameClass("http://www.w3.org/2001/XMLSchema-instance", "nil"), this.pool.createValue(BooleanType.theInstance, Boolean.TRUE)), body);
    }
    return new ElementPattern(new SimpleNameClass(decl.getTargetNamespace(), decl.getName()), body);
  }
  
  public Object modelGroup(XSModelGroup group)
  {
    XSModelGroup.Compositor comp = group.getCompositor();
    Expression exp;
    Expression exp;
    if (comp == XSModelGroup.CHOICE) {
      exp = Expression.nullSet;
    } else {
      exp = Expression.epsilon;
    }
    for (int i = 0; i < group.getSize(); i++)
    {
      Expression item = recurse(group.getChild(i));
      if (comp == XSModelGroup.CHOICE) {
        exp = this.pool.createChoice(exp, item);
      }
      if (comp == XSModelGroup.SEQUENCE) {
        exp = this.pool.createSequence(exp, item);
      }
      if (comp == XSModelGroup.ALL) {
        exp = this.pool.createInterleave(exp, item);
      }
    }
    return exp;
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    return recurse(decl.getModelGroup());
  }
  
  public Object wildcard(XSWildcard wc)
  {
    return new WildcardItem(this.builder.grammar.codeModel, wc);
  }
  
  private Expression recurse(XSComponent sc)
  {
    return recurse(sc, true);
  }
  
  private Expression recurse(XSComponent sc, boolean superClassCheck)
  {
    Expression e = this.builder.selector.bindToType(sc);
    if (e != null)
    {
      if ((this.superClass == e) && (superClassCheck)) {
        return this.superClass.agm;
      }
      return e;
    }
    return (Expression)sc.apply(this);
  }
  
  private ClassItem findSuperClass(ClassItem parent)
  {
    if (parent == null) {
      return null;
    }
    ClassItem[] result = new ClassItem[1];
    
    parent.exp.visit(new AGMFragmentBuilder.1(this, result));
    
    return result[0];
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\cs\AGMFragmentBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */