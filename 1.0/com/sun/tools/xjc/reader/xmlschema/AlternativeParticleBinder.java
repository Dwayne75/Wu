package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JDefinedClass;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.cs.ClassSelector;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;
import java.text.ParseException;
import java.util.Set;

public class AlternativeParticleBinder
  extends ParticleBinder
  implements XSTermFunction, BGMBuilder.ParticleHandler
{
  private XSParticle parent;
  
  AlternativeParticleBinder(BGMBuilder builder)
  {
    super(builder);
  }
  
  public Expression build(XSParticle p, ClassItem superClass)
  {
    return (Expression)particle(p);
  }
  
  public boolean checkFallback(XSParticle p, ClassItem superClass)
  {
    return false;
  }
  
  public Object particle(XSParticle p)
  {
    this.builder.selector.bindToType(p);
    
    XSParticle oldParent = this.parent;
    
    this.parent = p;
    
    XSTerm t = p.getTerm();
    Expression exp;
    if (needSkip(t))
    {
      XSElementDecl e = t.asElementDecl();
      
      this.builder.particlesWithGlobalElementSkip.add(p);
      
      ElementPattern eexp = this.builder.typeBuilder.elementDeclFlat(e);
      Expression exp;
      if (needSkippableElement(e)) {
        exp = this.pool.createChoice(eexp, this.builder.selector.bindToType(e));
      } else {
        exp = eexp;
      }
      Expression exp = this.pool.createChoice(this.builder.getSubstitionGroupList(e), exp);
      
      exp = this.builder.fieldBuilder.createFieldItem(computeLabel(p), false, exp, p);
    }
    else
    {
      exp = (Expression)t.apply(this);
    }
    this.parent = oldParent;
    return this.builder.processMinMax(exp, p);
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    Expression typeExp = this.builder.selector.bindToType(decl);
    if (typeExp != null) {
      return this.builder.fieldBuilder.createFieldItem(typeExp, decl, false);
    }
    return this.builder.fieldBuilder.elementDecl(decl);
  }
  
  public Object modelGroup(XSModelGroup group)
  {
    Expression typeExp = this.builder.selector.bindToType(group);
    if (typeExp == null)
    {
      if ((group.getCompositor() == XSModelGroup.CHOICE) || (getLocalPropCustomization(this.parent) != null)) {
        return this.builder.fieldBuilder.createFieldItem(this.builder.typeBuilder.build(group), group);
      }
      if (this.parent.getMaxOccurs() != 1) {
        try
        {
          JDefinedClass cls = this.builder.selector.getClassFactory().create(this.builder.getNameConverter().toClassName(NameGenerator.getName(this.builder, group)), group.getLocator());
          
          ClassItem ci = this.builder.grammar.createClassItem(cls, Expression.epsilon, group.getLocator());
          
          this.builder.selector.queueBuild(group, ci);
          typeExp = ci;
        }
        catch (ParseException e)
        {
          this.builder.errorReporter.error(group.getLocator(), "DefaultParticleBinder.UnableToGenerateNameFromModelGroup");
          
          typeExp = null;
        }
      }
    }
    if (typeExp != null) {
      return this.builder.fieldBuilder.createFieldItem(typeExp, group);
    }
    return this.builder.applyRecursively(group, this);
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    Expression typeExp = this.builder.selector.bindToType(decl);
    if (typeExp != null) {
      return this.builder.fieldBuilder.createFieldItem(typeExp, decl, false);
    }
    this.builder.selector.pushClassFactory(new PrefixedJClassFactoryImpl(this.builder, decl));
    
    Object r = modelGroup(decl.getModelGroup());
    
    this.builder.selector.popClassFactory();
    
    return r;
  }
  
  public Object wildcard(XSWildcard wc)
  {
    Expression typeExp = this.builder.selector.bindToType(wc);
    if (typeExp == null) {
      typeExp = this.builder.typeBuilder.build(wc);
    }
    return this.builder.fieldBuilder.createFieldItem("any", false, typeExp, wc);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\AlternativeParticleBinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */