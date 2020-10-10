package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JPackage;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CClassRef;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXSubstitutable;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode;
import com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeFieldBuilder;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

final class DefaultClassBinder
  implements ClassBinder
{
  private final SimpleTypeBuilder stb = (SimpleTypeBuilder)Ring.get(SimpleTypeBuilder.class);
  private final Model model = (Model)Ring.get(Model.class);
  protected final BGMBuilder builder = (BGMBuilder)Ring.get(BGMBuilder.class);
  protected final ClassSelector selector = (ClassSelector)Ring.get(ClassSelector.class);
  
  public CElement attGroupDecl(XSAttGroupDecl decl)
  {
    return allow(decl, decl.getName());
  }
  
  public CElement attributeDecl(XSAttributeDecl decl)
  {
    return allow(decl, decl.getName());
  }
  
  public CElement modelGroup(XSModelGroup mgroup)
  {
    return never();
  }
  
  public CElement modelGroupDecl(XSModelGroupDecl decl)
  {
    return never();
  }
  
  public CElement complexType(XSComplexType type)
  {
    CElement ci = allow(type, type.getName());
    if (ci != null) {
      return ci;
    }
    BindInfo bi = this.builder.getBindInfo(type);
    if (type.isGlobal())
    {
      QName tagName = null;
      String className = deriveName(type);
      Locator loc = type.getLocator();
      if (getGlobalBinding().isSimpleMode())
      {
        XSElementDecl referer = getSoleElementReferer(type);
        if ((referer != null) && (isCollapsable(referer)))
        {
          tagName = BGMBuilder.getName(referer);
          className = deriveName(referer);
          loc = referer.getLocator();
        }
      }
      JPackage pkg = this.selector.getPackage(type.getTargetNamespace());
      
      return new CClassInfo(this.model, pkg, className, loc, getTypeName(type), tagName, type, bi.toCustomizationList());
    }
    XSElementDecl element = type.getScope();
    if ((element.isGlobal()) && (isCollapsable(element)))
    {
      if (this.builder.getBindInfo(element).get(BIClass.class) != null) {
        return null;
      }
      return new CClassInfo(this.model, this.selector.getClassScope(), deriveName(element), element.getLocator(), null, BGMBuilder.getName(element), element, bi.toCustomizationList());
    }
    CElement parentType = this.selector.isBound(element, type);
    String className;
    String className;
    CClassInfoParent scope;
    if ((parentType != null) && ((parentType instanceof CElementInfo)) && (((CElementInfo)parentType).hasClass()))
    {
      CClassInfoParent scope = (CElementInfo)parentType;
      className = "Type";
    }
    else
    {
      className = this.builder.getNameConverter().toClassName(element.getName());
      
      BISchemaBinding sb = (BISchemaBinding)this.builder.getBindInfo(type.getOwnerSchema()).get(BISchemaBinding.class);
      if (sb != null) {
        className = sb.mangleAnonymousTypeClassName(className);
      }
      scope = this.selector.getClassScope();
    }
    return new CClassInfo(this.model, scope, className, type.getLocator(), null, null, type, bi.toCustomizationList());
  }
  
  private QName getTypeName(XSComplexType type)
  {
    if (type.getRedefinedBy() != null) {
      return null;
    }
    return BGMBuilder.getName(type);
  }
  
  private boolean isCollapsable(XSElementDecl decl)
  {
    XSType type = decl.getType();
    if (!type.isComplexType()) {
      return false;
    }
    if ((decl.getSubstitutables().size() > 1) || (decl.getSubstAffiliation() != null)) {
      return false;
    }
    if (decl.isNillable()) {
      return false;
    }
    BIXSubstitutable bixSubstitutable = (BIXSubstitutable)this.builder.getBindInfo(decl).get(BIXSubstitutable.class);
    if (bixSubstitutable != null)
    {
      bixSubstitutable.markAsAcknowledged();
      return false;
    }
    if ((getGlobalBinding().isSimpleMode()) && (decl.isGlobal()))
    {
      XSElementDecl referer = getSoleElementReferer(decl.getType());
      if (referer != null)
      {
        assert (referer == decl);
        return true;
      }
    }
    if ((!type.isLocal()) || (!type.isComplexType())) {
      return false;
    }
    return true;
  }
  
  @Nullable
  private XSElementDecl getSoleElementReferer(@NotNull XSType t)
  {
    Set<XSComponent> referer = this.builder.getReferer(t);
    
    XSElementDecl sole = null;
    for (XSComponent r : referer) {
      if ((r instanceof XSElementDecl))
      {
        XSElementDecl x = (XSElementDecl)r;
        if (x.isGlobal()) {
          if (sole == null) {
            sole = x;
          } else {
            return null;
          }
        }
      }
      else
      {
        return null;
      }
    }
    return sole;
  }
  
  public CElement elementDecl(XSElementDecl decl)
  {
    CElement r = allow(decl, decl.getName());
    if (r == null)
    {
      QName tagName = BGMBuilder.getName(decl);
      CCustomizations custs = this.builder.getBindInfo(decl).toCustomizationList();
      if (decl.isGlobal())
      {
        if (isCollapsable(decl)) {
          return this.selector.bindToType(decl.getType().asComplexType(), decl, true);
        }
        String className = null;
        if (getGlobalBinding().isGenerateElementClass()) {
          className = deriveName(decl);
        }
        CElementInfo cei = new CElementInfo(this.model, tagName, this.selector.getClassScope(), className, custs, decl.getLocator());
        
        this.selector.boundElements.put(decl, cei);
        
        this.stb.refererStack.push(decl);
        cei.initContentType(this.selector.bindToType(decl.getType(), decl), decl, decl.getDefaultValue());
        this.stb.refererStack.pop();
        r = cei;
      }
    }
    XSElementDecl top = decl.getSubstAffiliation();
    if (top != null)
    {
      CElement topci = this.selector.bindToType(top, decl);
      if (((r instanceof CClassInfo)) && ((topci instanceof CClassInfo))) {
        ((CClassInfo)r).setBaseClass((CClassInfo)topci);
      }
      if (((r instanceof CElementInfo)) && ((topci instanceof CElementInfo))) {
        ((CElementInfo)r).setSubstitutionHead((CElementInfo)topci);
      }
    }
    return r;
  }
  
  public CClassInfo empty(XSContentType ct)
  {
    return null;
  }
  
  public CClassInfo identityConstraint(XSIdentityConstraint xsIdentityConstraint)
  {
    return never();
  }
  
  public CClassInfo xpath(XSXPath xsxPath)
  {
    return never();
  }
  
  public CClassInfo attributeUse(XSAttributeUse use)
  {
    return never();
  }
  
  public CElement simpleType(XSSimpleType type)
  {
    CElement c = allow(type, type.getName());
    if (c != null) {
      return c;
    }
    if ((getGlobalBinding().isSimpleTypeSubstitution()) && (type.isGlobal())) {
      return new CClassInfo(this.model, this.selector.getClassScope(), deriveName(type), type.getLocator(), BGMBuilder.getName(type), null, type, null);
    }
    return never();
  }
  
  public CClassInfo particle(XSParticle particle)
  {
    return never();
  }
  
  public CClassInfo wildcard(XSWildcard wc)
  {
    return never();
  }
  
  public CClassInfo annotation(XSAnnotation annon)
  {
    if (!$assertionsDisabled) {
      throw new AssertionError();
    }
    return null;
  }
  
  public CClassInfo notation(XSNotation not)
  {
    if (!$assertionsDisabled) {
      throw new AssertionError();
    }
    return null;
  }
  
  public CClassInfo facet(XSFacet decl)
  {
    if (!$assertionsDisabled) {
      throw new AssertionError();
    }
    return null;
  }
  
  public CClassInfo schema(XSSchema schema)
  {
    if (!$assertionsDisabled) {
      throw new AssertionError();
    }
    return null;
  }
  
  private CClassInfo never()
  {
    return null;
  }
  
  private CElement allow(XSComponent component, String defaultBaseName)
  {
    BindInfo bindInfo = this.builder.getBindInfo(component);
    BIClass decl = (BIClass)bindInfo.get(BIClass.class);
    if (decl == null) {
      return null;
    }
    decl.markAsAcknowledged();
    
    String ref = decl.getExistingClassRef();
    if (ref != null) {
      if (!JJavaName.isFullyQualifiedClassName(ref))
      {
        ((ErrorReceiver)Ring.get(ErrorReceiver.class)).error(decl.getLocation(), Messages.format("ClassSelector.IncorrectClassName", new Object[] { ref }));
      }
      else
      {
        if ((component instanceof XSComplexType)) {
          ((ComplexTypeFieldBuilder)Ring.get(ComplexTypeFieldBuilder.class)).recordBindingMode((XSComplexType)component, ComplexTypeBindingMode.NORMAL);
        }
        return new CClassRef(this.model, component, decl, bindInfo.toCustomizationList());
      }
    }
    String clsName = decl.getClassName();
    if (clsName == null)
    {
      if (defaultBaseName == null)
      {
        ((ErrorReceiver)Ring.get(ErrorReceiver.class)).error(decl.getLocation(), Messages.format("ClassSelector.ClassNameIsRequired", new Object[0]));
        
        defaultBaseName = "undefined" + component.hashCode();
      }
      clsName = this.builder.deriveName(defaultBaseName, component);
    }
    else if (!JJavaName.isJavaIdentifier(clsName))
    {
      ((ErrorReceiver)Ring.get(ErrorReceiver.class)).error(decl.getLocation(), Messages.format("ClassSelector.IncorrectClassName", new Object[] { clsName }));
      
      clsName = "Undefined" + component.hashCode();
    }
    QName typeName = null;
    QName elementName = null;
    if ((component instanceof XSType))
    {
      XSType t = (XSType)component;
      typeName = BGMBuilder.getName(t);
    }
    if ((component instanceof XSElementDecl))
    {
      XSElementDecl e = (XSElementDecl)component;
      elementName = BGMBuilder.getName(e);
    }
    if (((component instanceof XSElementDecl)) && (!isCollapsable((XSElementDecl)component)))
    {
      XSElementDecl e = (XSElementDecl)component;
      
      CElementInfo cei = new CElementInfo(this.model, elementName, this.selector.getClassScope(), clsName, bindInfo.toCustomizationList(), decl.getLocation());
      
      this.selector.boundElements.put(e, cei);
      
      this.stb.refererStack.push(component);
      cei.initContentType(this.selector.bindToType(e.getType(), e), e, e.getDefaultValue());
      
      this.stb.refererStack.pop();
      return cei;
    }
    CClassInfo bt = new CClassInfo(this.model, this.selector.getClassScope(), clsName, decl.getLocation(), typeName, elementName, component, bindInfo.toCustomizationList());
    if (decl.getJavadoc() != null) {
      bt.javadoc = (decl.getJavadoc() + "\n\n");
    }
    String implClass = decl.getUserSpecifiedImplClass();
    if (implClass != null) {
      bt.setUserSpecifiedImplClass(implClass);
    }
    return bt;
  }
  
  private BIGlobalBinding getGlobalBinding()
  {
    return this.builder.getGlobalBinding();
  }
  
  private String deriveName(XSDeclaration comp)
  {
    return this.builder.deriveName(comp.getName(), comp);
  }
  
  private String deriveName(XSComplexType comp)
  {
    String seed = this.builder.deriveName(comp.getName(), comp);
    for (int cnt = comp.getRedefinedCount(); cnt > 0; cnt--) {
      seed = "Original" + seed;
    }
    return seed;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\DefaultClassBinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */