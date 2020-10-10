package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.model.CClass;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.BindGreen;
import com.sun.tools.xjc.reader.xmlschema.ClassSelector;
import com.sun.tools.xjc.reader.xmlschema.ParticleBinder;
import com.sun.tools.xjc.reader.xmlschema.WildcardNameClassBuilder;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.kohsuke.rngom.nc.ChoiceNameClass;
import org.kohsuke.rngom.nc.NameClass;
import org.kohsuke.rngom.nc.SimpleNameClass;

final class ExtendedComplexTypeBuilder
  extends CTBuilder
{
  private final Map<XSComplexType, NameClass[]> characteristicNameClasses = new HashMap();
  
  public boolean isApplicable(XSComplexType ct)
  {
    XSType baseType = ct.getBaseType();
    return (baseType != this.schemas.getAnyType()) && (baseType.isComplexType()) && (ct.getDerivationMethod() == 1);
  }
  
  public void build(XSComplexType ct)
  {
    XSComplexType baseType = ct.getBaseType().asComplexType();
    
    CClass baseClass = this.selector.bindToType(baseType, ct, true);
    assert (baseClass != null);
    
    this.selector.getCurrentBean().setBaseClass(baseClass);
    
    ComplexTypeBindingMode baseTypeFlag = this.builder.getBindingMode(baseType);
    
    XSContentType explicitContent = ct.getExplicitContent();
    if (!checkIfExtensionSafe(baseType, ct))
    {
      this.errorReceiver.error(ct.getLocator(), Messages.ERR_NO_FURTHER_EXTENSION.format(new Object[] { baseType.getName(), ct.getName() }));
      
      return;
    }
    if ((explicitContent != null) && (explicitContent.asParticle() != null))
    {
      if (baseTypeFlag == ComplexTypeBindingMode.NORMAL)
      {
        this.builder.recordBindingMode(ct, this.bgmBuilder.getParticleBinder().checkFallback(explicitContent.asParticle()) ? ComplexTypeBindingMode.FALLBACK_REST : ComplexTypeBindingMode.NORMAL);
        
        this.bgmBuilder.getParticleBinder().build(explicitContent.asParticle());
      }
      else
      {
        this.builder.recordBindingMode(ct, baseTypeFlag);
      }
    }
    else {
      this.builder.recordBindingMode(ct, baseTypeFlag);
    }
    this.green.attContainer(ct);
  }
  
  private boolean checkIfExtensionSafe(XSComplexType baseType, XSComplexType thisType)
  {
    XSComplexType lastType = getLastRestrictedType(baseType);
    if (lastType == null) {
      return true;
    }
    NameClass anc = NameClass.NULL;
    
    Iterator itr = thisType.iterateDeclaredAttributeUses();
    while (itr.hasNext()) {
      anc = new ChoiceNameClass(anc, getNameClass(((XSAttributeUse)itr.next()).getDecl()));
    }
    NameClass enc = getNameClass(thisType.getExplicitContent());
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
      
      NameClass nc = NameClass.NULL;
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
    return (chnc[0].hasOverlapWith(enc)) || (chnc[1].hasOverlapWith(anc));
  }
  
  private NameClass getNameClass(XSContentType t)
  {
    if (t == null) {
      return NameClass.NULL;
    }
    XSParticle p = t.asParticle();
    if (p == null) {
      return NameClass.NULL;
    }
    return (NameClass)p.getTerm().apply(this.contentModelNameClassBuilder);
  }
  
  private NameClass getNameClass(XSDeclaration decl)
  {
    return new SimpleNameClass(decl.getTargetNamespace(), decl.getName());
  }
  
  private final XSTermFunction<NameClass> contentModelNameClassBuilder = new XSTermFunction()
  {
    public NameClass wildcard(XSWildcard wc)
    {
      return WildcardNameClassBuilder.build(wc);
    }
    
    public NameClass modelGroupDecl(XSModelGroupDecl decl)
    {
      return modelGroup(decl.getModelGroup());
    }
    
    public NameClass modelGroup(XSModelGroup group)
    {
      NameClass nc = NameClass.NULL;
      for (int i = 0; i < group.getSize(); i++) {
        nc = new ChoiceNameClass(nc, (NameClass)group.getChild(i).getTerm().apply(this));
      }
      return nc;
    }
    
    public NameClass elementDecl(XSElementDecl decl)
    {
      return ExtendedComplexTypeBuilder.this.getNameClass(decl);
    }
  };
  
  private XSComplexType getLastRestrictedType(XSComplexType t)
  {
    if (t.getBaseType() == this.schemas.getAnyType()) {
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ct\ExtendedComplexTypeBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */