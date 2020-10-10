package com.sun.tools.xjc.reader.xmlschema.cs;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.JClassFactory;
import com.sun.tools.xjc.reader.xmlschema.NameGenerator;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class ModelGroupBindingClassBinder
  extends AbstractBinderImpl
{
  private final ClassBinder base;
  private final Set topLevelChoices = new HashSet();
  
  ModelGroupBindingClassBinder(ClassSelector classSelector, ClassBinder base)
  {
    super(classSelector);
    this.base = base;
  }
  
  public Object modelGroup(XSModelGroup mgroup)
  {
    ClassItem ci = (ClassItem)this.base.modelGroup(mgroup);
    if ((mgroup.getCompositor() == XSModelGroup.CHOICE) && (!this.topLevelChoices.contains(mgroup)))
    {
      if ((ci == null) && (!this.builder.getGlobalBinding().isChoiceContentPropertyModelGroupBinding())) {
        try
        {
          JDefinedClass clazz = this.owner.getClassFactory().create(NameGenerator.getName(this.owner.builder, mgroup), mgroup.getLocator());
          
          ci = wrapByClassItem(mgroup, clazz);
        }
        catch (ParseException e)
        {
          this.builder.errorReceiver.error(mgroup.getLocator(), Messages.format("DefaultParticleBinder.UnableToGenerateNameFromModelGroup"));
          
          ci = null;
        }
      }
      if (ci != null) {
        ci.hasGetContentMethod = true;
      }
    }
    return ci;
  }
  
  public Object complexType(XSComplexType type)
  {
    ClassItem ci = (ClassItem)this.base.complexType(type);
    if (ci == null) {
      return null;
    }
    if (needsToHaveChoiceContentProperty(type))
    {
      this.topLevelChoices.add(type.getContentType().asParticle().getTerm());
      ci.hasGetContentMethod = true;
    }
    return ci;
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    ClassItem ci = (ClassItem)this.base.modelGroupDecl(decl);
    if (ci != null) {
      return ci;
    }
    JPackage pkg = this.owner.getPackage(decl.getTargetNamespace());
    
    JDefinedClass clazz = this.owner.codeModelClassFactory.createInterface(pkg, deriveName(decl), decl.getLocator());
    
    ci = wrapByClassItem(decl, clazz);
    if (needsToHaveChoiceContentProperty(decl)) {
      ci.hasGetContentMethod = true;
    }
    return ci;
  }
  
  private boolean needsToHaveChoiceContentProperty(XSComplexType type)
  {
    if (type.iterateDeclaredAttributeUses().hasNext()) {
      return false;
    }
    XSParticle p = type.getContentType().asParticle();
    if (p == null) {
      return false;
    }
    if (p.getMaxOccurs() != 1) {
      return false;
    }
    XSModelGroup mg = p.getTerm().asModelGroup();
    if (mg == null) {
      return false;
    }
    if (this.builder.getGlobalBinding().isChoiceContentPropertyModelGroupBinding()) {
      return false;
    }
    return mg.getCompositor() == XSModelGroup.CHOICE;
  }
  
  private boolean needsToHaveChoiceContentProperty(XSModelGroupDecl decl)
  {
    return decl.getModelGroup().getCompositor() == XSModelGroup.CHOICE;
  }
  
  public Object annotation(XSAnnotation ann)
  {
    return this.base.annotation(ann);
  }
  
  public Object attGroupDecl(XSAttGroupDecl decl)
  {
    return this.base.attGroupDecl(decl);
  }
  
  public Object attributeDecl(XSAttributeDecl decl)
  {
    return this.base.attributeDecl(decl);
  }
  
  public Object attributeUse(XSAttributeUse use)
  {
    return this.base.attributeUse(use);
  }
  
  public Object facet(XSFacet facet)
  {
    return this.base.facet(facet);
  }
  
  public Object notation(XSNotation notation)
  {
    return this.base.notation(notation);
  }
  
  public Object schema(XSSchema schema)
  {
    return this.base.schema(schema);
  }
  
  public Object empty(XSContentType empty)
  {
    return this.base.empty(empty);
  }
  
  public Object particle(XSParticle particle)
  {
    return this.base.particle(particle);
  }
  
  public Object simpleType(XSSimpleType simpleType)
  {
    return this.base.simpleType(simpleType);
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    return this.base.elementDecl(decl);
  }
  
  public Object wildcard(XSWildcard wc)
  {
    return this.base.wildcard(wc);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\cs\ModelGroupBindingClassBinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */