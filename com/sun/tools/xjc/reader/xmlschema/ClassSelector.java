package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.util.JavadocEscapeWriter;
import com.sun.istack.NotNull;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClass;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CClassInfoParent.Package;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CNonElement;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.LocalScoping;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.impl.util.SchemaWriter;
import com.sun.xml.xsom.util.ComponentNameFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.xml.sax.Locator;

public final class ClassSelector
  extends BindingComponent
{
  private final BGMBuilder builder = (BGMBuilder)Ring.get(BGMBuilder.class);
  private final Map<XSComponent, Binding> bindMap = new HashMap();
  final Map<XSComponent, CElementInfo> boundElements = new HashMap();
  private final Stack<Binding> bindQueue = new Stack();
  private final Set<CClassInfo> built = new HashSet();
  private final ClassBinder classBinder;
  private final Stack<CClassInfoParent> classScopes = new Stack();
  private XSComponent currentRoot;
  private CClassInfo currentBean;
  
  private final class Binding
  {
    private final XSComponent sc;
    private final CTypeInfo bean;
    
    public Binding(XSComponent sc, CTypeInfo bean)
    {
      this.sc = sc;
      this.bean = bean;
    }
    
    void build()
    {
      if (!(this.bean instanceof CClassInfo)) {
        return;
      }
      CClassInfo bean = (CClassInfo)this.bean;
      if (!ClassSelector.this.built.add(bean)) {
        return;
      }
      for (String reservedClassName : ClassSelector.reservedClassNames) {
        if (bean.getName().equals(reservedClassName))
        {
          ClassSelector.this.getErrorReporter().error(this.sc.getLocator(), "ClassSelector.ReservedClassName", new Object[] { reservedClassName });
          
          break;
        }
      }
      if (ClassSelector.this.needValueConstructor(this.sc)) {
        bean.addConstructor(new String[] { "value" });
      }
      if (bean.javadoc == null) {
        ClassSelector.this.addSchemaFragmentJavadoc(bean, this.sc);
      }
      if (ClassSelector.this.builder.getGlobalBinding().getFlattenClasses() == LocalScoping.NESTED) {
        ClassSelector.this.pushClassScope(bean);
      } else {
        ClassSelector.this.pushClassScope(bean.parent());
      }
      XSComponent oldRoot = ClassSelector.this.currentRoot;
      CClassInfo oldBean = ClassSelector.this.currentBean;
      ClassSelector.this.currentRoot = this.sc;
      ClassSelector.this.currentBean = bean;
      this.sc.visit((XSVisitor)Ring.get(BindRed.class));
      ClassSelector.this.currentBean = oldBean;
      ClassSelector.this.currentRoot = oldRoot;
      ClassSelector.this.popClassScope();
      
      BIProperty prop = (BIProperty)ClassSelector.this.builder.getBindInfo(this.sc).get(BIProperty.class);
      if (prop != null) {
        prop.markAsAcknowledged();
      }
    }
  }
  
  public ClassSelector()
  {
    this.classBinder = new Abstractifier(new DefaultClassBinder());
    Ring.add(ClassBinder.class, this.classBinder);
    
    this.classScopes.push(null);
    
    XSComplexType anyType = ((XSSchemaSet)Ring.get(XSSchemaSet.class)).getComplexType("http://www.w3.org/2001/XMLSchema", "anyType");
    this.bindMap.put(anyType, new Binding(anyType, CBuiltinLeafInfo.ANYTYPE));
  }
  
  public final CClassInfoParent getClassScope()
  {
    assert (!this.classScopes.isEmpty());
    return (CClassInfoParent)this.classScopes.peek();
  }
  
  public final void pushClassScope(CClassInfoParent clsFctry)
  {
    assert (clsFctry != null);
    this.classScopes.push(clsFctry);
  }
  
  public final void popClassScope()
  {
    this.classScopes.pop();
  }
  
  public XSComponent getCurrentRoot()
  {
    return this.currentRoot;
  }
  
  public CClassInfo getCurrentBean()
  {
    return this.currentBean;
  }
  
  public final CElement isBound(XSElementDecl x, XSComponent referer)
  {
    CElementInfo r = (CElementInfo)this.boundElements.get(x);
    if (r != null) {
      return r;
    }
    return bindToType(x, referer);
  }
  
  public CTypeInfo bindToType(XSComponent sc, XSComponent referer)
  {
    return _bindToClass(sc, referer, false);
  }
  
  public CElement bindToType(XSElementDecl e, XSComponent referer)
  {
    return (CElement)_bindToClass(e, referer, false);
  }
  
  public CClass bindToType(XSComplexType t, XSComponent referer, boolean cannotBeDelayed)
  {
    return (CClass)_bindToClass(t, referer, cannotBeDelayed);
  }
  
  public TypeUse bindToType(XSType t, XSComponent referer)
  {
    if ((t instanceof XSSimpleType)) {
      return ((SimpleTypeBuilder)Ring.get(SimpleTypeBuilder.class)).build((XSSimpleType)t);
    }
    return (CNonElement)_bindToClass(t, referer, false);
  }
  
  CTypeInfo _bindToClass(@NotNull XSComponent sc, XSComponent referer, boolean cannotBeDelayed)
  {
    if (!this.bindMap.containsKey(sc))
    {
      boolean isGlobal = false;
      if ((sc instanceof XSDeclaration))
      {
        isGlobal = ((XSDeclaration)sc).isGlobal();
        if (isGlobal) {
          pushClassScope(new CClassInfoParent.Package(getPackage(((XSDeclaration)sc).getTargetNamespace())));
        }
      }
      CElement bean = (CElement)sc.apply(this.classBinder);
      if (isGlobal) {
        popClassScope();
      }
      if (bean == null) {
        return null;
      }
      if ((bean instanceof CClassInfo))
      {
        XSSchema os = sc.getOwnerSchema();
        BISchemaBinding sb = (BISchemaBinding)this.builder.getBindInfo(os).get(BISchemaBinding.class);
        if ((sb != null) && (!sb.map))
        {
          getErrorReporter().error(sc.getLocator(), "ERR_REFERENCE_TO_NONEXPORTED_CLASS", new Object[] { sc.apply(new ComponentNameFunction()) });
          
          getErrorReporter().error(sb.getLocation(), "ERR_REFERENCE_TO_NONEXPORTED_CLASS_MAP_FALSE", new Object[] { os.getTargetNamespace() });
          if (referer != null) {
            getErrorReporter().error(referer.getLocator(), "ERR_REFERENCE_TO_NONEXPORTED_CLASS_REFERER", new Object[] { referer.apply(new ComponentNameFunction()) });
          }
        }
      }
      queueBuild(sc, bean);
    }
    Binding bind = (Binding)this.bindMap.get(sc);
    if (cannotBeDelayed) {
      bind.build();
    }
    return bind.bean;
  }
  
  public void executeTasks()
  {
    while (this.bindQueue.size() != 0) {
      ((Binding)this.bindQueue.pop()).build();
    }
  }
  
  private boolean needValueConstructor(XSComponent sc)
  {
    if (!(sc instanceof XSElementDecl)) {
      return false;
    }
    XSElementDecl decl = (XSElementDecl)sc;
    if (!decl.getType().isSimpleType()) {
      return false;
    }
    return true;
  }
  
  private static final String[] reservedClassNames = { "ObjectFactory" };
  
  public void queueBuild(XSComponent sc, CElement bean)
  {
    Binding b = new Binding(sc, bean);
    this.bindQueue.push(b);
    Binding old = (Binding)this.bindMap.put(sc, b);
    assert ((old == null) || (old.bean == bean));
  }
  
  private void addSchemaFragmentJavadoc(CClassInfo bean, XSComponent sc)
  {
    String doc = this.builder.getBindInfo(sc).getDocumentation();
    if (doc != null) {
      append(bean, doc);
    }
    Locator loc = sc.getLocator();
    String fileName = null;
    if (loc != null)
    {
      fileName = loc.getPublicId();
      if (fileName == null) {
        fileName = loc.getSystemId();
      }
    }
    if (fileName == null) {
      fileName = "";
    }
    String lineNumber = Messages.format("ClassSelector.JavadocLineUnknown", new Object[0]);
    if ((loc != null) && (loc.getLineNumber() != -1)) {
      lineNumber = String.valueOf(loc.getLineNumber());
    }
    String componentName = (String)sc.apply(new ComponentNameFunction());
    String jdoc = Messages.format("ClassSelector.JavadocHeading", new Object[] { componentName, fileName, lineNumber });
    append(bean, jdoc);
    
    StringWriter out = new StringWriter();
    out.write("<pre>\n");
    SchemaWriter sw = new SchemaWriter(new JavadocEscapeWriter(out));
    sc.visit(sw);
    out.write("</pre>");
    append(bean, out.toString());
  }
  
  private void append(CClassInfo bean, String doc)
  {
    if (bean.javadoc == null)
    {
      bean.javadoc = (doc + '\n');
    }
    else
    {
      CClassInfo tmp41_40 = bean;tmp41_40.javadoc = (tmp41_40.javadoc + '\n' + doc + '\n');
    }
  }
  
  private static Set<String> checkedPackageNames = new HashSet();
  
  public JPackage getPackage(String targetNamespace)
  {
    XSSchema s = ((XSSchemaSet)Ring.get(XSSchemaSet.class)).getSchema(targetNamespace);
    
    BISchemaBinding sb = (BISchemaBinding)this.builder.getBindInfo(s).get(BISchemaBinding.class);
    if (sb != null) {
      sb.markAsAcknowledged();
    }
    String name = null;
    if (this.builder.defaultPackage1 != null) {
      name = this.builder.defaultPackage1;
    }
    if ((name == null) && (sb != null) && (sb.getPackageName() != null)) {
      name = sb.getPackageName();
    }
    if ((name == null) && (this.builder.defaultPackage2 != null)) {
      name = this.builder.defaultPackage2;
    }
    if (name == null) {
      name = this.builder.getNameConverter().toPackageName(targetNamespace);
    }
    if (name == null) {
      name = "generated";
    }
    if (checkedPackageNames.add(name)) {
      if (!JJavaName.isJavaPackageName(name)) {
        getErrorReporter().error(s.getLocator(), "ClassSelector.IncorrectPackageName", new Object[] { targetNamespace, name });
      }
    }
    return ((JCodeModel)Ring.get(JCodeModel.class))._package(name);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ClassSelector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */