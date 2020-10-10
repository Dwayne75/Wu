package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.fmt.JStaticJavaFile;
import com.sun.istack.NotNull;
import com.sun.tools.xjc.AbortException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.api.SpecVersion;
import com.sun.tools.xjc.generator.annotation.spec.XmlAnyAttributeWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlEnumValueWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlEnumWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlJavaTypeAdapterWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlMimeTypeWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlRootElementWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlSeeAlsoWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlTypeWriter;
import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CClassInfoParent.Visitor;
import com.sun.tools.xjc.model.CClassRef;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CEnumConstant;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.model.CNonElement;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.EnumConstantOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import com.sun.xml.xsom.XmlString;
import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.activation.MimeType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.namespace.QName;

public final class BeanGenerator
  implements Outline
{
  private final CodeModelClassFactory codeModelClassFactory;
  private final ErrorReceiver errorReceiver;
  private final Map<JPackage, PackageOutlineImpl> packageContexts = new HashMap();
  private final Map<CClassInfo, ClassOutlineImpl> classes = new HashMap();
  private final Map<CEnumLeafInfo, EnumOutline> enums = new HashMap();
  private final Map<Class, JClass> generatedRuntime = new HashMap();
  private final Model model;
  private final JCodeModel codeModel;
  private final Map<CPropertyInfo, FieldOutline> fields = new HashMap();
  final Map<CElementInfo, ElementOutlineImpl> elements = new HashMap();
  
  public static Outline generate(Model model, ErrorReceiver _errorReceiver)
  {
    try
    {
      return new BeanGenerator(model, _errorReceiver);
    }
    catch (AbortException e) {}
    return null;
  }
  
  private BeanGenerator(Model _model, ErrorReceiver _errorReceiver)
  {
    this.model = _model;
    this.codeModel = this.model.codeModel;
    this.errorReceiver = _errorReceiver;
    this.codeModelClassFactory = new CodeModelClassFactory(this.errorReceiver);
    for (CEnumLeafInfo p : this.model.enums().values()) {
      this.enums.put(p, generateEnumDef(p));
    }
    JPackage[] packages = getUsedPackages(Aspect.EXPOSED);
    for (JPackage pkg : packages) {
      getPackageContext(pkg);
    }
    for (CClassInfo bean : this.model.beans().values()) {
      getClazz(bean);
    }
    for (PackageOutlineImpl p : this.packageContexts.values()) {
      p.calcDefaultValues();
    }
    JClass OBJECT = this.codeModel.ref(Object.class);
    for (ClassOutlineImpl cc : getClasses())
    {
      CClassInfo superClass = cc.target.getBaseClass();
      if (superClass != null)
      {
        this.model.strategy._extends(cc, getClazz(superClass));
      }
      else
      {
        CClassRef refSuperClass = cc.target.getRefBaseClass();
        if (refSuperClass != null)
        {
          cc.implClass._extends(refSuperClass.toType(this, Aspect.EXPOSED));
        }
        else
        {
          if ((this.model.rootClass != null) && (cc.implClass._extends().equals(OBJECT))) {
            cc.implClass._extends(this.model.rootClass);
          }
          if (this.model.rootInterface != null) {
            cc.ref._implements(this.model.rootInterface);
          }
        }
      }
    }
    for (ClassOutlineImpl co : getClasses()) {
      generateClassBody(co);
    }
    for (EnumOutline eo : this.enums.values()) {
      generateEnumBody(eo);
    }
    for (CElementInfo ei : this.model.getAllElements()) {
      getPackageContext(ei._package()).objectFactoryGenerator().populate(ei);
    }
    if (this.model.options.debugMode) {
      generateClassList();
    }
  }
  
  private void generateClassList()
  {
    try
    {
      JDefinedClass jc = this.codeModel.rootPackage()._class("JAXBDebug");
      JMethod m = jc.method(17, JAXBContext.class, "createContext");
      JVar $classLoader = m.param(ClassLoader.class, "classLoader");
      m._throws(JAXBException.class);
      JInvocation inv = this.codeModel.ref(JAXBContext.class).staticInvoke("newInstance");
      m.body()._return(inv);
      switch (this.model.strategy)
      {
      case INTF_AND_IMPL: 
        StringBuilder buf = new StringBuilder();
        for (PackageOutlineImpl po : this.packageContexts.values())
        {
          if (buf.length() > 0) {
            buf.append(':');
          }
          buf.append(po._package().name());
        }
        inv.arg(buf.toString()).arg($classLoader);
        break;
      case BEAN_ONLY: 
        for (ClassOutlineImpl cc : getClasses()) {
          inv.arg(cc.implRef.dotclass());
        }
        for (PackageOutlineImpl po : this.packageContexts.values()) {
          inv.arg(po.objectFactory().dotclass());
        }
        break;
      default: 
        throw new IllegalStateException();
      }
    }
    catch (JClassAlreadyExistsException e)
    {
      e.printStackTrace();
    }
  }
  
  public Model getModel()
  {
    return this.model;
  }
  
  public JCodeModel getCodeModel()
  {
    return this.codeModel;
  }
  
  public JClassContainer getContainer(CClassInfoParent parent, Aspect aspect)
  {
    CClassInfoParent.Visitor<JClassContainer> v;
    switch (aspect)
    {
    case EXPOSED: 
      v = this.exposedContainerBuilder;
      break;
    case IMPLEMENTATION: 
      v = this.implContainerBuilder;
      break;
    default: 
      if (!$assertionsDisabled) {
        throw new AssertionError();
      }
      throw new IllegalStateException();
    }
    return (JClassContainer)parent.accept(v);
  }
  
  public final JType resolve(CTypeRef ref, Aspect a)
  {
    return ((NType)ref.getTarget().getType()).toType(this, a);
  }
  
  private final CClassInfoParent.Visitor<JClassContainer> exposedContainerBuilder = new CClassInfoParent.Visitor()
  {
    public JClassContainer onBean(CClassInfo bean)
    {
      return BeanGenerator.this.getClazz(bean).ref;
    }
    
    public JClassContainer onElement(CElementInfo element)
    {
      return BeanGenerator.this.getElement(element).implClass;
    }
    
    public JClassContainer onPackage(JPackage pkg)
    {
      return BeanGenerator.this.model.strategy.getPackage(pkg, Aspect.EXPOSED);
    }
  };
  private final CClassInfoParent.Visitor<JClassContainer> implContainerBuilder = new CClassInfoParent.Visitor()
  {
    public JClassContainer onBean(CClassInfo bean)
    {
      return BeanGenerator.this.getClazz(bean).implClass;
    }
    
    public JClassContainer onElement(CElementInfo element)
    {
      return BeanGenerator.this.getElement(element).implClass;
    }
    
    public JClassContainer onPackage(JPackage pkg)
    {
      return BeanGenerator.this.model.strategy.getPackage(pkg, Aspect.IMPLEMENTATION);
    }
  };
  
  public final JPackage[] getUsedPackages(Aspect aspect)
  {
    Set<JPackage> s = new TreeSet();
    for (CClassInfo bean : this.model.beans().values())
    {
      JClassContainer cont = getContainer(bean.parent(), aspect);
      if (cont.isPackage()) {
        s.add((JPackage)cont);
      }
    }
    for (CElementInfo e : this.model.getElementMappings(null).values()) {
      s.add(e._package());
    }
    return (JPackage[])s.toArray(new JPackage[s.size()]);
  }
  
  public ErrorReceiver getErrorReceiver()
  {
    return this.errorReceiver;
  }
  
  public CodeModelClassFactory getClassFactory()
  {
    return this.codeModelClassFactory;
  }
  
  public PackageOutlineImpl getPackageContext(JPackage p)
  {
    PackageOutlineImpl r = (PackageOutlineImpl)this.packageContexts.get(p);
    if (r == null)
    {
      r = new PackageOutlineImpl(this, this.model, p);
      this.packageContexts.put(p, r);
    }
    return r;
  }
  
  private ClassOutlineImpl generateClassDef(CClassInfo bean)
  {
    ImplStructureStrategy.Result r = this.model.strategy.createClasses(this, bean);
    JClass implRef;
    JClass implRef;
    if (bean.getUserSpecifiedImplClass() != null)
    {
      JDefinedClass usr;
      try
      {
        usr = this.codeModel._class(bean.getUserSpecifiedImplClass());
        
        usr.hide();
      }
      catch (JClassAlreadyExistsException e)
      {
        usr = e.getExistingClass();
      }
      usr._extends(r.implementation);
      implRef = usr;
    }
    else
    {
      implRef = r.implementation;
    }
    return new ClassOutlineImpl(this, bean, r.exposed, r.implementation, implRef);
  }
  
  public Collection<ClassOutlineImpl> getClasses()
  {
    assert (this.model.beans().size() == this.classes.size());
    return this.classes.values();
  }
  
  public ClassOutlineImpl getClazz(CClassInfo bean)
  {
    ClassOutlineImpl r = (ClassOutlineImpl)this.classes.get(bean);
    if (r == null) {
      this.classes.put(bean, r = generateClassDef(bean));
    }
    return r;
  }
  
  public ElementOutlineImpl getElement(CElementInfo ei)
  {
    ElementOutlineImpl def = (ElementOutlineImpl)this.elements.get(ei);
    if ((def == null) && (ei.hasClass())) {
      def = new ElementOutlineImpl(this, ei);
    }
    return def;
  }
  
  public EnumOutline getEnum(CEnumLeafInfo eli)
  {
    return (EnumOutline)this.enums.get(eli);
  }
  
  public Collection<EnumOutline> getEnums()
  {
    return this.enums.values();
  }
  
  public Iterable<? extends PackageOutline> getAllPackageContexts()
  {
    return this.packageContexts.values();
  }
  
  public FieldOutline getField(CPropertyInfo prop)
  {
    return (FieldOutline)this.fields.get(prop);
  }
  
  private void generateClassBody(ClassOutlineImpl cc)
  {
    CClassInfo target = cc.target;
    if (this.model.serializable)
    {
      cc.implClass._implements(Serializable.class);
      if (this.model.serialVersionUID != null) {
        cc.implClass.field(28, this.codeModel.LONG, "serialVersionUID", JExpr.lit(this.model.serialVersionUID.longValue()));
      }
    }
    String mostUsedNamespaceURI = cc._package().getMostUsedNamespaceURI();
    
    XmlTypeWriter xtw = (XmlTypeWriter)cc.implClass.annotate2(XmlTypeWriter.class);
    writeTypeName(cc.target.getTypeName(), xtw, mostUsedNamespaceURI);
    if (this.model.options.target.isLaterThan(SpecVersion.V2_1))
    {
      Iterator<CClassInfo> subclasses = cc.target.listSubclasses();
      if (subclasses.hasNext())
      {
        XmlSeeAlsoWriter saw = (XmlSeeAlsoWriter)cc.implClass.annotate2(XmlSeeAlsoWriter.class);
        while (subclasses.hasNext())
        {
          CClassInfo s = (CClassInfo)subclasses.next();
          saw.value(getClazz(s).implRef);
        }
      }
    }
    if (target.isElement())
    {
      String namespaceURI = target.getElementName().getNamespaceURI();
      String localPart = target.getElementName().getLocalPart();
      
      XmlRootElementWriter xrew = (XmlRootElementWriter)cc.implClass.annotate2(XmlRootElementWriter.class);
      xrew.name(localPart);
      if (!namespaceURI.equals(mostUsedNamespaceURI)) {
        xrew.namespace(namespaceURI);
      }
    }
    if (target.isOrdered()) {
      for (CPropertyInfo p : target.getProperties()) {
        if (!(p instanceof CAttributePropertyInfo)) {
          xtw.propOrder(p.getName(false));
        }
      }
    } else {
      xtw.getAnnotationUse().paramArray("propOrder");
    }
    for (CPropertyInfo prop : target.getProperties()) {
      generateFieldDecl(cc, prop);
    }
    if (target.declaresAttributeWildcard()) {
      generateAttributeWildcard(cc);
    }
    cc.ref.javadoc().append(target.javadoc);
    
    cc._package().objectFactoryGenerator().populate(cc);
  }
  
  private void writeTypeName(QName typeName, XmlTypeWriter xtw, String mostUsedNamespaceURI)
  {
    if (typeName == null)
    {
      xtw.name("");
    }
    else
    {
      xtw.name(typeName.getLocalPart());
      String typeNameURI = typeName.getNamespaceURI();
      if (!typeNameURI.equals(mostUsedNamespaceURI)) {
        xtw.namespace(typeNameURI);
      }
    }
  }
  
  private void generateAttributeWildcard(ClassOutlineImpl cc)
  {
    String FIELD_NAME = "otherAttributes";
    String METHOD_SEED = this.model.getNameConverter().toClassName(FIELD_NAME);
    
    JClass mapType = this.codeModel.ref(Map.class).narrow(new Class[] { QName.class, String.class });
    JClass mapImpl = this.codeModel.ref(HashMap.class).narrow(new Class[] { QName.class, String.class });
    
    JFieldVar $ref = cc.implClass.field(4, mapType, FIELD_NAME, JExpr._new(mapImpl));
    
    $ref.annotate2(XmlAnyAttributeWriter.class);
    
    MethodWriter writer = cc.createMethodWriter();
    
    JMethod $get = writer.declareMethod(mapType, "get" + METHOD_SEED);
    $get.javadoc().append("Gets a map that contains attributes that aren't bound to any typed property on this class.\n\n<p>\nthe map is keyed by the name of the attribute and \nthe value is the string value of the attribute.\n\nthe map returned by this method is live, and you can add new attribute\nby updating the map directly. Because of this design, there's no setter.\n");
    
    $get.javadoc().addReturn().append("always non-null");
    
    $get.body()._return($ref);
  }
  
  private EnumOutline generateEnumDef(CEnumLeafInfo e)
  {
    JDefinedClass type = getClassFactory().createClass(getContainer(e.parent, Aspect.EXPOSED), e.shortName, e.getLocator(), ClassType.ENUM);
    
    type.javadoc().append(e.javadoc);
    
    new EnumOutline(e, type)
    {
      @NotNull
      public Outline parent()
      {
        return BeanGenerator.this;
      }
    };
  }
  
  private void generateEnumBody(EnumOutline eo)
  {
    JDefinedClass type = eo.clazz;
    CEnumLeafInfo e = eo.target;
    
    XmlTypeWriter xtw = (XmlTypeWriter)type.annotate2(XmlTypeWriter.class);
    writeTypeName(e.getTypeName(), xtw, eo._package().getMostUsedNamespaceURI());
    
    JCodeModel codeModel = this.model.codeModel;
    
    JType baseExposedType = e.base.toType(this, Aspect.EXPOSED).unboxify();
    JType baseImplType = e.base.toType(this, Aspect.IMPLEMENTATION).unboxify();
    
    XmlEnumWriter xew = (XmlEnumWriter)type.annotate2(XmlEnumWriter.class);
    xew.value(baseExposedType);
    
    boolean needsValue = e.needsValueField();
    
    Set<String> enumFieldNames = new HashSet();
    for (CEnumConstant mem : e.members)
    {
      String constName = mem.getName();
      if (!JJavaName.isJavaIdentifier(constName)) {
        getErrorReceiver().error(e.getLocator(), Messages.ERR_UNUSABLE_NAME.format(new Object[] { mem.getLexicalValue(), constName }));
      }
      if (!enumFieldNames.add(constName)) {
        getErrorReceiver().error(e.getLocator(), Messages.ERR_NAME_COLLISION.format(new Object[] { constName }));
      }
      JEnumConstant constRef = type.enumConstant(constName);
      if (needsValue) {
        constRef.arg(e.base.createConstant(this, new XmlString(mem.getLexicalValue())));
      }
      if (!mem.getLexicalValue().equals(constName)) {
        ((XmlEnumValueWriter)constRef.annotate2(XmlEnumValueWriter.class)).value(mem.getLexicalValue());
      }
      if (mem.javadoc != null) {
        constRef.javadoc().append(mem.javadoc);
      }
      eo.constants.add(new EnumConstantOutline(mem, constRef) {});
    }
    if (needsValue)
    {
      JFieldVar $value = type.field(12, baseExposedType, "value");
      
      type.method(1, baseExposedType, "value").body()._return($value);
      
      JMethod m = type.constructor(0);
      m.body().assign($value, m.param(baseImplType, "v"));
      
      JMethod m = type.method(17, type, "fromValue");
      JVar $v = m.param(baseExposedType, "v");
      JForEach fe = m.body().forEach(type, "c", type.staticInvoke("values"));
      JExpression eq;
      JExpression eq;
      if (baseExposedType.isPrimitive()) {
        eq = fe.var().ref($value).eq($v);
      } else {
        eq = fe.var().ref($value).invoke("equals").arg($v);
      }
      fe.body()._if(eq)._then()._return(fe.var());
      
      JInvocation ex = JExpr._new(codeModel.ref(IllegalArgumentException.class));
      JExpression strForm;
      JExpression strForm;
      if (baseExposedType.isPrimitive())
      {
        strForm = codeModel.ref(String.class).staticInvoke("valueOf").arg($v);
      }
      else
      {
        JExpression strForm;
        if (baseExposedType == codeModel.ref(String.class)) {
          strForm = $v;
        } else {
          strForm = $v.invoke("toString");
        }
      }
      m.body()._throw(ex.arg(strForm));
    }
    else
    {
      type.method(1, String.class, "value").body()._return(JExpr.invoke("name"));
      
      JMethod m = type.method(17, type, "fromValue");
      m.body()._return(JExpr.invoke("valueOf").arg(m.param(String.class, "v")));
    }
  }
  
  private FieldOutline generateFieldDecl(ClassOutlineImpl cc, CPropertyInfo prop)
  {
    FieldRenderer fr = prop.realization;
    if (fr == null) {
      fr = this.model.options.getFieldRendererFactory().getDefault();
    }
    FieldOutline field = fr.generate(cc, prop);
    this.fields.put(prop, field);
    
    return field;
  }
  
  public final void generateAdapterIfNecessary(CPropertyInfo prop, JAnnotatable field)
  {
    CAdapter adapter = prop.getAdapter();
    if (adapter != null) {
      if (adapter.getAdapterIfKnown() == SwaRefAdapter.class)
      {
        field.annotate(XmlAttachmentRef.class);
      }
      else
      {
        XmlJavaTypeAdapterWriter xjtw = (XmlJavaTypeAdapterWriter)field.annotate2(XmlJavaTypeAdapterWriter.class);
        xjtw.value(((NClass)adapter.adapterType).toType(this, Aspect.EXPOSED));
      }
    }
    switch (prop.id())
    {
    case ID: 
      field.annotate(XmlID.class);
      break;
    case IDREF: 
      field.annotate(XmlIDREF.class);
    }
    if (prop.getExpectedMimeType() != null) {
      ((XmlMimeTypeWriter)field.annotate2(XmlMimeTypeWriter.class)).value(prop.getExpectedMimeType().toString());
    }
  }
  
  public final JClass addRuntime(Class clazz)
  {
    JClass g = (JClass)this.generatedRuntime.get(clazz);
    if (g == null)
    {
      JPackage implPkg = getUsedPackages(Aspect.IMPLEMENTATION)[0].subPackage("runtime");
      g = generateStaticClass(clazz, implPkg);
      this.generatedRuntime.put(clazz, g);
    }
    return g;
  }
  
  public JClass generateStaticClass(Class src, JPackage out)
  {
    String shortName = getShortName(src.getName());
    
    URL res = src.getResource(shortName + ".java");
    if (res == null) {
      res = src.getResource(shortName + ".java_");
    }
    if (res == null) {
      throw new InternalError("Unable to load source code of " + src.getName() + " as a resource");
    }
    JStaticJavaFile sjf = new JStaticJavaFile(out, shortName, res, null);
    out.addResourceFile(sjf);
    return sjf.getJClass();
  }
  
  private String getShortName(String name)
  {
    return name.substring(name.lastIndexOf('.') + 1);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\BeanGenerator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */