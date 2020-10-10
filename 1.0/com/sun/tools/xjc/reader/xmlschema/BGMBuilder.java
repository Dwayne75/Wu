package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.fmt.JTextFile;
import com.sun.msv.datatype.xsd.QnameType;
import com.sun.msv.datatype.xsd.QnameValueType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.xmlschema.OccurrenceExp;
import com.sun.msv.util.StringPair;
import com.sun.tools.xjc.AbortException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageTracker;
import com.sun.tools.xjc.reader.annotator.AnnotatorController;
import com.sun.tools.xjc.reader.annotator.DatatypeSimplifier;
import com.sun.tools.xjc.reader.annotator.FieldCollisionChecker;
import com.sun.tools.xjc.reader.annotator.HierarchyAnnotator;
import com.sun.tools.xjc.reader.annotator.RelationNormalizer;
import com.sun.tools.xjc.reader.annotator.SymbolSpaceTypeAssigner;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXSerializable;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXSuperClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.reader.xmlschema.cs.ClassSelector;
import com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeFieldBuilder;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.xml.sax.Locator;

public class BGMBuilder
  implements AnnotatorController
{
  public final ClassSelector selector;
  public final TypeBuilder typeBuilder;
  public final FieldBuilder fieldBuilder;
  public final ParticleBinder particleBinder;
  public final ComplexTypeFieldBuilder complexTypeBuilder;
  
  public static AnnotatedGrammar build(XSSchemaSet schemas, JCodeModel codeModel, ErrorReceiver errorReceiver, String defPackage, boolean inExtensionMode)
  {
    ErrorReceiverFilter erFilter = new ErrorReceiverFilter(errorReceiver);
    try
    {
      AnnotatedGrammar grammar = new BGMBuilder(schemas, codeModel, erFilter, defPackage, inExtensionMode)._build(schemas);
      if (erFilter.hadError()) {
        return null;
      }
      return grammar;
    }
    catch (AbortException e) {}
    return null;
  }
  
  public final Set particlesWithGlobalElementSkip = new HashSet();
  public final boolean inExtensionMode;
  private BIGlobalBinding globalBinding;
  private NameConverter nameConverter;
  public final XSSchemaSet schemas;
  
  private AnnotatedGrammar _build(XSSchemaSet schemas)
  {
    buildContents();
    buildTopLevelExp();
    this.selector.executeTasks();
    
    reportUnusedCustomizations();
    if (!this.errorReporter.hadError())
    {
      this.grammar.visit(new DatatypeSimplifier(this.grammar.getPool()));
      HierarchyAnnotator.annotate(this.grammar, this);
      SymbolSpaceTypeAssigner.assign(this.grammar, this);
      FieldCollisionChecker.check(this.grammar, this);
      RelationNormalizer.normalize(this.grammar, this);
    }
    return this.grammar;
  }
  
  private void promoteGlobalBindings()
  {
    for (Iterator itr = this.schemas.iterateSchema(); itr.hasNext();)
    {
      XSSchema s = (XSSchema)itr.next();
      BindInfo bi = getBindInfo(s);
      
      BIGlobalBinding gb = (BIGlobalBinding)bi.get(BIGlobalBinding.NAME);
      if ((gb != null) && (this.globalBinding == null))
      {
        this.globalBinding = gb;
        this.globalBinding.markAsAcknowledged();
      }
    }
    if (this.globalBinding == null)
    {
      this.globalBinding = new BIGlobalBinding(this.grammar.codeModel);
      BindInfo big = new BindInfo(null);
      big.addDecl(this.globalBinding);
      big.setOwner(this, null);
    }
    BIXSuperClass root = this.globalBinding.getSuperClassExtension();
    if (root != null) {
      this.grammar.rootClass = root.getRootClass();
    }
    BIXSerializable serial = this.globalBinding.getSerializableExtension();
    if (serial != null) {
      this.grammar.serialVersionUID = new Long(serial.getUID());
    }
    this.nameConverter = this.globalBinding.getNameConverter();
    
    this.globalBinding.dispatchGlobalConversions(this.schemas);
  }
  
  public BIGlobalBinding getGlobalBinding()
  {
    return this.globalBinding;
  }
  
  public NameConverter getNameConverter()
  {
    return this.nameConverter;
  }
  
  private void buildContents()
  {
    for (Iterator itr = this.schemas.iterateSchema(); itr.hasNext();)
    {
      XSSchema s = (XSSchema)itr.next();
      if (!s.getTargetNamespace().equals("http://www.w3.org/2001/XMLSchema"))
      {
        checkMultipleSchemaBindings(s);
        processPackageJavadoc(s);
        populate(s.iterateAttGroupDecls());
        populate(s.iterateAttributeDecls());
        populate(s.iterateComplexTypes());
        populate(s.iterateElementDecls());
        populate(s.iterateModelGroupDecls());
        populate(s.iterateSimpleTypes());
      }
    }
  }
  
  private void checkMultipleSchemaBindings(XSSchema schema)
  {
    ArrayList locations = new ArrayList();
    
    BindInfo bi = getBindInfo(schema);
    for (int i = 0; i < bi.size(); i++) {
      if (bi.get(i).getName() == BISchemaBinding.NAME) {
        locations.add(bi.get(i).getLocation());
      }
    }
    if (locations.size() <= 1) {
      return;
    }
    this.errorReporter.error((Locator)locations.get(0), "BGMBuilder.MultipleSchemaBindings", schema.getTargetNamespace());
    for (int i = 1; i < locations.size(); i++) {
      this.errorReporter.error((Locator)locations.get(i), "BGMBuilder.MultipleSchemaBindings.Location");
    }
  }
  
  private void populate(Iterator itr)
  {
    while (itr.hasNext())
    {
      XSComponent sc = (XSComponent)itr.next();
      this.selector.bindToType(sc);
    }
  }
  
  private void processPackageJavadoc(XSSchema s)
  {
    BISchemaBinding cust = (BISchemaBinding)getBindInfo(s).get(BISchemaBinding.NAME);
    if (cust == null) {
      return;
    }
    if (cust.getJavadoc() == null) {
      return;
    }
    JTextFile html = new JTextFile("package.html");
    html.setContents(cust.getJavadoc());
    this.selector.getPackage(s.getTargetNamespace()).addResourceFile(html);
  }
  
  private void buildTopLevelExp()
  {
    Expression top = Expression.nullSet;
    
    Iterator itr = this.schemas.iterateElementDecls();
    while (itr.hasNext())
    {
      XSElementDecl decl = (XSElementDecl)itr.next();
      
      TypeItem ti = this.selector.bindToType(decl);
      if ((ti instanceof ClassItem)) {
        top = this.pool.createChoice(top, ti);
      }
    }
    if (top == Expression.nullSet) {
      this.errorReporter.warning(null, "BGMBuilder.NoGlobalElement", null);
    }
    this.grammar.exp = top;
  }
  
  private void reportUnusedCustomizations()
  {
    new UnusedCustomizationChecker(this).run();
  }
  
  public BGMBuilder(XSSchemaSet _schemas, JCodeModel codeModel, ErrorReceiver _errorReceiver, String defaultPackage, boolean _inExtensionMode)
  {
    this.schemas = _schemas;
    this.inExtensionMode = _inExtensionMode;
    this.grammar = new AnnotatedGrammar(Expression.nullSet, this.pool, codeModel);
    this.errorReceiver = _errorReceiver;
    this.errorReporter = new ErrorReporter(_errorReceiver);
    this.simpleTypeBuilder = new SimpleTypeBuilder(this);
    
    this.typeBuilder = new TypeBuilder(this);
    this.fieldBuilder = new FieldBuilder(this);
    this.complexTypeBuilder = new ComplexTypeFieldBuilder(this);
    
    promoteGlobalBindings();
    
    this.selector = new ClassSelector(this, defaultPackage);
    if (getGlobalBinding().isModelGroupBinding()) {
      this.particleBinder = new AlternativeParticleBinder(this);
    } else {
      this.particleBinder = new DefaultParticleBinder(this);
    }
  }
  
  public BindInfo getOrCreateBindInfo(XSComponent schemaComponent)
  {
    BindInfo bi = _getBindInfoReadOnly(schemaComponent);
    if (bi != null) {
      return bi;
    }
    bi = new BindInfo(null);
    bi.setOwner(this, schemaComponent);
    this.externalBindInfos.put(schemaComponent, bi);
    return bi;
  }
  
  private final BindInfo emptyBindInfo = new BindInfo(null);
  
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
  
  private final Map externalBindInfos = new HashMap();
  public final ErrorReporter errorReporter;
  public final ErrorReceiver errorReceiver;
  public final ExpressionPool pool = new ExpressionPool();
  public final AnnotatedGrammar grammar;
  public final SimpleTypeBuilder simpleTypeBuilder;
  
  public Expression processMinMax(Expression item, XSParticle p)
  {
    return processMinMax(item, p.getMinOccurs(), p.getMaxOccurs());
  }
  
  public Expression processMinMax(Expression item, int min, int max)
  {
    Expression exp = Expression.epsilon;
    for (int i = 0; i < min; i++) {
      exp = this.pool.createSequence(item, exp);
    }
    if (max == -1)
    {
      if (min == 1) {
        return this.pool.createOneOrMore(item);
      }
      Expression exactExp = this.pool.createSequence(exp, this.pool.createZeroOrMore(item));
      if (min <= 1) {
        return exactExp;
      }
      return new OccurrenceExp(exactExp, max, min, item);
    }
    if (max == 0) {
      return Expression.nullSet;
    }
    Expression tmp = Expression.epsilon;
    for (int i = min; i < max; i++) {
      tmp = this.pool.createOptional(this.pool.createSequence(item, tmp));
    }
    Expression exactExp = this.pool.createSequence(exp, tmp);
    if (max == 1) {
      return exactExp;
    }
    return new OccurrenceExp(exactExp, max, min, item);
  }
  
  protected Expression applyRecursively(XSModelGroup mg, BGMBuilder.ParticleHandler f)
  {
    Expression[] exp = new Expression[mg.getSize()];
    for (int i = 0; i < exp.length; i++) {
      exp[i] = ((Expression)f.particle(mg.getChild(i)));
    }
    if (mg.getCompositor() == XSModelGroup.SEQUENCE)
    {
      Expression r = Expression.epsilon;
      for (int i = 0; i < exp.length; i++) {
        r = this.pool.createSequence(r, exp[i]);
      }
      return r;
    }
    if (mg.getCompositor() == XSModelGroup.ALL)
    {
      Expression r = Expression.epsilon;
      for (int i = 0; i < exp.length; i++) {
        r = this.pool.createInterleave(r, exp[i]);
      }
      return r;
    }
    if (mg.getCompositor() == XSModelGroup.CHOICE)
    {
      Expression r = Expression.nullSet;
      for (int i = 0; i < exp.length; i++) {
        r = this.pool.createChoice(r, exp[i]);
      }
      return r;
    }
    _assert(false);
    return null;
  }
  
  private final Expression xsiTypeExp = this.pool.createOptional(this.pool.createAttribute(new SimpleNameClass("http://www.w3.org/2001/XMLSchema-instance", "type"), this.pool.createData(QnameType.theInstance)));
  
  public Expression createXsiTypeExp(XSElementDecl decl)
  {
    return new IgnoreItem(this.xsiTypeExp, decl.getLocator());
  }
  
  private final Map substitutionGroupCache = new HashMap();
  
  public Expression getSubstitionGroupList(XSElementDecl e)
  {
    Expression exp = (Expression)this.substitutionGroupCache.get(e);
    if (exp == null)
    {
      Set group = e.getSubstitutables();
      exp = Expression.nullSet;
      for (Iterator itr = group.iterator(); itr.hasNext();)
      {
        XSElementDecl decl = (XSElementDecl)itr.next();
        if ((decl != e) && 
          (!decl.isAbstract())) {
          exp = this.pool.createChoice(exp, this.selector.bindToType(decl));
        }
      }
      this.substitutionGroupCache.put(e, exp);
    }
    return exp;
  }
  
  public final Expression getTypeSubstitutionList(XSComplexType ct, boolean strict)
  {
    if (!this.inExtensionMode) {
      return Expression.nullSet;
    }
    Expression exp = Expression.nullSet;
    
    XSType[] group = ct.listSubstitutables();
    for (int i = 0; i < group.length; i++) {
      if ((!strict) || (!group[i].asComplexType().isAbstract())) {
        exp = this.pool.createChoice(this.pool.createSequence(this.pool.createAttribute(new SimpleNameClass("http://www.w3.org/2001/XMLSchema-instance", "type"), new IgnoreItem(this.pool.createValue(QnameType.theInstance, new StringPair("http://www.w3.org/2001/XMLSchema", "qname"), new QnameValueType(group[i].getTargetNamespace(), group[i].getName())), null)), this.selector.bindToType(group[i])), exp);
      }
    }
    return exp;
  }
  
  private static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
  
  public PackageTracker getPackageTracker()
  {
    throw new JAXBAssertionError();
  }
  
  public void reportError(Expression[] locations, String msg)
  {
    reportError(new Locator[0], msg);
  }
  
  public void reportError(Locator[] locations, String msg)
  {
    Locator loc = null;
    if (locations.length != 0) {
      loc = locations[0];
    }
    this.errorReceiver.error(loc, msg);
  }
  
  public ErrorReceiver getErrorReceiver()
  {
    return this.errorReceiver;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\BGMBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */