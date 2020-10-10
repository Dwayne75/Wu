package com.sun.tools.xjc.reader.xmlschema.cs;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.util.JavadocEscapeWriter;
import com.sun.msv.grammar.Expression;
import com.sun.msv.util.LightStack;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.FieldBuilder;
import com.sun.tools.xjc.reader.xmlschema.JClassFactory;
import com.sun.tools.xjc.reader.xmlschema.PrefixedJClassFactoryImpl;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.impl.util.SchemaWriter;
import com.sun.xml.xsom.util.ComponentNameFunction;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.xml.sax.Locator;

public class ClassSelector
{
  private final String defaultPackageName;
  public final BGMBuilder builder;
  protected final AGMFragmentBuilder agmFragmentBuilder;
  public final CodeModelClassFactory codeModelClassFactory;
  private final Map bindMap = new HashMap();
  private final LightStack bindQueue = new LightStack();
  private final ClassBinder classBinder;
  private final DOMBinder domBinder;
  private final Stack classFactories = new Stack();
  
  public ClassSelector(BGMBuilder _builder, String defaultPackage)
  {
    this.builder = _builder;
    this.agmFragmentBuilder = new AGMFragmentBuilder(_builder);
    this.codeModelClassFactory = new CodeModelClassFactory(this.builder.getErrorReceiver());
    
    this.domBinder = new DOMBinder(this);
    this.defaultPackageName = defaultPackage;
    
    ClassBinder c = new DefaultClassBinder(this);
    if (this.builder.getGlobalBinding().isModelGroupBinding()) {
      c = new ModelGroupBindingClassBinder(this, c);
    }
    this.classBinder = c;
    
    this.classFactories.push(null);
  }
  
  public final JClassFactory getClassFactory()
  {
    return (JClassFactory)this.classFactories.peek();
  }
  
  public final void pushClassFactory(JClassFactory clsFctry)
  {
    this.classFactories.push(clsFctry);
  }
  
  public final void popClassFactory()
  {
    this.classFactories.pop();
  }
  
  public ClassItem bindToType(XSComplexType ct)
  {
    return _bindToClass(ct, true);
  }
  
  public TypeItem bindToType(XSElementDecl e)
  {
    TypeItem t = this.domBinder.bind(e);
    if (t != null) {
      return t;
    }
    return _bindToClass(e, false);
  }
  
  public Expression bindToType(XSComponent sc)
  {
    Expression t = this.domBinder.bind(sc);
    if (t != null) {
      return t;
    }
    return _bindToClass(sc, false);
  }
  
  private ClassItem _bindToClass(XSComponent sc, boolean cannotBeDelayed)
  {
    if (!this.bindMap.containsKey(sc))
    {
      if ((sc instanceof XSElementDecl)) {
        checkAbstractComplexType((XSElementDecl)sc);
      }
      boolean isGlobal = false;
      if ((sc instanceof XSDeclaration))
      {
        isGlobal = ((XSDeclaration)sc).isGlobal();
        if (isGlobal) {
          pushClassFactory(new JClassFactoryImpl(this, getPackage(((XSDeclaration)sc).getTargetNamespace())));
        }
      }
      ClassItem ci = (ClassItem)sc.apply(this.classBinder);
      if (isGlobal) {
        popClassFactory();
      }
      if (ci == null) {
        return null;
      }
      queueBuild(sc, ci);
    }
    ClassSelector.Binding bind = (ClassSelector.Binding)this.bindMap.get(sc);
    if (cannotBeDelayed) {
      bind.build();
    }
    return ClassSelector.Binding.access$200(bind);
  }
  
  public void executeTasks()
  {
    while (this.bindQueue.size() != 0) {
      ((ClassSelector.Binding)this.bindQueue.pop()).build();
    }
  }
  
  private Set reportedAbstractComplexTypes = null;
  
  private void checkAbstractComplexType(XSElementDecl decl)
  {
    if (this.builder.inExtensionMode) {
      return;
    }
    XSType t = decl.getType();
    if ((t.isComplexType()) && (t.asComplexType().isAbstract()))
    {
      if (this.reportedAbstractComplexTypes == null) {
        this.reportedAbstractComplexTypes = new HashSet();
      }
      if (this.reportedAbstractComplexTypes.add(t))
      {
        this.builder.errorReceiver.error(t.getLocator(), Messages.format("ClassSelector.AbstractComplexType", t.getName()));
        
        this.builder.errorReceiver.error(decl.getLocator(), Messages.format("ClassSelector.AbstractComplexType.SourceLocation"));
      }
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
  
  public void queueBuild(XSComponent sc, ClassItem ci)
  {
    ClassSelector.Binding b = new ClassSelector.Binding(this, sc, ci);
    this.bindQueue.push(b);
    Object o = this.bindMap.put(sc, b);
    _assert(o == null);
  }
  
  private void build(XSComponent sc, ClassItem ci)
  {
    _assert(ClassSelector.Binding.access$200((ClassSelector.Binding)this.bindMap.get(sc)) == ci);
    for (int i = 0; i < reservedClassNames.length; i++) {
      if (ci.getTypeAsDefined().name().equals(reservedClassNames[i]))
      {
        this.builder.errorReceiver.error(sc.getLocator(), Messages.format("ClassSelector.ReservedClassName", reservedClassNames[i]));
        
        break;
      }
    }
    addSchemaFragmentJavadoc(ci.getTypeAsDefined().javadoc(), sc);
    if (com.sun.tools.xjc.util.Util.getSystemProperty(getClass(), "nestedInterface") != null) {
      pushClassFactory(new PrefixedJClassFactoryImpl(this.builder, ci.getTypeAsDefined()));
    } else {
      pushClassFactory(new JClassFactoryImpl(this, ci.getTypeAsDefined()));
    }
    ci.exp = this.builder.fieldBuilder.build(sc);
    
    ci.agm.exp = this.agmFragmentBuilder.build(sc, ci);
    
    popClassFactory();
    
    BIProperty prop = (BIProperty)this.builder.getBindInfo(sc).get(BIProperty.NAME);
    if (prop != null) {
      prop.markAsAcknowledged();
    }
    if (ci.hasGetContentMethod) {
      ci.exp.visit(new ClassSelector.1(this));
    }
  }
  
  private void addSchemaFragmentJavadoc(JDocComment javadoc, XSComponent sc)
  {
    BindInfo bi = this.builder.getBindInfo(sc);
    String doc = bi.getDocumentation();
    if ((doc != null) && (bi.hasTitleInDocumentation()))
    {
      javadoc.appendComment(doc);
      javadoc.appendComment("\n");
    }
    StringWriter out = new StringWriter();
    SchemaWriter sw = new SchemaWriter(new JavadocEscapeWriter(out));
    sc.visit(sw);
    
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
    String lineNumber = Messages.format("ClassSelector.JavadocLineUnknown");
    if ((loc != null) && (loc.getLineNumber() != -1)) {
      lineNumber = String.valueOf(loc.getLineNumber());
    }
    String componentName = (String)sc.apply(new ComponentNameFunction());
    
    javadoc.appendComment(Messages.format("ClassSelector.JavadocHeading", componentName, fileName, lineNumber));
    if ((doc != null) && (!bi.hasTitleInDocumentation()))
    {
      javadoc.appendComment("\n");
      javadoc.appendComment(doc);
      javadoc.appendComment("\n");
    }
    javadoc.appendComment("\n<p>\n<pre>\n");
    javadoc.appendComment(out.getBuffer().toString());
    javadoc.appendComment("</pre>");
  }
  
  private static Set checkedPackageNames = new HashSet();
  
  public JPackage getPackage(String targetNamespace)
  {
    XSSchema s = this.builder.schemas.getSchema(targetNamespace);
    
    BISchemaBinding sb = (BISchemaBinding)this.builder.getBindInfo(s).get(BISchemaBinding.NAME);
    
    String name = null;
    if (this.defaultPackageName != null) {
      name = this.defaultPackageName;
    }
    if ((name == null) && (sb != null) && (sb.getPackageName() != null)) {
      name = sb.getPackageName();
    }
    if (name == null) {
      name = com.sun.tools.xjc.reader.Util.getPackageNameFromNamespaceURI(targetNamespace, this.builder.getNameConverter());
    }
    if (name == null) {
      name = "generated";
    }
    if (checkedPackageNames.add(name)) {
      if (!JJavaName.isJavaPackageName(name)) {
        this.builder.errorReceiver.error(s.getLocator(), Messages.format("ClassSelector.IncorrectPackageName", targetNamespace, name));
      }
    }
    return this.builder.grammar.codeModel._package(name);
  }
  
  private static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\cs\ClassSelector.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */