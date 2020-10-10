package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.istack.Pool;
import com.sun.istack.Pool.Impl;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.api.ErrorListener;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.RawAccessor;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.bind.util.Which;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeEnumLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;
import com.sun.xml.bind.v2.runtime.output.Encoded;
import com.sun.xml.bind.v2.runtime.property.AttributeProperty;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext.State;
import com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator;
import com.sun.xml.bind.v2.util.EditDistance;
import com.sun.xml.bind.v2.util.QNameMap;
import com.sun.xml.bind.v2.util.QNameMap.Entry;
import com.sun.xml.txw2.output.ResultFactory;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.Binder;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public final class JAXBContextImpl
  extends JAXBRIContext
{
  private final Map<TypeReference, Bridge> bridges = new LinkedHashMap();
  private static SAXTransformerFactory tf;
  private static DocumentBuilder db;
  private final QNameMap<JaxBeanInfo> rootMap = new QNameMap();
  private final HashMap<QName, JaxBeanInfo> typeMap = new HashMap();
  private final Map<Class, JaxBeanInfo> beanInfoMap = new LinkedHashMap();
  protected Map<RuntimeTypeInfo, JaxBeanInfo> beanInfos = new LinkedHashMap();
  private final Map<Class, Map<QName, ElementBeanInfoImpl>> elements = new LinkedHashMap();
  public final Pool<Marshaller> marshallerPool = new Pool.Impl()
  {
    @NotNull
    protected Marshaller create()
    {
      return JAXBContextImpl.this.createMarshaller();
    }
  };
  public final Pool<Unmarshaller> unmarshallerPool = new Pool.Impl()
  {
    @NotNull
    protected Unmarshaller create()
    {
      return JAXBContextImpl.this.createUnmarshaller();
    }
  };
  public NameBuilder nameBuilder = new NameBuilder();
  public final NameList nameList;
  private final String defaultNsUri;
  private final Class[] classes;
  protected final boolean c14nSupport;
  public final boolean xmlAccessorFactorySupport;
  public final boolean allNillable;
  private WeakReference<RuntimeTypeInfoSet> typeInfoSetCache;
  @NotNull
  private RuntimeAnnotationReader annotaitonReader;
  private boolean hasSwaRef;
  @NotNull
  private final Map<Class, Class> subclassReplacements;
  public final boolean fastBoot;
  private Encoded[] utf8nameTable;
  
  public JAXBContextImpl(Class[] classes, Collection<TypeReference> typeRefs, Map<Class, Class> subclassReplacements, String defaultNsUri, boolean c14nSupport, @Nullable RuntimeAnnotationReader ar, boolean xmlAccessorFactorySupport, boolean allNillable)
    throws JAXBException
  {
    DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
    if (defaultNsUri == null) {
      defaultNsUri = "";
    }
    if (ar == null) {
      ar = new RuntimeInlineAnnotationReader();
    }
    if (subclassReplacements == null) {
      subclassReplacements = Collections.emptyMap();
    }
    if (typeRefs == null) {
      typeRefs = Collections.emptyList();
    }
    this.annotaitonReader = ar;
    this.subclassReplacements = subclassReplacements;
    boolean fastBoot;
    try
    {
      fastBoot = Boolean.getBoolean(JAXBContextImpl.class.getName() + ".fastBoot");
    }
    catch (SecurityException e)
    {
      fastBoot = false;
    }
    this.fastBoot = fastBoot;
    
    this.defaultNsUri = defaultNsUri;
    this.c14nSupport = c14nSupport;
    this.xmlAccessorFactorySupport = xmlAccessorFactorySupport;
    this.allNillable = allNillable;
    this.classes = new Class[classes.length];
    System.arraycopy(classes, 0, this.classes, 0, classes.length);
    
    RuntimeTypeInfoSet typeSet = getTypeInfoSet();
    
    this.elements.put(null, new LinkedHashMap());
    for (RuntimeBuiltinLeafInfo leaf : RuntimeBuiltinLeafInfoImpl.builtinBeanInfos)
    {
      bi = new LeafBeanInfoImpl(this, leaf);
      this.beanInfoMap.put(leaf.getClazz(), bi);
      for (QName t : bi.getTypeNames()) {
        this.typeMap.put(t, bi);
      }
    }
    LeafBeanInfoImpl<?> bi;
    for (RuntimeEnumLeafInfo e : typeSet.enums().values())
    {
      JaxBeanInfo<?> bi = getOrCreate(e);
      for (QName qn : bi.getTypeNames()) {
        this.typeMap.put(qn, bi);
      }
      if (e.isElement()) {
        this.rootMap.put(e.getElementName(), bi);
      }
    }
    for (RuntimeArrayInfo a : typeSet.arrays().values())
    {
      ai = getOrCreate(a);
      for (QName qn : ai.getTypeNames()) {
        this.typeMap.put(qn, ai);
      }
    }
    JaxBeanInfo<?> ai;
    for (RuntimeClassInfo ci : typeSet.beans().values())
    {
      bi = getOrCreate(ci);
      if (bi.isElement()) {
        this.rootMap.put(ci.getElementName(), bi);
      }
      for (QName qn : bi.getTypeNames()) {
        this.typeMap.put(qn, bi);
      }
    }
    ClassBeanInfoImpl<?> bi;
    for (RuntimeElementInfo n : typeSet.getAllElements())
    {
      ElementBeanInfoImpl bi = getOrCreate(n);
      if (n.getScope() == null) {
        this.rootMap.put(n.getElementName(), bi);
      }
      RuntimeClassInfo scope = n.getScope();
      Class scopeClazz = scope == null ? null : (Class)scope.getClazz();
      Map<QName, ElementBeanInfoImpl> m = (Map)this.elements.get(scopeClazz);
      if (m == null)
      {
        m = new LinkedHashMap();
        this.elements.put(scopeClazz, m);
      }
      m.put(n.getElementName(), bi);
    }
    this.beanInfoMap.put(JAXBElement.class, new ElementBeanInfoImpl(this));
    
    this.beanInfoMap.put(CompositeStructure.class, new CompositeStructureBeanInfo(this));
    
    getOrCreate(typeSet.getAnyTypeInfo());
    for (JaxBeanInfo bi : this.beanInfos.values()) {
      bi.link(this);
    }
    for (Map.Entry<Class, Class> e : RuntimeUtil.primitiveToBox.entrySet()) {
      this.beanInfoMap.put(e.getKey(), this.beanInfoMap.get(e.getValue()));
    }
    ReflectionNavigator nav = typeSet.getNavigator();
    for (TypeReference tr : typeRefs)
    {
      XmlJavaTypeAdapter xjta = (XmlJavaTypeAdapter)tr.get(XmlJavaTypeAdapter.class);
      Adapter<Type, Class> a = null;
      XmlList xl = (XmlList)tr.get(XmlList.class);
      
      Class erasedType = nav.erasure(tr.type);
      if (xjta != null) {
        a = new Adapter(xjta.value(), nav);
      }
      if (tr.get(XmlAttachmentRef.class) != null)
      {
        a = new Adapter(SwaRefAdapter.class, nav);
        this.hasSwaRef = true;
      }
      if (a != null) {
        erasedType = nav.erasure((Type)a.defaultType);
      }
      Name name = this.nameBuilder.createElementName(tr.tagName);
      InternalBridge bridge;
      InternalBridge bridge;
      if (xl == null) {
        bridge = new BridgeImpl(this, name, getBeanInfo(erasedType, true), tr);
      } else {
        bridge = new BridgeImpl(this, name, new ValueListBeanInfoImpl(this, erasedType), tr);
      }
      if (a != null) {
        bridge = new BridgeAdapter(bridge, (Class)a.adapterType);
      }
      this.bridges.put(tr, bridge);
    }
    this.nameList = this.nameBuilder.conclude();
    for (JaxBeanInfo bi : this.beanInfos.values()) {
      bi.wrapUp();
    }
    this.nameBuilder = null;
    this.beanInfos = null;
  }
  
  public boolean hasSwaRef()
  {
    return this.hasSwaRef;
  }
  
  private RuntimeTypeInfoSet getTypeInfoSet()
    throws IllegalAnnotationsException
  {
    if (this.typeInfoSetCache != null)
    {
      RuntimeTypeInfoSet r = (RuntimeTypeInfoSet)this.typeInfoSetCache.get();
      if (r != null) {
        return r;
      }
    }
    RuntimeModelBuilder builder = new RuntimeModelBuilder(this, this.annotaitonReader, this.subclassReplacements, this.defaultNsUri);
    
    IllegalAnnotationsException.Builder errorHandler = new IllegalAnnotationsException.Builder();
    builder.setErrorHandler(errorHandler);
    for (Class c : this.classes) {
      if (c != CompositeStructure.class) {
        builder.getTypeInfo(new Ref(c));
      }
    }
    this.hasSwaRef |= builder.hasSwaRef;
    RuntimeTypeInfoSet r = builder.link();
    
    errorHandler.check();
    assert (r != null) : "if no error was reported, the link must be a success";
    
    this.typeInfoSetCache = new WeakReference(r);
    
    return r;
  }
  
  public ElementBeanInfoImpl getElement(Class scope, QName name)
  {
    Map<QName, ElementBeanInfoImpl> m = (Map)this.elements.get(scope);
    if (m != null)
    {
      ElementBeanInfoImpl bi = (ElementBeanInfoImpl)m.get(name);
      if (bi != null) {
        return bi;
      }
    }
    m = (Map)this.elements.get(null);
    return (ElementBeanInfoImpl)m.get(name);
  }
  
  private ElementBeanInfoImpl getOrCreate(RuntimeElementInfo rei)
  {
    JaxBeanInfo bi = (JaxBeanInfo)this.beanInfos.get(rei);
    if (bi != null) {
      return (ElementBeanInfoImpl)bi;
    }
    return new ElementBeanInfoImpl(this, rei);
  }
  
  protected JaxBeanInfo getOrCreate(RuntimeEnumLeafInfo eli)
  {
    JaxBeanInfo bi = (JaxBeanInfo)this.beanInfos.get(eli);
    if (bi != null) {
      return bi;
    }
    bi = new LeafBeanInfoImpl(this, eli);
    this.beanInfoMap.put(bi.jaxbType, bi);
    return bi;
  }
  
  protected ClassBeanInfoImpl getOrCreate(RuntimeClassInfo ci)
  {
    ClassBeanInfoImpl bi = (ClassBeanInfoImpl)this.beanInfos.get(ci);
    if (bi != null) {
      return bi;
    }
    bi = new ClassBeanInfoImpl(this, ci);
    this.beanInfoMap.put(bi.jaxbType, bi);
    return bi;
  }
  
  protected JaxBeanInfo getOrCreate(RuntimeArrayInfo ai)
  {
    JaxBeanInfo abi = (JaxBeanInfo)this.beanInfos.get(ai);
    if (abi != null) {
      return abi;
    }
    abi = new ArrayBeanInfoImpl(this, ai);
    
    this.beanInfoMap.put(ai.getType(), abi);
    return abi;
  }
  
  public JaxBeanInfo getOrCreate(RuntimeTypeInfo e)
  {
    if ((e instanceof RuntimeElementInfo)) {
      return getOrCreate((RuntimeElementInfo)e);
    }
    if ((e instanceof RuntimeClassInfo)) {
      return getOrCreate((RuntimeClassInfo)e);
    }
    if ((e instanceof RuntimeLeafInfo))
    {
      JaxBeanInfo bi = (JaxBeanInfo)this.beanInfos.get(e);
      assert (bi != null);
      return bi;
    }
    if ((e instanceof RuntimeArrayInfo)) {
      return getOrCreate((RuntimeArrayInfo)e);
    }
    if (e.getType() == Object.class)
    {
      JaxBeanInfo bi = (JaxBeanInfo)this.beanInfoMap.get(Object.class);
      if (bi == null)
      {
        bi = new AnyTypeBeanInfo(this, e);
        this.beanInfoMap.put(Object.class, bi);
      }
      return bi;
    }
    throw new IllegalArgumentException();
  }
  
  public final JaxBeanInfo getBeanInfo(Object o)
  {
    for (Class c = o.getClass(); c != Object.class; c = c.getSuperclass())
    {
      JaxBeanInfo bi = (JaxBeanInfo)this.beanInfoMap.get(c);
      if (bi != null) {
        return bi;
      }
    }
    if ((o instanceof org.w3c.dom.Element)) {
      return (JaxBeanInfo)this.beanInfoMap.get(Object.class);
    }
    return null;
  }
  
  public final JaxBeanInfo getBeanInfo(Object o, boolean fatal)
    throws JAXBException
  {
    JaxBeanInfo bi = getBeanInfo(o);
    if (bi != null) {
      return bi;
    }
    if (fatal)
    {
      if ((o instanceof Document)) {
        throw new JAXBException(Messages.ELEMENT_NEEDED_BUT_FOUND_DOCUMENT.format(new Object[] { o.getClass() }));
      }
      throw new JAXBException(Messages.UNKNOWN_CLASS.format(new Object[] { o.getClass() }));
    }
    return null;
  }
  
  public final <T> JaxBeanInfo<T> getBeanInfo(Class<T> clazz)
  {
    return (JaxBeanInfo)this.beanInfoMap.get(clazz);
  }
  
  public final <T> JaxBeanInfo<T> getBeanInfo(Class<T> clazz, boolean fatal)
    throws JAXBException
  {
    JaxBeanInfo<T> bi = getBeanInfo(clazz);
    if (bi != null) {
      return bi;
    }
    if (fatal) {
      throw new JAXBException(clazz.getName() + " is not known to this context");
    }
    return null;
  }
  
  public final Loader selectRootLoader(UnmarshallingContext.State state, TagName tag)
  {
    JaxBeanInfo beanInfo = (JaxBeanInfo)this.rootMap.get(tag.uri, tag.local);
    if (beanInfo == null) {
      return null;
    }
    return beanInfo.getLoader(this, true);
  }
  
  public JaxBeanInfo getGlobalType(QName name)
  {
    return (JaxBeanInfo)this.typeMap.get(name);
  }
  
  public String getNearestTypeName(QName name)
  {
    String[] all = new String[this.typeMap.size()];
    int i = 0;
    for (QName qn : this.typeMap.keySet())
    {
      if (qn.getLocalPart().equals(name.getLocalPart())) {
        return qn.toString();
      }
      all[(i++)] = qn.toString();
    }
    String nearest = EditDistance.findNearest(name.toString(), all);
    if (EditDistance.editDistance(nearest, name.toString()) > 10) {
      return null;
    }
    return nearest;
  }
  
  public Set<QName> getValidRootNames()
  {
    Set<QName> r = new TreeSet(QNAME_COMPARATOR);
    for (QNameMap.Entry e : this.rootMap.entrySet()) {
      r.add(e.createQName());
    }
    return r;
  }
  
  public synchronized Encoded[] getUTF8NameTable()
  {
    if (this.utf8nameTable == null)
    {
      Encoded[] x = new Encoded[this.nameList.localNames.length];
      for (int i = 0; i < x.length; i++)
      {
        Encoded e = new Encoded(this.nameList.localNames[i]);
        e.compact();
        x[i] = e;
      }
      this.utf8nameTable = x;
    }
    return this.utf8nameTable;
  }
  
  public int getNumberOfLocalNames()
  {
    return this.nameList.localNames.length;
  }
  
  public int getNumberOfElementNames()
  {
    return this.nameList.numberOfElementNames;
  }
  
  public int getNumberOfAttributeNames()
  {
    return this.nameList.numberOfAttributeNames;
  }
  
  /* Error */
  static javax.xml.transform.Transformer createTransformer()
  {
    // Byte code:
    //   0: ldc_w 34
    //   3: dup
    //   4: astore_0
    //   5: monitorenter
    //   6: getstatic 208	com/sun/xml/bind/v2/runtime/JAXBContextImpl:tf	Ljavax/xml/transform/sax/SAXTransformerFactory;
    //   9: ifnonnull +12 -> 21
    //   12: invokestatic 209	javax/xml/transform/TransformerFactory:newInstance	()Ljavax/xml/transform/TransformerFactory;
    //   15: checkcast 210	javax/xml/transform/sax/SAXTransformerFactory
    //   18: putstatic 208	com/sun/xml/bind/v2/runtime/JAXBContextImpl:tf	Ljavax/xml/transform/sax/SAXTransformerFactory;
    //   21: getstatic 208	com/sun/xml/bind/v2/runtime/JAXBContextImpl:tf	Ljavax/xml/transform/sax/SAXTransformerFactory;
    //   24: invokevirtual 211	javax/xml/transform/sax/SAXTransformerFactory:newTransformer	()Ljavax/xml/transform/Transformer;
    //   27: aload_0
    //   28: monitorexit
    //   29: areturn
    //   30: astore_1
    //   31: aload_0
    //   32: monitorexit
    //   33: aload_1
    //   34: athrow
    //   35: astore_0
    //   36: new 213	java/lang/Error
    //   39: dup
    //   40: aload_0
    //   41: invokespecial 214	java/lang/Error:<init>	(Ljava/lang/Throwable;)V
    //   44: athrow
    // Line number table:
    //   Java source line #689	-> byte code offset #0
    //   Java source line #690	-> byte code offset #6
    //   Java source line #691	-> byte code offset #12
    //   Java source line #692	-> byte code offset #21
    //   Java source line #693	-> byte code offset #30
    //   Java source line #694	-> byte code offset #35
    //   Java source line #695	-> byte code offset #36
    // Local variable table:
    //   start	length	slot	name	signature
    //   35	6	0	e	javax.xml.transform.TransformerConfigurationException
    //   30	4	1	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   6	29	30	finally
    //   30	33	30	finally
    //   0	29	35	javax/xml/transform/TransformerConfigurationException
    //   30	35	35	javax/xml/transform/TransformerConfigurationException
  }
  
  /* Error */
  public static javax.xml.transform.sax.TransformerHandler createTransformerHandler()
  {
    // Byte code:
    //   0: ldc_w 34
    //   3: dup
    //   4: astore_0
    //   5: monitorenter
    //   6: getstatic 208	com/sun/xml/bind/v2/runtime/JAXBContextImpl:tf	Ljavax/xml/transform/sax/SAXTransformerFactory;
    //   9: ifnonnull +12 -> 21
    //   12: invokestatic 209	javax/xml/transform/TransformerFactory:newInstance	()Ljavax/xml/transform/TransformerFactory;
    //   15: checkcast 210	javax/xml/transform/sax/SAXTransformerFactory
    //   18: putstatic 208	com/sun/xml/bind/v2/runtime/JAXBContextImpl:tf	Ljavax/xml/transform/sax/SAXTransformerFactory;
    //   21: getstatic 208	com/sun/xml/bind/v2/runtime/JAXBContextImpl:tf	Ljavax/xml/transform/sax/SAXTransformerFactory;
    //   24: invokevirtual 215	javax/xml/transform/sax/SAXTransformerFactory:newTransformerHandler	()Ljavax/xml/transform/sax/TransformerHandler;
    //   27: aload_0
    //   28: monitorexit
    //   29: areturn
    //   30: astore_1
    //   31: aload_0
    //   32: monitorexit
    //   33: aload_1
    //   34: athrow
    //   35: astore_0
    //   36: new 213	java/lang/Error
    //   39: dup
    //   40: aload_0
    //   41: invokespecial 214	java/lang/Error:<init>	(Ljava/lang/Throwable;)V
    //   44: athrow
    // Line number table:
    //   Java source line #704	-> byte code offset #0
    //   Java source line #705	-> byte code offset #6
    //   Java source line #706	-> byte code offset #12
    //   Java source line #707	-> byte code offset #21
    //   Java source line #708	-> byte code offset #30
    //   Java source line #709	-> byte code offset #35
    //   Java source line #710	-> byte code offset #36
    // Local variable table:
    //   start	length	slot	name	signature
    //   35	6	0	e	javax.xml.transform.TransformerConfigurationException
    //   30	4	1	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   6	29	30	finally
    //   30	33	30	finally
    //   0	29	35	javax/xml/transform/TransformerConfigurationException
    //   30	35	35	javax/xml/transform/TransformerConfigurationException
  }
  
  static Document createDom()
  {
    synchronized (JAXBContextImpl.class)
    {
      if (db == null) {
        try
        {
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          dbf.setNamespaceAware(true);
          db = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
          throw new FactoryConfigurationError(e);
        }
      }
      return db.newDocument();
    }
  }
  
  public MarshallerImpl createMarshaller()
  {
    return new MarshallerImpl(this, null);
  }
  
  public UnmarshallerImpl createUnmarshaller()
  {
    return new UnmarshallerImpl(this, null);
  }
  
  public Validator createValidator()
  {
    throw new UnsupportedOperationException(Messages.NOT_IMPLEMENTED_IN_2_0.format(new Object[0]));
  }
  
  public JAXBIntrospector createJAXBIntrospector()
  {
    new JAXBIntrospector()
    {
      public boolean isElement(Object object)
      {
        return getElementName(object) != null;
      }
      
      public QName getElementName(Object jaxbElement)
      {
        try
        {
          return JAXBContextImpl.this.getElementName(jaxbElement);
        }
        catch (JAXBException e) {}
        return null;
      }
    };
  }
  
  private NonElement<Type, Class> getXmlType(RuntimeTypeInfoSet tis, TypeReference tr)
  {
    if (tr == null) {
      throw new IllegalArgumentException();
    }
    XmlJavaTypeAdapter xjta = (XmlJavaTypeAdapter)tr.get(XmlJavaTypeAdapter.class);
    XmlList xl = (XmlList)tr.get(XmlList.class);
    
    Ref<Type, Class> ref = new Ref(this.annotaitonReader, tis.getNavigator(), tr.type, xjta, xl);
    
    return tis.getTypeInfo(ref);
  }
  
  public void generateEpisode(Result output)
  {
    if (output == null) {
      throw new IllegalArgumentException();
    }
    createSchemaGenerator().writeEpisodeFile(ResultFactory.createSerializer(output));
  }
  
  public void generateSchema(SchemaOutputResolver outputResolver)
    throws IOException
  {
    if (outputResolver == null) {
      throw new IOException(Messages.NULL_OUTPUT_RESOLVER.format(new Object[0]));
    }
    final SAXParseException[] e = new SAXParseException[1];
    
    createSchemaGenerator().write(outputResolver, new ErrorListener()
    {
      public void error(SAXParseException exception)
      {
        e[0] = exception;
      }
      
      public void fatalError(SAXParseException exception)
      {
        e[0] = exception;
      }
      
      public void warning(SAXParseException exception) {}
      
      public void info(SAXParseException exception) {}
    });
    if (e[0] != null)
    {
      IOException x = new IOException(Messages.FAILED_TO_GENERATE_SCHEMA.format(new Object[0]));
      x.initCause(e[0]);
      throw x;
    }
  }
  
  private XmlSchemaGenerator<Type, Class, Field, Method> createSchemaGenerator()
  {
    RuntimeTypeInfoSet tis;
    try
    {
      tis = getTypeInfoSet();
    }
    catch (IllegalAnnotationsException e)
    {
      throw new AssertionError(e);
    }
    XmlSchemaGenerator<Type, Class, Field, Method> xsdgen = new XmlSchemaGenerator(tis.getNavigator(), tis);
    
    Set<QName> rootTagNames = new HashSet();
    for (RuntimeElementInfo ei : tis.getAllElements()) {
      rootTagNames.add(ei.getElementName());
    }
    for (RuntimeClassInfo ci : tis.beans().values()) {
      if (ci.isElement()) {
        rootTagNames.add(ci.asElement().getElementName());
      }
    }
    for (TypeReference tr : this.bridges.keySet()) {
      if (!rootTagNames.contains(tr.tagName)) {
        if ((tr.type == Void.TYPE) || (tr.type == Void.class))
        {
          xsdgen.add(tr.tagName, false, null);
        }
        else if (tr.type != CompositeStructure.class)
        {
          NonElement<Type, Class> typeInfo = getXmlType(tis, tr);
          xsdgen.add(tr.tagName, !Navigator.REFLECTION.isPrimitive(tr.type), typeInfo);
        }
      }
    }
    return xsdgen;
  }
  
  public QName getTypeName(TypeReference tr)
  {
    try
    {
      NonElement<Type, Class> xt = getXmlType(getTypeInfoSet(), tr);
      if (xt == null) {
        throw new IllegalArgumentException();
      }
      return xt.getTypeName();
    }
    catch (IllegalAnnotationsException e)
    {
      throw new AssertionError(e);
    }
  }
  
  public SchemaOutputResolver createTestResolver()
  {
    new SchemaOutputResolver()
    {
      public Result createOutput(String namespaceUri, String suggestedFileName)
      {
        SAXResult r = new SAXResult(new DefaultHandler());
        r.setSystemId(suggestedFileName);
        return r;
      }
    };
  }
  
  public <T> Binder<T> createBinder(Class<T> domType)
  {
    if (domType == Node.class) {
      return createBinder();
    }
    return super.createBinder(domType);
  }
  
  public Binder<Node> createBinder()
  {
    return new BinderImpl(this, new DOMScanner());
  }
  
  public QName getElementName(Object o)
    throws JAXBException
  {
    JaxBeanInfo bi = getBeanInfo(o, true);
    if (!bi.isElement()) {
      return null;
    }
    return new QName(bi.getElementNamespaceURI(o), bi.getElementLocalName(o));
  }
  
  public Bridge createBridge(TypeReference ref)
  {
    return (Bridge)this.bridges.get(ref);
  }
  
  @NotNull
  public BridgeContext createBridgeContext()
  {
    return new BridgeContextImpl(this);
  }
  
  public RawAccessor getElementPropertyAccessor(Class wrapperBean, String nsUri, String localName)
    throws JAXBException
  {
    JaxBeanInfo bi = getBeanInfo(wrapperBean, true);
    if (!(bi instanceof ClassBeanInfoImpl)) {
      throw new JAXBException(wrapperBean + " is not a bean");
    }
    for (ClassBeanInfoImpl cb = (ClassBeanInfoImpl)bi; cb != null; cb = cb.superClazz) {
      for (Property p : cb.properties)
      {
        final Accessor acc = p.getElementPropertyAccessor(nsUri, localName);
        if (acc != null) {
          new RawAccessor()
          {
            public Object get(Object bean)
              throws AccessorException
            {
              return acc.getUnadapted(bean);
            }
            
            public void set(Object bean, Object value)
              throws AccessorException
            {
              acc.setUnadapted(bean, value);
            }
          };
        }
      }
    }
    throw new JAXBException(new QName(nsUri, localName) + " is not a valid property on " + wrapperBean);
  }
  
  public List<String> getKnownNamespaceURIs()
  {
    return Arrays.asList(this.nameList.namespaceURIs);
  }
  
  public String getBuildId()
  {
    Package pkg = getClass().getPackage();
    if (pkg == null) {
      return null;
    }
    return pkg.getImplementationVersion();
  }
  
  public String toString()
  {
    StringBuilder buf = new StringBuilder(Which.which(getClass()) + " Build-Id: " + getBuildId());
    buf.append("\nClasses known to this context:\n");
    
    Set<String> names = new TreeSet();
    for (Class key : this.beanInfoMap.keySet()) {
      names.add(key.getName());
    }
    for (String name : names) {
      buf.append("  ").append(name).append('\n');
    }
    return buf.toString();
  }
  
  public String getXMIMEContentType(Object o)
  {
    JaxBeanInfo bi = getBeanInfo(o);
    if (!(bi instanceof ClassBeanInfoImpl)) {
      return null;
    }
    ClassBeanInfoImpl cb = (ClassBeanInfoImpl)bi;
    for (Property p : cb.properties) {
      if ((p instanceof AttributeProperty))
      {
        AttributeProperty ap = (AttributeProperty)p;
        if (ap.attName.equals("http://www.w3.org/2005/05/xmlmime", "contentType")) {
          try
          {
            return (String)ap.xacc.print(o);
          }
          catch (AccessorException e)
          {
            return null;
          }
          catch (SAXException e)
          {
            return null;
          }
          catch (ClassCastException e)
          {
            return null;
          }
        }
      }
    }
    return null;
  }
  
  public JAXBContextImpl createAugmented(Class<?> clazz)
    throws JAXBException
  {
    Class[] newList = new Class[this.classes.length + 1];
    System.arraycopy(this.classes, 0, newList, 0, this.classes.length);
    newList[this.classes.length] = clazz;
    
    return new JAXBContextImpl(newList, this.bridges.keySet(), this.subclassReplacements, this.defaultNsUri, this.c14nSupport, this.annotaitonReader, this.xmlAccessorFactorySupport, this.allNillable);
  }
  
  private static final Comparator<QName> QNAME_COMPARATOR = new Comparator()
  {
    public int compare(QName lhs, QName rhs)
    {
      int r = lhs.getLocalPart().compareTo(rhs.getLocalPart());
      if (r != 0) {
        return r;
      }
      return lhs.getNamespaceURI().compareTo(rhs.getNamespaceURI());
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\JAXBContextImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */