package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.util.NameClassCollisionChecker;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.FieldBuilder;
import com.sun.tools.xjc.reader.xmlschema.ParticleBinder;
import com.sun.tools.xjc.reader.xmlschema.TypeBuilder;
import com.sun.tools.xjc.reader.xmlschema.WildcardNameClassBuilder;
import com.sun.tools.xjc.reader.xmlschema.cs.ClassSelector;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExtendedComplexTypeBuilder
  extends AbstractCTBuilder
{
  private final Map characteristicNameClasses = new HashMap();
  
  public ExtendedComplexTypeBuilder(ComplexTypeFieldBuilder _builder)
  {
    super(_builder);
  }
  
  public boolean isApplicable(XSComplexType ct)
  {
    XSType baseType = ct.getBaseType();
    return (baseType != this.bgmBuilder.schemas.getAnyType()) && (baseType.isComplexType()) && (ct.getDerivationMethod() == 1);
  }
  
  public Expression build(XSComplexType ct)
  {
    XSComplexType baseType = ct.getBaseType().asComplexType();
    
    ClassItem baseClass = this.bgmBuilder.selector.bindToType(baseType);
    _assert(baseClass != null);
    
    Expression exp = new SuperClassItem(baseClass, ct.getLocator());
    
    ComplexTypeBindingMode baseTypeFlag = this.builder.getBindingMode(baseType);
    
    XSContentType explicitContent = ct.getExplicitContent();
    if (!checkIfExtensionSafe(baseType, ct))
    {
      this.bgmBuilder.errorReceiver.error(ct.getLocator(), Messages.format("FieldBuilder.NoFurtherExtension", baseType.getName(), ct.getName()));
      
      return Expression.epsilon;
    }
    if ((explicitContent != null) && (explicitContent.asParticle() != null))
    {
      if (baseTypeFlag == ComplexTypeBindingMode.NORMAL)
      {
        this.builder.recordBindingMode(ct, this.bgmBuilder.particleBinder.checkFallback(explicitContent.asParticle(), baseClass) ? ComplexTypeBindingMode.FALLBACK_REST : ComplexTypeBindingMode.NORMAL);
        
        exp = this.pool.createSequence(exp, this.bgmBuilder.particleBinder.build(explicitContent.asParticle(), baseClass));
      }
      else
      {
        Expression body = this.bgmBuilder.typeBuilder.build(explicitContent);
        if (ct.isMixed()) {
          body = this.pool.createInterleave(this.pool.createZeroOrMore(this.bgmBuilder.grammar.createPrimitiveItem(new IdentityTransducer(this.bgmBuilder.grammar.codeModel), StringType.theInstance, this.pool.createData(StringType.theInstance), ct.getLocator())), body);
        }
        FieldItem fi = new FieldItem(baseTypeFlag == ComplexTypeBindingMode.FALLBACK_CONTENT ? "Content" : "Rest", body, ct.getLocator());
        
        fi.multiplicity = Multiplicity.star;
        fi.collisionExpected = true;
        
        exp = this.pool.createSequence(exp, fi);
        this.builder.recordBindingMode(ct, baseTypeFlag);
      }
    }
    else {
      this.builder.recordBindingMode(ct, baseTypeFlag);
    }
    return this.pool.createSequence(this.bgmBuilder.fieldBuilder.attributeContainer(ct), exp);
  }
  
  private boolean checkIfExtensionSafe(XSComplexType baseType, XSComplexType thisType)
  {
    XSComplexType lastType = getLastRestrictedType(baseType);
    if (lastType == null) {
      return true;
    }
    NameClass anc = NameClass.NONE;
    
    Iterator itr = thisType.iterateDeclaredAttributeUses();
    while (itr.hasNext()) {
      anc = new ChoiceNameClass(anc, getNameClass(((XSAttributeUse)itr.next()).getDecl()));
    }
    anc = anc.simplify();
    
    NameClass enc = getNameClass(thisType.getExplicitContent()).simplify();
    while (lastType != lastType.getBaseType())
    {
      if (checkCollision(anc, enc, lastType)) {
        return false;
      }
      if (lastType.getBaseType().isSimpleType()) {
        return true;
      }
      lastType = lastType.getBaseType().asComplexType();
    }
    return true;
  }
  
  private boolean checkCollision(NameClass anc, NameClass enc, XSComplexType type)
  {
    NameClass[] chnc = (NameClass[])this.characteristicNameClasses.get(type);
    if (chnc == null)
    {
      chnc = new NameClass[2];
      chnc[0] = getNameClass(type.getContentType());
      
      NameClass nc = NameClass.NONE;
      Iterator itr = type.iterateAttributeUses();
      while (itr.hasNext()) {
        anc = new ChoiceNameClass(anc, getNameClass(((XSAttributeUse)itr.next()).getDecl()));
      }
      XSWildcard wc = type.getAttributeWildcard();
      if (wc != null) {
        nc = new ChoiceNameClass(nc, WildcardNameClassBuilder.build(wc));
      }
      chnc[1] = nc;
      
      this.characteristicNameClasses.put(type, chnc);
    }
    return (this.collisionChecker.check(chnc[0], enc)) || (this.collisionChecker.check(chnc[1], anc));
  }
  
  private NameClass getNameClass(XSContentType t)
  {
    if (t == null) {
      return NameClass.NONE;
    }
    XSParticle p = t.asParticle();
    if (p == null) {
      return NameClass.NONE;
    }
    return (NameClass)p.getTerm().apply(this.contentModelNameClassBuilder);
  }
  
  private NameClass getNameClass(XSDeclaration decl)
  {
    return new SimpleNameClass(decl.getTargetNamespace(), decl.getName());
  }
  
  private final NameClassCollisionChecker collisionChecker = new NameClassCollisionChecker();
  private final XSTermFunction contentModelNameClassBuilder = new ExtendedComplexTypeBuilder.1(this);
  
  private XSComplexType getLastRestrictedType(XSComplexType t)
  {
    if (t.getBaseType() == this.bgmBuilder.schemas.getAnyType()) {
      return null;
    }
    if (t.getDerivationMethod() == 2) {
      return t;
    }
    XSComplexType baseType = t.getBaseType().asComplexType();
    if (baseType != null) {
      return getLastRestrictedType(baseType);
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\ExtendedComplexTypeBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */