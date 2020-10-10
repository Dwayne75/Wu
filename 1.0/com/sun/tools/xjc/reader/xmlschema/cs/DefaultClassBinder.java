package com.sun.tools.xjc.reader.xmlschema.cs;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.JClassFactory;
import com.sun.tools.xjc.reader.xmlschema.NameGenerator;
import com.sun.tools.xjc.reader.xmlschema.SimpleTypeBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import java.text.ParseException;
import java.util.Stack;
import javax.xml.bind.Element;

class DefaultClassBinder
  extends AbstractBinderImpl
{
  DefaultClassBinder(ClassSelector classSelector)
  {
    super(classSelector);
  }
  
  public Object attGroupDecl(XSAttGroupDecl decl)
  {
    return allow(decl, decl.getName());
  }
  
  public Object attributeDecl(XSAttributeDecl decl)
  {
    return allow(decl, decl.getName());
  }
  
  public Object modelGroup(XSModelGroup mgroup)
  {
    String defaultName;
    try
    {
      defaultName = NameGenerator.getName(this.owner.builder, mgroup);
    }
    catch (ParseException e)
    {
      String defaultName;
      defaultName = null;
    }
    return allow(mgroup, defaultName);
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    return allow(decl, decl.getName());
  }
  
  public Object complexType(XSComplexType type)
  {
    ClassItem ci = allow(type, type.getName());
    if (ci != null) {
      return ci;
    }
    if (type.isGlobal())
    {
      JPackage pkg = this.owner.getPackage(type.getTargetNamespace());
      
      JDefinedClass clazz = this.owner.codeModelClassFactory.createInterface(pkg, deriveName(type), type.getLocator());
      
      return wrapByClassItem(type, clazz);
    }
    String className = this.builder.getNameConverter().toClassName(type.getScope().getName());
    
    BISchemaBinding sb = (BISchemaBinding)this.builder.getBindInfo(type.getOwnerSchema()).get(BISchemaBinding.NAME);
    if (sb != null) {
      className = sb.mangleAnonymousTypeClassName(className);
    } else {
      className = className + "Type";
    }
    return wrapByClassItem(type, getClassFactory(type).create(className, type.getLocator()));
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    ClassItem r = allow(decl, decl.getName());
    if ((r == null) && 
      (decl.isGlobal())) {
      r = wrapByClassItem(decl, this.owner.codeModelClassFactory.createInterface(this.owner.getPackage(decl.getTargetNamespace()), deriveName(decl), decl.getLocator()));
    }
    if (r != null) {
      r.getTypeAsDefined()._implements(Element.class);
    }
    return r;
  }
  
  public Object empty(XSContentType ct)
  {
    return null;
  }
  
  public Object attributeUse(XSAttributeUse use)
  {
    return never(use);
  }
  
  public Object simpleType(XSSimpleType type)
  {
    this.builder.simpleTypeBuilder.refererStack.push(type);
    
    this.builder.simpleTypeBuilder.build(type);
    
    this.builder.simpleTypeBuilder.refererStack.pop();
    
    return never(type);
  }
  
  public Object particle(XSParticle particle)
  {
    return never(particle);
  }
  
  public Object wildcard(XSWildcard wc)
  {
    return never(wc);
  }
  
  public Object annotation(XSAnnotation annon)
  {
    _assert(false);
    return null;
  }
  
  public Object notation(XSNotation not)
  {
    _assert(false);
    return null;
  }
  
  public Object facet(XSFacet decl)
  {
    _assert(false);
    return null;
  }
  
  public Object schema(XSSchema schema)
  {
    _assert(false);
    return null;
  }
  
  private JClassFactory getClassFactory(XSComponent component)
  {
    JClassFactory cf = this.owner.getClassFactory();
    if ((component instanceof XSComplexType))
    {
      XSComplexType xsct = (XSComplexType)component;
      if (xsct.isLocal())
      {
        TypeItem parent = this.owner.bindToType(xsct.getScope());
        if ((parent instanceof ClassItem)) {
          return new JClassFactoryImpl(this.owner, ((ClassItem)parent).getTypeAsDefined().parentContainer());
        }
      }
    }
    return cf;
  }
  
  private ClassItem never(XSComponent component)
  {
    return null;
  }
  
  private ClassItem allow(XSComponent component, String defaultBaseName)
  {
    BIClass decl = (BIClass)this.builder.getBindInfo(component).get(BIClass.NAME);
    if (decl == null) {
      return null;
    }
    decl.markAsAcknowledged();
    
    String clsName = decl.getClassName();
    if (clsName == null)
    {
      if (defaultBaseName == null)
      {
        this.builder.errorReceiver.error(decl.getLocation(), Messages.format("ClassSelector.ClassNameIsRequired"));
        
        defaultBaseName = "undefined" + component.hashCode();
      }
      clsName = deriveName(defaultBaseName, component);
    }
    else if (!JJavaName.isJavaIdentifier(clsName))
    {
      this.builder.errorReceiver.error(decl.getLocation(), Messages.format("ClassSelector.IncorrectClassName", clsName));
      
      clsName = "Undefined" + component.hashCode();
    }
    JDefinedClass r = getClassFactory(component).create(clsName, decl.getLocation());
    if (decl.getJavadoc() != null) {
      r.javadoc().appendComment(decl.getJavadoc() + "\n\n");
    }
    ClassItem ci = wrapByClassItem(component, r);
    
    String implClass = decl.getUserSpecifiedImplClass();
    if (implClass != null) {
      ci.setUserSpecifiedImplClass(implClass);
    }
    return ci;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\cs\DefaultClassBinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */