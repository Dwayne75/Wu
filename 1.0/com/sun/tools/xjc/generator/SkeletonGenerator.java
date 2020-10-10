package com.sun.tools.xjc.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.fmt.JStaticFile;
import com.sun.codemodel.fmt.JStaticJavaFile;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.tools.xjc.AbortException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.cls.ImplStructureStrategy;
import com.sun.tools.xjc.generator.cls.PararellStructureStrategy;
import com.sun.tools.xjc.generator.field.DefaultFieldRendererFactory;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.JAXBObject;
import com.sun.xml.bind.RIElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xml.sax.Locator;

public class SkeletonGenerator
  implements GeneratorContext
{
  private final CodeModelClassFactory codeModelClassFactory;
  private final ErrorReceiver errorReceiver;
  private final Options opts;
  private final Map packageContexts = new HashMap();
  private final Map classContexts = new HashMap();
  private final AnnotatedGrammar grammar;
  private final JCodeModel codeModel;
  private final Map runtimeClasses = new HashMap();
  private final Map fields = new HashMap();
  private final LookupTableBuilder lookupTableBuilder;
  
  public static GeneratorContext generate(AnnotatedGrammar grammar, Options opt, ErrorReceiver _errorReceiver)
  {
    try
    {
      return new SkeletonGenerator(grammar, opt, _errorReceiver);
    }
    catch (AbortException e) {}
    return null;
  }
  
  private SkeletonGenerator(AnnotatedGrammar _grammar, Options opt, ErrorReceiver _errorReceiver)
  {
    this.grammar = _grammar;
    this.opts = opt;
    this.codeModel = this.grammar.codeModel;
    this.errorReceiver = _errorReceiver;
    this.codeModelClassFactory = new CodeModelClassFactory(this.errorReceiver);
    
    populateTransducers(this.grammar);
    
    generateStaticRuntime();
    
    JPackage[] packages = this.grammar.getUsedPackages();
    if (packages.length != 0) {
      this.lookupTableBuilder = new LookupTableCache(new LookupTableInterner(new LookupTableFactory(packages[0].subPackage("impl"))));
    } else {
      this.lookupTableBuilder = null;
    }
    for (int i = 0; i < packages.length; i++)
    {
      JPackage pkg = packages[i];
      this.packageContexts.put(pkg, new PackageContext(this, this.grammar, opt, pkg));
    }
    ClassItem[] items = this.grammar.getClasses();
    
    ImplStructureStrategy strategy = new PararellStructureStrategy(this.codeModelClassFactory);
    for (int i = 0; i < items.length; i++) {
      this.classContexts.put(items[i], new ClassContext(this, strategy, items[i]));
    }
    for (int i = 0; i < items.length; i++) {
      generateClass(getClassContext(items[i]));
    }
    for (int i = 0; i < items.length; i++)
    {
      ClassContext cc = getClassContext(items[i]);
      
      ClassItem superClass = cc.target.getSuperClass();
      if (superClass != null) {
        cc.implClass._extends(getClassContext(superClass).implRef);
      } else if (this.grammar.rootClass != null) {
        cc.implClass._extends(this.grammar.rootClass);
      }
      FieldUse[] fus = items[i].getDeclaredFieldUses();
      for (int j = 0; j < fus.length; j++) {
        if (fus[j].isDelegated()) {
          generateDelegation(items[i].locator, cc.implClass, (JClass)fus[j].type, getField(fus[j]));
        }
      }
    }
  }
  
  public AnnotatedGrammar getGrammar()
  {
    return this.grammar;
  }
  
  public JCodeModel getCodeModel()
  {
    return this.codeModel;
  }
  
  public LookupTableBuilder getLookupTableBuilder()
  {
    return this.lookupTableBuilder;
  }
  
  private JPackage getRuntimePackage()
  {
    if (this.opts.runtimePackage != null) {
      return this.codeModel._package(this.opts.runtimePackage);
    }
    JPackage[] pkgs = this.grammar.getUsedPackages();
    if (pkgs.length == 0) {
      return null;
    }
    JPackage pkg = pkgs[0];
    if ((pkg.name().startsWith("org.w3")) && (pkgs.length > 1)) {
      pkg = this.grammar.getUsedPackages()[1];
    }
    return pkg.subPackage("impl.runtime");
  }
  
  private void generateStaticRuntime()
  {
    if (!this.opts.generateRuntime) {
      return;
    }
    JPackage pkg = getRuntimePackage();
    String prefix = "com/sun/tools/xjc/runtime/";
    if (pkg == null) {
      return;
    }
    BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("com/sun/tools/xjc/runtime/filelist")));
    try
    {
      String line;
      while ((line = r.readLine()) != null) {
        if (!line.startsWith("#"))
        {
          String name = line.substring(12);
          boolean forU = line.charAt(2) == 'x';
          boolean forW = line.charAt(4) == 'x';
          boolean forM = line.charAt(6) == 'x';
          boolean forV = line.charAt(8) == 'x';
          boolean must = line.charAt(10) == 'x';
          if ((must) || ((forU) && (this.opts.generateUnmarshallingCode)) || ((forW) && (this.opts.generateValidatingUnmarshallingCode)) || ((forM) && (this.opts.generateMarshallingCode)) || ((forV) && (this.opts.generateValidationCode))) {
            if (name.endsWith(".java"))
            {
              String className = name.substring(0, name.length() - 5);
              Class cls = Class.forName("com/sun/tools/xjc/runtime/".replace('/', '.') + className);
              
              addRuntime(cls);
            }
            else
            {
              JStaticFile s = new JStaticFile("com/sun/tools/xjc/runtime/" + name);
              pkg.addResourceFile(s);
            }
          }
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new JAXBAssertionError();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
      throw new JAXBAssertionError();
    }
    String line;
  }
  
  public JClass getRuntime(Class clazz)
  {
    JClass r = (JClass)this.runtimeClasses.get(clazz);
    if (r != null) {
      return r;
    }
    return addRuntime(clazz);
  }
  
  private JClass addRuntime(Class runtimeClass)
  {
    JPackage pkg = getRuntimePackage();
    String shortName = getShortName(runtimeClass.getName());
    if (!pkg.hasResourceFile(shortName + ".java"))
    {
      URL res = runtimeClass.getResource(shortName + ".java");
      if (res == null) {
        throw new JAXBAssertionError("Unable to load source code of " + runtimeClass.getName() + " as a resource");
      }
      JStaticJavaFile sjf = new JStaticJavaFile(pkg, shortName, res, new SkeletonGenerator.PreProcessor(this, null));
      if (this.opts.generateRuntime) {
        pkg.addResourceFile(sjf);
      }
      this.runtimeClasses.put(runtimeClass, sjf.getJClass());
    }
    return getRuntime(runtimeClass);
  }
  
  private String getShortName(String name)
  {
    return name.substring(name.lastIndexOf('.') + 1);
  }
  
  public ErrorReceiver getErrorReceiver()
  {
    return this.errorReceiver;
  }
  
  public CodeModelClassFactory getClassFactory()
  {
    return this.codeModelClassFactory;
  }
  
  public PackageContext getPackageContext(JPackage p)
  {
    return (PackageContext)this.packageContexts.get(p);
  }
  
  public ClassContext getClassContext(ClassItem ci)
  {
    return (ClassContext)this.classContexts.get(ci);
  }
  
  public PackageContext[] getAllPackageContexts()
  {
    return (PackageContext[])this.packageContexts.values().toArray(new PackageContext[this.packageContexts.size()]);
  }
  
  public FieldRenderer getField(FieldUse fu)
  {
    return (FieldRenderer)this.fields.get(fu);
  }
  
  private void generateClass(ClassContext cc)
  {
    if (this.grammar.serialVersionUID != null)
    {
      cc.implClass._implements(Serializable.class);
      cc.implClass.field(28, this.codeModel.LONG, "serialVersionUID", JExpr.lit(this.grammar.serialVersionUID.longValue()));
    }
    if ((cc.target.exp instanceof NameClassAndExpression))
    {
      XmlNameStoreAlgorithm nsa = XmlNameStoreAlgorithm.get((NameClassAndExpression)cc.target.exp);
      
      nsa.populate(cc);
      if ((cc.target.exp instanceof ElementExp))
      {
        cc.implClass._implements(RIElement.class);
        
        cc.implClass.method(1, String.class, "____jaxb_ri____getNamespaceURI").body()._return(nsa.getNamespaceURI());
        
        cc.implClass.method(1, String.class, "____jaxb_ri____getLocalName").body()._return(nsa.getLocalPart());
      }
    }
    cc.implClass._implements(JAXBObject.class);
    
    FieldUse[] fus = cc.target.getDeclaredFieldUses();
    for (int j = 0; j < fus.length; j++) {
      generateFieldDecl(cc, fus[j]);
    }
    if (cc.target.hasGetContentMethod) {
      generateChoiceContentField(cc);
    }
    cc._package.objectFactoryGenerator.populate(cc);
    
    cc._package.versionGenerator.generateVersionReference(cc);
  }
  
  private void generateChoiceContentField(ClassContext cc)
  {
    FieldUse[] fus = cc.target.getDeclaredFieldUses();
    
    JType[] types = new JType[fus.length];
    for (int i = 0; i < fus.length; i++)
    {
      FieldRenderer fr = getField(fus[i]);
      types[i] = fr.getValueType();
    }
    JType returnType = TypeUtil.getCommonBaseType(this.codeModel, types);
    
    MethodWriter helper = cc.createMethodWriter();
    JMethod $get = helper.declareMethod(returnType, "getContent");
    for (int i = 0; i < fus.length; i++)
    {
      FieldRenderer fr = getField(fus[i]);
      
      JBlock then = $get.body()._if(fr.hasSetValue())._then();
      then._return(fr.getValue());
    }
    $get.body()._return(JExpr._null());
    
    JMethod $isSet = helper.declareMethod(this.codeModel.BOOLEAN, "isSetContent");
    JExpression exp = JExpr.FALSE;
    for (int i = 0; i < fus.length; i++) {
      exp = exp.cor(getField(fus[i]).hasSetValue());
    }
    $isSet.body()._return(exp);
    
    JMethod $unset = helper.declareMethod(this.codeModel.VOID, "unsetContent");
    for (int i = 0; i < fus.length; i++) {
      getField(fus[i]).unsetValues($unset.body());
    }
    for (int i = 0; i < fus.length; i++)
    {
      FieldRenderer fr1 = getField(fus[i]);
      for (int j = 0; j < fus.length; j++) {
        if (i != j)
        {
          FieldRenderer fr2 = getField(fus[j]);
          fr2.unsetValues(fr1.getOnSetEventHandler());
        }
      }
    }
  }
  
  private void generateDelegation(Locator errorSource, JDefinedClass impl, JClass _intf, FieldRenderer fr)
  {
    JDefinedClass intf = (JDefinedClass)_intf;
    for (Iterator itr = intf._implements(); itr.hasNext();) {
      generateDelegation(errorSource, impl, (JClass)itr.next(), fr);
    }
    for (Iterator itr = intf.methods(); itr.hasNext();)
    {
      JMethod m = (JMethod)itr.next();
      if (impl.getMethod(m.name(), m.listParamTypes()) != null) {
        this.errorReceiver.error(errorSource, Messages.format("SkeletonGenerator.MethodCollision", m.name(), impl.fullName(), intf.fullName()));
      }
      JMethod n = impl.method(1, m.type(), m.name());
      JVar[] mp = m.listParams();
      
      JInvocation inv = fr.getValue().invoke(m);
      if (m.type() == this.codeModel.VOID) {
        n.body().add(inv);
      } else {
        n.body()._return(inv);
      }
      for (int j = 0; j < mp.length; j++) {
        inv.arg(n.param(mp[j].type(), mp[j].name()));
      }
    }
  }
  
  private void populateTransducers(AnnotatedGrammar grammar)
  {
    PrimitiveItem[] pis = grammar.getPrimitives();
    for (int i = 0; i < pis.length; i++) {
      pis[i].xducer.populate(grammar, this);
    }
  }
  
  private FieldRenderer generateFieldDecl(ClassContext cc, FieldUse fu)
  {
    FieldRendererFactory frf = fu.getRealization();
    if (frf == null) {
      frf = new DefaultFieldRendererFactory(this.codeModel);
    }
    FieldRenderer field = frf.create(cc, fu);
    field.generate();
    this.fields.put(fu, field);
    
    return field;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\SkeletonGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */