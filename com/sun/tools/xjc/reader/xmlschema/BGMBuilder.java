package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.fmt.JTextFile;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.tools.xjc.model.CClassInfoParent.Package;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.ModelChecker;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDom;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISerializable;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.util.XSFinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.xml.sax.Locator;

public class BGMBuilder
  extends BindingComponent
{
  public final boolean inExtensionMode;
  public final String defaultPackage1;
  public final String defaultPackage2;
  
  public static Model build(XSSchemaSet _schemas, JCodeModel codeModel, ErrorReceiver _errorReceiver, Options opts)
  {
    Ring old = Ring.begin();
    try
    {
      ErrorReceiverFilter ef = new ErrorReceiverFilter(_errorReceiver);
      
      Ring.add(XSSchemaSet.class, _schemas);
      Ring.add(codeModel);
      Model model = new Model(opts, codeModel, null, opts.classNameAllocator, _schemas);
      Ring.add(model);
      Ring.add(ErrorReceiver.class, ef);
      Ring.add(CodeModelClassFactory.class, new CodeModelClassFactory(ef));
      
      BGMBuilder builder = new BGMBuilder(opts.defaultPackage, opts.defaultPackage2, opts.isExtensionMode(), opts.getFieldRendererFactory());
      
      builder._build();
      Model localModel1;
      if (ef.hadError()) {
        return null;
      }
      return model;
    }
    finally
    {
      Ring.end(old);
    }
  }
  
  private final BindGreen green = (BindGreen)Ring.get(BindGreen.class);
  private final BindPurple purple = (BindPurple)Ring.get(BindPurple.class);
  public final Model model = (Model)Ring.get(Model.class);
  public final FieldRendererFactory fieldRendererFactory;
  private RefererFinder refFinder;
  private BIGlobalBinding globalBinding;
  private ParticleBinder particleBinder;
  
  protected BGMBuilder(String defaultPackage1, String defaultPackage2, boolean _inExtensionMode, FieldRendererFactory fieldRendererFactory)
  {
    this.inExtensionMode = _inExtensionMode;
    this.defaultPackage1 = defaultPackage1;
    this.defaultPackage2 = defaultPackage2;
    this.fieldRendererFactory = fieldRendererFactory;
    
    DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
    
    promoteGlobalBindings();
  }
  
  private void _build()
  {
    buildContents();
    getClassSelector().executeTasks();
    
    ((UnusedCustomizationChecker)Ring.get(UnusedCustomizationChecker.class)).run();
    
    ((ModelChecker)Ring.get(ModelChecker.class)).check();
  }
  
  private void promoteGlobalBindings()
  {
    XSSchemaSet schemas = (XSSchemaSet)Ring.get(XSSchemaSet.class);
    for (XSSchema s : schemas.getSchemas())
    {
      BindInfo bi = getBindInfo(s);
      
      this.model.getCustomizations().addAll(bi.toCustomizationList());
      
      BIGlobalBinding gb = (BIGlobalBinding)bi.get(BIGlobalBinding.class);
      if (gb != null) {
        if (this.globalBinding == null)
        {
          this.globalBinding = gb;
          this.globalBinding.markAsAcknowledged();
        }
        else
        {
          gb.markAsAcknowledged();
          getErrorReporter().error(gb.getLocation(), "ERR_MULTIPLE_GLOBAL_BINDINGS", new Object[0]);
          
          getErrorReporter().error(this.globalBinding.getLocation(), "ERR_MULTIPLE_GLOBAL_BINDINGS_OTHER", new Object[0]);
        }
      }
    }
    if (this.globalBinding == null)
    {
      this.globalBinding = new BIGlobalBinding();
      BindInfo big = new BindInfo();
      big.addDecl(this.globalBinding);
      big.setOwner(this, null);
    }
    this.model.strategy = this.globalBinding.getCodeGenerationStrategy();
    this.model.rootClass = this.globalBinding.getSuperClass();
    this.model.rootInterface = this.globalBinding.getSuperInterface();
    
    this.particleBinder = (this.globalBinding.isSimpleMode() ? new ExpressionParticleBinder() : new DefaultParticleBinder());
    
    BISerializable serial = this.globalBinding.getSerializable();
    if (serial != null)
    {
      this.model.serializable = true;
      this.model.serialVersionUID = serial.uid;
    }
    if (this.globalBinding.nameConverter != null) {
      this.model.setNameConverter(this.globalBinding.nameConverter);
    }
    this.globalBinding.dispatchGlobalConversions(schemas);
    
    this.globalBinding.errorCheck();
  }
  
  @NotNull
  public BIGlobalBinding getGlobalBinding()
  {
    return this.globalBinding;
  }
  
  @NotNull
  public ParticleBinder getParticleBinder()
  {
    return this.particleBinder;
  }
  
  public NameConverter getNameConverter()
  {
    return this.model.getNameConverter();
  }
  
  private void buildContents()
  {
    ClassSelector cs = getClassSelector();
    SimpleTypeBuilder stb = (SimpleTypeBuilder)Ring.get(SimpleTypeBuilder.class);
    for (XSSchema s : ((XSSchemaSet)Ring.get(XSSchemaSet.class)).getSchemas())
    {
      BISchemaBinding sb = (BISchemaBinding)getBindInfo(s).get(BISchemaBinding.class);
      if ((sb != null) && (!sb.map))
      {
        sb.markAsAcknowledged();
      }
      else
      {
        getClassSelector().pushClassScope(new CClassInfoParent.Package(getClassSelector().getPackage(s.getTargetNamespace())));
        
        checkMultipleSchemaBindings(s);
        processPackageJavadoc(s);
        populate(s.getAttGroupDecls(), s);
        populate(s.getAttributeDecls(), s);
        populate(s.getElementDecls(), s);
        populate(s.getModelGroupDecls(), s);
        for (XSType t : s.getTypes().values())
        {
          stb.refererStack.push(t);
          this.model.typeUses().put(getName(t), cs.bindToType(t, s));
          stb.refererStack.pop();
        }
        getClassSelector().popClassScope();
      }
    }
  }
  
  private void checkMultipleSchemaBindings(XSSchema schema)
  {
    ArrayList<Locator> locations = new ArrayList();
    
    BindInfo bi = getBindInfo(schema);
    for (BIDeclaration bid : bi) {
      if (bid.getName() == BISchemaBinding.NAME) {
        locations.add(bid.getLocation());
      }
    }
    if (locations.size() <= 1) {
      return;
    }
    getErrorReporter().error((Locator)locations.get(0), "BGMBuilder.MultipleSchemaBindings", new Object[] { schema.getTargetNamespace() });
    for (int i = 1; i < locations.size(); i++) {
      getErrorReporter().error((Locator)locations.get(i), "BGMBuilder.MultipleSchemaBindings.Location", new Object[0]);
    }
  }
  
  private void populate(Map<String, ? extends XSComponent> col, XSSchema schema)
  {
    ClassSelector cs = getClassSelector();
    for (XSComponent sc : col.values()) {
      cs.bindToType(sc, schema);
    }
  }
  
  private void processPackageJavadoc(XSSchema s)
  {
    BISchemaBinding cust = (BISchemaBinding)getBindInfo(s).get(BISchemaBinding.class);
    if (cust == null) {
      return;
    }
    cust.markAsAcknowledged();
    if (cust.getJavadoc() == null) {
      return;
    }
    JTextFile html = new JTextFile("package.html");
    html.setContents(cust.getJavadoc());
    getClassSelector().getPackage(s.getTargetNamespace()).addResourceFile(html);
  }
  
  public BindInfo getOrCreateBindInfo(XSComponent schemaComponent)
  {
    BindInfo bi = _getBindInfoReadOnly(schemaComponent);
    if (bi != null) {
      return bi;
    }
    bi = new BindInfo();
    bi.setOwner(this, schemaComponent);
    this.externalBindInfos.put(schemaComponent, bi);
    return bi;
  }
  
  private final BindInfo emptyBindInfo = new BindInfo();
  
  public BindInfo getBindInfo(XSComponent schemaComponent)
  {
    BindInfo bi = _getBindInfoReadOnly(schemaComponent);
    if (bi != null) {
      return bi;
    }
    return this.emptyBindInfo;
  }
  
  private BindInfo _getBindInfoReadOnly(XSComponent schemaComponent)
  {
    BindInfo bi = (BindInfo)this.externalBindInfos.get(schemaComponent);
    if (bi != null) {
      return bi;
    }
    XSAnnotation annon = schemaComponent.getAnnotation();
    if (annon != null)
    {
      bi = (BindInfo)annon.getAnnotation();
      if (bi != null)
      {
        if (bi.getOwner() == null) {
          bi.setOwner(this, schemaComponent);
        }
        return bi;
      }
    }
    return null;
  }
  
  private final Map<XSComponent, BindInfo> externalBindInfos = new HashMap();
  
  protected final BIDom getLocalDomCustomization(XSParticle p)
  {
    BIDom dom = (BIDom)getBindInfo(p).get(BIDom.class);
    if (dom != null) {
      return dom;
    }
    dom = (BIDom)getBindInfo(p.getTerm()).get(BIDom.class);
    if (dom != null) {
      return dom;
    }
    XSTerm t = p.getTerm();
    if (t.isElementDecl()) {
      return (BIDom)getBindInfo(t.asElementDecl().getType()).get(BIDom.class);
    }
    if (t.isModelGroupDecl()) {
      return (BIDom)getBindInfo(t.asModelGroupDecl().getModelGroup()).get(BIDom.class);
    }
    return null;
  }
  
  private final XSFinder toPurple = new XSFinder()
  {
    public Boolean attributeUse(XSAttributeUse use)
    {
      return Boolean.valueOf(true);
    }
    
    public Boolean simpleType(XSSimpleType xsSimpleType)
    {
      return Boolean.valueOf(true);
    }
    
    public Boolean wildcard(XSWildcard xsWildcard)
    {
      return Boolean.valueOf(true);
    }
  };
  private Transformer identityTransformer;
  
  public void ying(XSComponent sc, @Nullable XSComponent referer)
  {
    if ((((Boolean)sc.apply(this.toPurple)).booleanValue() == true) || (getClassSelector().bindToType(sc, referer) != null)) {
      sc.visit(this.purple);
    } else {
      sc.visit(this.green);
    }
  }
  
  public Transformer getIdentityTransformer()
  {
    try
    {
      if (this.identityTransformer == null) {
        this.identityTransformer = TransformerFactory.newInstance().newTransformer();
      }
      return this.identityTransformer;
    }
    catch (TransformerConfigurationException e)
    {
      throw new Error(e);
    }
  }
  
  public Set<XSComponent> getReferer(XSType c)
  {
    if (this.refFinder == null)
    {
      this.refFinder = new RefererFinder();
      this.refFinder.schemaSet((XSSchemaSet)Ring.get(XSSchemaSet.class));
    }
    return this.refFinder.getReferer(c);
  }
  
  public static QName getName(XSDeclaration decl)
  {
    String local = decl.getName();
    if (local == null) {
      return null;
    }
    return new QName(decl.getTargetNamespace(), local);
  }
  
  public String deriveName(String name, XSComponent comp)
  {
    XSSchema owner = comp.getOwnerSchema();
    
    name = getNameConverter().toClassName(name);
    if (owner != null)
    {
      BISchemaBinding sb = (BISchemaBinding)getBindInfo(owner).get(BISchemaBinding.class);
      if (sb != null) {
        name = sb.mangleClassName(name, comp);
      }
    }
    return name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\BGMBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */