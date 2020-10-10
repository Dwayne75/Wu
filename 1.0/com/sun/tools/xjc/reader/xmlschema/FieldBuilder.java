package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.generator.field.ConstFieldRenderer;
import com.sun.tools.xjc.generator.field.XsiNilFieldRenderer;
import com.sun.tools.xjc.generator.field.XsiTypeFieldRenderer.Factory;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.DefaultValue;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.xducer.BuiltinDatatypeTransducerFactory;
import com.sun.tools.xjc.grammar.xducer.WhitespaceTransducer;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.reader.xmlschema.cs.ClassSelector;
import com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeFieldBuilder;
import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.impl.ModelGroupImpl;
import com.sun.xml.xsom.impl.ParticleImpl;
import com.sun.xml.xsom.impl.Ref.Term;
import com.sun.xml.xsom.impl.SchemaImpl;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import org.xml.sax.Locator;

public class FieldBuilder
  extends AbstractXSFunctionImpl
{
  private final BGMBuilder builder;
  private final ExpressionPool pool;
  
  FieldBuilder(BGMBuilder _builder)
  {
    this.builder = _builder;
    this.pool = this.builder.pool;
  }
  
  public final Expression build(XSComponent sc)
  {
    return (Expression)sc.apply(this);
  }
  
  public Object attGroupDecl(XSAttGroupDecl decl)
  {
    return attributeContainer(decl);
  }
  
  public Expression attributeContainer(XSAttContainer decl)
  {
    Expression exp = Expression.epsilon;
    
    Iterator itr = decl.iterateAttGroups();
    while (itr.hasNext()) {
      exp = this.pool.createSequence(exp, build((XSAttGroupDecl)itr.next()));
    }
    itr = decl.iterateDeclaredAttributeUses();
    while (itr.hasNext()) {
      exp = this.pool.createSequence(exp, build((XSAttributeUse)itr.next()));
    }
    return exp;
  }
  
  public Object attributeDecl(XSAttributeDecl arg0)
  {
    _assert(false);
    return null;
  }
  
  public Object attributeUse(XSAttributeUse use)
  {
    BIProperty cust = getPropCustomization(use);
    
    AttributeExp body = this.builder.typeBuilder._attributeDecl(use.getDecl());
    Expression originalBody = body.exp;
    
    boolean hasFixedValue = use.getFixedValue() != null;
    if (hasFixedValue)
    {
      String token = use.getFixedValue();
      
      Expression contents = FixedExpBuilder.build(body.exp, token, this.builder.grammar, use.getContext());
      if (contents == Expression.nullSet)
      {
        Locator loc;
        Locator loc;
        if (use.getDecl().getFixedValue() != null) {
          loc = use.getDecl().getLocator();
        } else {
          loc = use.getLocator();
        }
        this.builder.errorReporter.error(loc, "FieldBuilder.IncorrectFixedValue", token);
      }
      else
      {
        body = new AttributeExp(body.nameClass, contents);
      }
    }
    boolean toConstant = (BIProperty.getCustomization(this.builder, use).isConstantProperty()) && (use.getFixedValue() != null);
    
    String xmlName = use.getDecl().getName();
    String defaultName = toConstant ? makeJavaConstName(xmlName) : makeJavaName(xmlName);
    
    FieldItem exp = createFieldItem(defaultName, toConstant, body, use);
    if (use.getDefaultValue() != null)
    {
      String token = use.getDefaultValue();
      
      Expression contents = FixedExpBuilder.build(body.exp, token, this.builder.grammar, use.getContext());
      if (contents == Expression.nullSet)
      {
        Locator loc;
        Locator loc;
        if (use.getDecl().getDefaultValue() != null) {
          loc = use.getDecl().getLocator();
        } else {
          loc = use.getLocator();
        }
        this.builder.errorReporter.error(loc, "FieldBuilder.IncorrectDefaultValue", token);
      }
      ArrayList values = new ArrayList();
      contents.visit(new FieldBuilder.1(this, values));
      
      exp.defaultValues = ((DefaultValue[])values.toArray(new DefaultValue[values.size()]));
    }
    if (toConstant) {
      exp.realization = ConstFieldRenderer.theFactory;
    }
    if (hasFixedValue) {
      originalBody.visit(new FieldBuilder.2(this, exp, use, cust));
    }
    if (!use.isRequired()) {
      return this.pool.createOptional(exp);
    }
    return exp;
  }
  
  private BIProperty getPropCustomization(XSAttributeUse use)
  {
    BIProperty cust = (BIProperty)this.builder.getBindInfo(use).get(BIProperty.NAME);
    if (cust != null) {
      return cust;
    }
    return (BIProperty)this.builder.getBindInfo(use.getDecl()).get(BIProperty.NAME);
  }
  
  public Object complexType(XSComplexType type)
  {
    return this.builder.complexTypeBuilder.build(type);
  }
  
  public Object simpleType(XSSimpleType type)
  {
    return simpleType(type, type.getOwnerSchema());
  }
  
  public Expression simpleType(XSSimpleType type, XSComponent property)
  {
    BIProperty prop = BIProperty.getCustomization(this.builder, property);
    
    return prop.createFieldItem("Value", false, this.builder.simpleTypeBuilder.build(type), type);
  }
  
  public Object particle(XSParticle p)
  {
    _assert(false);
    return null;
  }
  
  private Expression particle(XSParticle p, ClassItem superClass)
  {
    return this.builder.particleBinder.build(p, superClass);
  }
  
  public Object empty(XSContentType ct)
  {
    return Expression.epsilon;
  }
  
  private XSParticle makeParticle(XSTerm t)
  {
    return new ParticleImpl(null, null, (Ref.Term)t, t.getLocator());
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    this.builder.selector.pushClassFactory(new PrefixedJClassFactoryImpl(this.builder, decl));
    
    Object r = build(decl.getModelGroup());
    
    this.builder.selector.popClassFactory();
    
    return r;
  }
  
  public Object wildcard(XSWildcard wc)
  {
    return particle(makeParticle(wc), null);
  }
  
  public Object modelGroup(XSModelGroup mg)
  {
    if (this.builder.getGlobalBinding().isModelGroupBinding()) {
      return this.builder.applyRecursively(mg, new FieldBuilder.3(this));
    }
    XSModelGroup mg2 = new ModelGroupImpl((SchemaImpl)mg.getOwnerSchema(), null, mg.getLocator(), mg.getCompositor(), mg.getChildren());
    
    return particle(makeParticle(mg2), null);
  }
  
  public FieldItem createFieldItem(Expression typeExp, XSDeclaration source, boolean forConstant)
  {
    String defaultName = this.builder.getNameConverter().toPropertyName(source.getName());
    
    return createFieldItem(defaultName, forConstant, typeExp, source);
  }
  
  public Expression createFieldItem(Expression typeExp, XSModelGroup modelGroup)
  {
    try
    {
      String defaultName = NameGenerator.getName(this.builder, modelGroup);
      return createFieldItem(defaultName, false, typeExp, modelGroup);
    }
    catch (ParseException e)
    {
      this.builder.errorReporter.error(modelGroup.getLocator(), "ClassSelector.ClassNameIsRequired");
    }
    return Expression.epsilon;
  }
  
  public FieldItem createFieldItem(String defaultName, boolean forConstant, Expression typeExp, XSComponent source)
  {
    BIProperty cust = BIProperty.getCustomization(this.builder, source);
    return cust.createFieldItem(defaultName, forConstant, typeExp, source);
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    boolean isMappedToType = this.builder.selector.bindToType(decl) != null;
    if (!isMappedToType) {
      return createFieldItem(this.builder.typeBuilder.elementDeclFlat(decl), decl, false);
    }
    Expression type = this.builder.selector.bindToType(decl.getType());
    Expression body;
    Expression body;
    if (type != null)
    {
      _assert(type instanceof ClassItem);
      ClassItem defaultType = (ClassItem)type;
      if (((decl.getType() instanceof XSComplexType)) && (this.builder.getGlobalBinding().isTypeSubstitutionSupportEnabled())) {
        type = this.pool.createChoice(type, this.builder.getTypeSubstitutionList((XSComplexType)decl.getType(), false));
      } else {
        type = this.pool.createSequence(type, this.builder.createXsiTypeExp(decl));
      }
      Expression body;
      if (this.builder.getGlobalBinding().isTypeSubstitutionSupportEnabled())
      {
        FieldItem fi = new FieldItem("ValueObject", type, decl.getLocator());
        fi.realization = new XsiTypeFieldRenderer.Factory(defaultType);
        fi.setDelegation(true);
        fi.javadoc = Messages.format("FieldBuilder.Javadoc.ValueObject", defaultType.getType().fullName(), fi.name);
        
        body = fi;
      }
      else
      {
        body = new SuperClassItem(type, decl.getLocator());
      }
    }
    else
    {
      this.builder.simpleTypeBuilder.refererStack.push(decl);
      
      body = build(decl.getType());
      
      this.builder.simpleTypeBuilder.refererStack.pop();
      
      body = this.pool.createSequence(body, this.builder.createXsiTypeExp(decl));
    }
    SimpleNameClass name = new SimpleNameClass(decl.getTargetNamespace(), decl.getName());
    if (decl.isNillable())
    {
      FieldItem fi = new FieldItem("Nil", buildXsiNilExpForClass(), decl.getLocator());
      
      fi.realization = XsiNilFieldRenderer.theFactory;
      fi.javadoc = Messages.format("FieldBuilder.Javadoc.NilProperty");
      
      body.visit(new FieldBuilder.4(this));
      
      body = this.pool.createChoice(fi, body);
    }
    return new ElementPattern(name, body);
  }
  
  private Expression buildXsiNilExpForClass()
  {
    return new AttributeExp(new SimpleNameClass("http://www.w3.org/2001/XMLSchema-instance", "nil"), this.builder.grammar.createPrimitiveItem(WhitespaceTransducer.create(BuiltinDatatypeTransducerFactory.get(this.builder.grammar, BooleanType.theInstance), this.builder.grammar.codeModel, WhitespaceNormalizer.COLLAPSE), BooleanType.theInstance, this.pool.createData(BooleanType.theInstance), null));
  }
  
  private String makeJavaName(String xmlName)
  {
    return this.builder.getNameConverter().toPropertyName(xmlName);
  }
  
  private String makeJavaConstName(String xmlName)
  {
    return this.builder.getNameConverter().toConstantName(xmlName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\FieldBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */