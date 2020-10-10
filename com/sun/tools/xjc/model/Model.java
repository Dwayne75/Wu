package com.sun.tools.xjc.model;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.api.ClassNameAllocator;
import com.sun.tools.xjc.generator.bean.BeanGenerator;
import com.sun.tools.xjc.generator.bean.ImplStructureStrategy;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.model.nav.NavigatorImpl;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.xmlschema.Messages;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.util.FlattenIterator;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSSchemaSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import org.w3c.dom.Element;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

public final class Model
  implements TypeInfoSet<NType, NClass, Void, Void>, CCustomizable
{
  private final Map<NClass, CClassInfo> beans = new LinkedHashMap();
  private final Map<NClass, CEnumLeafInfo> enums = new LinkedHashMap();
  private final Map<NClass, Map<QName, CElementInfo>> elementMappings = new HashMap();
  private final Iterable<? extends CElementInfo> allElements = new Iterable()
  {
    public Iterator<CElementInfo> iterator()
    {
      return new FlattenIterator(Model.this.elementMappings.values());
    }
  };
  private final Map<QName, TypeUse> typeUses = new LinkedHashMap();
  private NameConverter nameConverter;
  CCustomizations customizations;
  private boolean packageLevelAnnotations = true;
  public final XSSchemaSet schemaComponent;
  private CCustomizations gloablCustomizations = new CCustomizations();
  @XmlTransient
  public final JCodeModel codeModel;
  public final Options options;
  @XmlAttribute
  public boolean serializable;
  @XmlAttribute
  public Long serialVersionUID;
  @XmlTransient
  public JClass rootClass;
  @XmlTransient
  public JClass rootInterface;
  
  public Model(Options opts, JCodeModel cm, NameConverter nc, ClassNameAllocator allocator, XSSchemaSet schemaComponent)
  {
    this.options = opts;
    this.codeModel = cm;
    this.nameConverter = nc;
    this.defaultSymbolSpace = new SymbolSpace(this.codeModel);
    this.defaultSymbolSpace.setType(this.codeModel.ref(Object.class));
    
    this.elementMappings.put(null, new HashMap());
    if (opts.automaticNameConflictResolution) {
      allocator = new AutoClassNameAllocator(allocator);
    }
    this.allocator = new ClassNameAllocatorWrapper(allocator);
    this.schemaComponent = schemaComponent;
    this.gloablCustomizations.setParent(this, this);
  }
  
  public void setNameConverter(NameConverter nameConverter)
  {
    assert (this.nameConverter == null);
    assert (nameConverter != null);
    this.nameConverter = nameConverter;
  }
  
  public final NameConverter getNameConverter()
  {
    return this.nameConverter;
  }
  
  public boolean isPackageLevelAnnotations()
  {
    return this.packageLevelAnnotations;
  }
  
  public void setPackageLevelAnnotations(boolean packageLevelAnnotations)
  {
    this.packageLevelAnnotations = packageLevelAnnotations;
  }
  
  public ImplStructureStrategy strategy = ImplStructureStrategy.BEAN_ONLY;
  final ClassNameAllocatorWrapper allocator;
  @XmlTransient
  public final SymbolSpace defaultSymbolSpace;
  private final Map<String, SymbolSpace> symbolSpaces = new HashMap();
  
  public SymbolSpace getSymbolSpace(String name)
  {
    SymbolSpace ss = (SymbolSpace)this.symbolSpaces.get(name);
    if (ss == null) {
      this.symbolSpaces.put(name, ss = new SymbolSpace(this.codeModel));
    }
    return ss;
  }
  
  public Outline generateCode(Options opt, ErrorReceiver receiver)
  {
    ErrorReceiverFilter ehf = new ErrorReceiverFilter(receiver);
    for (Plugin ma : opt.activePlugins) {
      ma.postProcessModel(this, ehf);
    }
    Outline o = BeanGenerator.generate(this, ehf);
    try
    {
      for (Plugin ma : opt.activePlugins) {
        ma.run(o, opt, ehf);
      }
    }
    catch (SAXException e)
    {
      return null;
    }
    Set<CCustomizations> check = new HashSet();
    for (CCustomizations c = this.customizations; c != null; c = c.next)
    {
      if (!check.add(c)) {
        throw new AssertionError();
      }
      for (CPluginCustomization p : c) {
        if (!p.isAcknowledged())
        {
          ehf.error(p.locator, Messages.format("UnusedCustomizationChecker.UnacknolwedgedCustomization", new Object[] { p.element.getNodeName() }));
          
          ehf.error(c.getOwner().getLocator(), Messages.format("UnusedCustomizationChecker.UnacknolwedgedCustomization.Relevant", new Object[0]));
        }
      }
    }
    if (ehf.hadError()) {
      o = null;
    }
    return o;
  }
  
  public final Map<QName, CClassInfo> createTopLevelBindings()
  {
    Map<QName, CClassInfo> r = new HashMap();
    for (CClassInfo b : beans().values()) {
      if (b.isElement()) {
        r.put(b.getElementName(), b);
      }
    }
    return r;
  }
  
  public Navigator<NType, NClass, Void, Void> getNavigator()
  {
    return NavigatorImpl.theInstance;
  }
  
  public CNonElement getTypeInfo(NType type)
  {
    CBuiltinLeafInfo leaf = (CBuiltinLeafInfo)CBuiltinLeafInfo.LEAVES.get(type);
    if (leaf != null) {
      return leaf;
    }
    return getClassInfo((NClass)getNavigator().asDecl(type));
  }
  
  public CBuiltinLeafInfo getAnyTypeInfo()
  {
    return CBuiltinLeafInfo.ANYTYPE;
  }
  
  public CNonElement getTypeInfo(Ref<NType, NClass> ref)
  {
    assert (!ref.valueList);
    return getTypeInfo((NType)ref.type);
  }
  
  public Map<NClass, CClassInfo> beans()
  {
    return this.beans;
  }
  
  public Map<NClass, CEnumLeafInfo> enums()
  {
    return this.enums;
  }
  
  public Map<QName, TypeUse> typeUses()
  {
    return this.typeUses;
  }
  
  public Map<NType, ? extends CArrayInfo> arrays()
  {
    return Collections.emptyMap();
  }
  
  public Map<NType, ? extends CBuiltinLeafInfo> builtins()
  {
    return CBuiltinLeafInfo.LEAVES;
  }
  
  public CClassInfo getClassInfo(NClass t)
  {
    return (CClassInfo)this.beans.get(t);
  }
  
  public CElementInfo getElementInfo(NClass scope, QName name)
  {
    Map<QName, CElementInfo> m = (Map)this.elementMappings.get(scope);
    if (m != null)
    {
      CElementInfo r = (CElementInfo)m.get(name);
      if (r != null) {
        return r;
      }
    }
    return (CElementInfo)((Map)this.elementMappings.get(null)).get(name);
  }
  
  public Map<QName, CElementInfo> getElementMappings(NClass scope)
  {
    return (Map)this.elementMappings.get(scope);
  }
  
  public Iterable<? extends CElementInfo> getAllElements()
  {
    return this.allElements;
  }
  
  /**
   * @deprecated
   */
  public XSComponent getSchemaComponent()
  {
    return null;
  }
  
  /**
   * @deprecated
   */
  public Locator getLocator()
  {
    LocatorImpl r = new LocatorImpl();
    r.setLineNumber(-1);
    r.setColumnNumber(-1);
    return r;
  }
  
  public CCustomizations getCustomizations()
  {
    return this.gloablCustomizations;
  }
  
  public Map<String, String> getXmlNs(String namespaceUri)
  {
    return Collections.emptyMap();
  }
  
  public Map<String, String> getSchemaLocations()
  {
    return Collections.emptyMap();
  }
  
  public XmlNsForm getElementFormDefault(String nsUri)
  {
    throw new UnsupportedOperationException();
  }
  
  public XmlNsForm getAttributeFormDefault(String nsUri)
  {
    throw new UnsupportedOperationException();
  }
  
  public void dump(Result out)
  {
    throw new UnsupportedOperationException();
  }
  
  void add(CEnumLeafInfo e)
  {
    this.enums.put(e.getClazz(), e);
  }
  
  void add(CClassInfo ci)
  {
    this.beans.put(ci.getClazz(), ci);
  }
  
  void add(CElementInfo ei)
  {
    NClass clazz = null;
    if (ei.getScope() != null) {
      clazz = ei.getScope().getClazz();
    }
    Map<QName, CElementInfo> m = (Map)this.elementMappings.get(clazz);
    if (m == null) {
      this.elementMappings.put(clazz, m = new HashMap());
    }
    m.put(ei.getElementName(), ei);
  }
  
  private final Map<JPackage, CClassInfoParent.Package> cache = new HashMap();
  static final Locator EMPTY_LOCATOR;
  
  public CClassInfoParent.Package getPackage(JPackage pkg)
  {
    CClassInfoParent.Package r = (CClassInfoParent.Package)this.cache.get(pkg);
    if (r == null) {
      this.cache.put(pkg, r = new CClassInfoParent.Package(pkg));
    }
    return r;
  }
  
  static
  {
    LocatorImpl l = new LocatorImpl();
    l.setColumnNumber(-1);
    l.setLineNumber(-1);
    EMPTY_LOCATOR = l;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\Model.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */