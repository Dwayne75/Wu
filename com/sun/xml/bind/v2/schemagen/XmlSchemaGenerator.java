package com.sun.xml.bind.v2.schemagen;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.api.ErrorListener;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.EnumConstant;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.core.MapPropertyInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.NonElementRef;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import com.sun.xml.bind.v2.schemagen.episode.Bindings;
import com.sun.xml.bind.v2.schemagen.episode.Klass;
import com.sun.xml.bind.v2.schemagen.episode.SchemaBindings;
import com.sun.xml.bind.v2.schemagen.xmlschema.Any;
import com.sun.xml.bind.v2.schemagen.xmlschema.AttrDecls;
import com.sun.xml.bind.v2.schemagen.xmlschema.AttributeType;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexContent;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexExtension;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeHost;
import com.sun.xml.bind.v2.schemagen.xmlschema.ContentModelContainer;
import com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup;
import com.sun.xml.bind.v2.schemagen.xmlschema.Import;
import com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute;
import com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement;
import com.sun.xml.bind.v2.schemagen.xmlschema.NoFixedFacet;
import com.sun.xml.bind.v2.schemagen.xmlschema.Occurs;
import com.sun.xml.bind.v2.schemagen.xmlschema.Schema;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleContent;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleExtension;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestrictionModel;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleType;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost;
import com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelAttribute;
import com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement;
import com.sun.xml.bind.v2.schemagen.xmlschema.TypeDefParticle;
import com.sun.xml.bind.v2.schemagen.xmlschema.TypeHost;
import com.sun.xml.bind.v2.schemagen.xmlschema.Wildcard;
import com.sun.xml.bind.v2.util.CollisionCheckStack;
import com.sun.xml.bind.v2.util.StackRecorder;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.TxwException;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.output.ResultFactory;
import com.sun.xml.txw2.output.XmlSerializer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimeType;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXParseException;

public final class XmlSchemaGenerator<T, C, F, M>
{
  private static final Logger logger = com.sun.xml.bind.Util.getClassLogger();
  private final Map<String, XmlSchemaGenerator<T, C, F, M>.Namespace> namespaces = new TreeMap(NAMESPACE_COMPARATOR);
  private ErrorListener errorListener;
  private Navigator<T, C, F, M> navigator;
  private final TypeInfoSet<T, C, F, M> types;
  private final NonElement<T, C> stringType;
  private final NonElement<T, C> anyType;
  private final CollisionCheckStack<ClassInfo<T, C>> collisionChecker = new CollisionCheckStack();
  
  public XmlSchemaGenerator(Navigator<T, C, F, M> navigator, TypeInfoSet<T, C, F, M> types)
  {
    this.navigator = navigator;
    this.types = types;
    
    this.stringType = types.getTypeInfo(navigator.ref(String.class));
    this.anyType = types.getAnyTypeInfo();
    for (ClassInfo<T, C> ci : types.beans().values()) {
      add(ci);
    }
    for (ElementInfo<T, C> ei1 : types.getElementMappings(null).values()) {
      add(ei1);
    }
    for (EnumLeafInfo<T, C> ei : types.enums().values()) {
      add(ei);
    }
    for (ArrayInfo<T, C> a : types.arrays().values()) {
      add(a);
    }
  }
  
  private XmlSchemaGenerator<T, C, F, M>.Namespace getNamespace(String uri)
  {
    XmlSchemaGenerator<T, C, F, M>.Namespace n = (Namespace)this.namespaces.get(uri);
    if (n == null) {
      this.namespaces.put(uri, n = new Namespace(uri));
    }
    return n;
  }
  
  public void add(ClassInfo<T, C> clazz)
  {
    assert (clazz != null);
    
    String nsUri = null;
    if (clazz.getClazz() == this.navigator.asDecl(CompositeStructure.class)) {
      return;
    }
    if (clazz.isElement())
    {
      nsUri = clazz.getElementName().getNamespaceURI();
      XmlSchemaGenerator<T, C, F, M>.Namespace ns = getNamespace(nsUri);
      ns.classes.add(clazz);
      ns.addDependencyTo(clazz.getTypeName());
      
      add(clazz.getElementName(), false, clazz);
    }
    QName tn = clazz.getTypeName();
    if (tn != null) {
      nsUri = tn.getNamespaceURI();
    } else if (nsUri == null) {
      return;
    }
    XmlSchemaGenerator<T, C, F, M>.Namespace n = getNamespace(nsUri);
    n.classes.add(clazz);
    for (PropertyInfo<T, C> p : clazz.getProperties())
    {
      n.processForeignNamespaces(p);
      if ((p instanceof AttributePropertyInfo))
      {
        AttributePropertyInfo<T, C> ap = (AttributePropertyInfo)p;
        String aUri = ap.getXmlName().getNamespaceURI();
        if (aUri.length() > 0)
        {
          getNamespace(aUri).addGlobalAttribute(ap);
          n.addDependencyTo(ap.getXmlName());
        }
      }
      if ((p instanceof ElementPropertyInfo))
      {
        ElementPropertyInfo<T, C> ep = (ElementPropertyInfo)p;
        for (TypeRef<T, C> tref : ep.getTypes())
        {
          String eUri = tref.getTagName().getNamespaceURI();
          if ((eUri.length() > 0) && (!eUri.equals(n.uri)))
          {
            getNamespace(eUri).addGlobalElement(tref);
            n.addDependencyTo(tref.getTagName());
          }
        }
      }
      if (generateSwaRefAdapter(p)) {
        n.useSwaRef = true;
      }
    }
    ClassInfo<T, C> bc = clazz.getBaseClass();
    if (bc != null)
    {
      add(bc);
      n.addDependencyTo(bc.getTypeName());
    }
  }
  
  public void add(ElementInfo<T, C> elem)
  {
    assert (elem != null);
    
    QName name = elem.getElementName();
    XmlSchemaGenerator<T, C, F, M>.Namespace n = getNamespace(name.getNamespaceURI()); XmlSchemaGenerator<T, C, F, M>.Namespace 
      tmp47_46 = n;tmp47_46.getClass();n.elementDecls.put(name.getLocalPart(), new XmlSchemaGenerator.Namespace.ElementWithType(tmp47_46, true, elem.getContentType()));
    
    n.processForeignNamespaces(elem.getProperty());
  }
  
  public void add(EnumLeafInfo<T, C> envm)
  {
    assert (envm != null);
    
    String nsUri = null;
    if (envm.isElement())
    {
      nsUri = envm.getElementName().getNamespaceURI();
      XmlSchemaGenerator<T, C, F, M>.Namespace ns = getNamespace(nsUri);
      ns.enums.add(envm);
      ns.addDependencyTo(envm.getTypeName());
      
      add(envm.getElementName(), false, envm);
    }
    QName typeName = envm.getTypeName();
    if (typeName != null) {
      nsUri = typeName.getNamespaceURI();
    } else if (nsUri == null) {
      return;
    }
    XmlSchemaGenerator<T, C, F, M>.Namespace n = getNamespace(nsUri);
    n.enums.add(envm);
    
    n.addDependencyTo(envm.getBaseType().getTypeName());
  }
  
  public void add(ArrayInfo<T, C> a)
  {
    assert (a != null);
    
    String namespaceURI = a.getTypeName().getNamespaceURI();
    XmlSchemaGenerator<T, C, F, M>.Namespace n = getNamespace(namespaceURI);
    n.arrays.add(a);
    
    n.addDependencyTo(a.getItemType().getTypeName());
  }
  
  public void add(QName tagName, boolean isNillable, NonElement<T, C> type)
  {
    if ((type != null) && (type.getType() == this.navigator.ref(CompositeStructure.class))) {
      return;
    }
    XmlSchemaGenerator<T, C, F, M>.Namespace n = getNamespace(tagName.getNamespaceURI()); XmlSchemaGenerator<T, C, F, M>.Namespace 
      tmp51_49 = n;tmp51_49.getClass();n.elementDecls.put(tagName.getLocalPart(), new XmlSchemaGenerator.Namespace.ElementWithType(tmp51_49, isNillable, type));
    if (type != null) {
      n.addDependencyTo(type.getTypeName());
    }
  }
  
  public void writeEpisodeFile(XmlSerializer out)
  {
    Bindings root = (Bindings)TXW.create(Bindings.class, out);
    if (this.namespaces.containsKey("")) {
      root._namespace("http://java.sun.com/xml/ns/jaxb", "jaxb");
    }
    root.version("2.1");
    for (Map.Entry<String, XmlSchemaGenerator<T, C, F, M>.Namespace> e : this.namespaces.entrySet())
    {
      Bindings group = root.bindings();
      
      String tns = (String)e.getKey();
      String prefix;
      String prefix;
      if (!tns.equals(""))
      {
        group._namespace(tns, "tns");
        prefix = "tns:";
      }
      else
      {
        prefix = "";
      }
      group.scd("x-schema::" + (tns.equals("") ? "" : "tns"));
      group.schemaBindings().map(false);
      for (ClassInfo<T, C> ci : ((Namespace)e.getValue()).classes) {
        if (ci.getTypeName() != null)
        {
          if (ci.getTypeName().getNamespaceURI().equals(tns))
          {
            Bindings child = group.bindings();
            child.scd('~' + prefix + ci.getTypeName().getLocalPart());
            child.klass().ref(ci.getName());
          }
          if ((ci.isElement()) && (ci.getElementName().getNamespaceURI().equals(tns)))
          {
            Bindings child = group.bindings();
            child.scd(prefix + ci.getElementName().getLocalPart());
            child.klass().ref(ci.getName());
          }
        }
      }
      for (EnumLeafInfo<T, C> en : ((Namespace)e.getValue()).enums) {
        if (en.getTypeName() != null)
        {
          Bindings child = group.bindings();
          child.scd('~' + prefix + en.getTypeName().getLocalPart());
          child.klass().ref(this.navigator.getClassName(en.getClazz()));
        }
      }
      group.commit(true);
    }
    root.commit();
  }
  
  public void write(SchemaOutputResolver resolver, ErrorListener errorListener)
    throws IOException
  {
    if (resolver == null) {
      throw new IllegalArgumentException();
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.log(Level.FINE, "Wrigin XML Schema for " + toString(), new StackRecorder());
    }
    resolver = new FoolProofResolver(resolver);
    this.errorListener = errorListener;
    
    Map<String, String> schemaLocations = this.types.getSchemaLocations();
    
    Map<XmlSchemaGenerator<T, C, F, M>.Namespace, Result> out = new HashMap();
    Map<XmlSchemaGenerator<T, C, F, M>.Namespace, String> systemIds = new HashMap();
    
    this.namespaces.remove("http://www.w3.org/2001/XMLSchema");
    for (XmlSchemaGenerator<T, C, F, M>.Namespace n : this.namespaces.values())
    {
      String schemaLocation = (String)schemaLocations.get(n.uri);
      if (schemaLocation != null)
      {
        systemIds.put(n, schemaLocation);
      }
      else
      {
        Result output = resolver.createOutput(n.uri, "schema" + (out.size() + 1) + ".xsd");
        if (output != null)
        {
          out.put(n, output);
          systemIds.put(n, output.getSystemId());
        }
      }
    }
    for (Map.Entry<XmlSchemaGenerator<T, C, F, M>.Namespace, Result> e : out.entrySet())
    {
      Result result = (Result)e.getValue();
      ((Namespace)e.getKey()).writeTo(result, systemIds);
      if ((result instanceof StreamResult))
      {
        OutputStream outputStream = ((StreamResult)result).getOutputStream();
        if (outputStream != null)
        {
          outputStream.close();
        }
        else
        {
          Writer writer = ((StreamResult)result).getWriter();
          if (writer != null) {
            writer.close();
          }
        }
      }
    }
  }
  
  private class Namespace
  {
    @NotNull
    final String uri;
    private final Set<XmlSchemaGenerator<T, C, F, M>.Namespace> depends = new LinkedHashSet();
    private boolean selfReference;
    private final Set<ClassInfo<T, C>> classes = new LinkedHashSet();
    private final Set<EnumLeafInfo<T, C>> enums = new LinkedHashSet();
    private final Set<ArrayInfo<T, C>> arrays = new LinkedHashSet();
    private final MultiMap<String, AttributePropertyInfo<T, C>> attributeDecls = new MultiMap(null);
    private final MultiMap<String, XmlSchemaGenerator<T, C, F, M>.Namespace.ElementDeclaration> elementDecls = new MultiMap(new ElementWithType(true, XmlSchemaGenerator.this.anyType));
    private Form attributeFormDefault;
    private Form elementFormDefault;
    private boolean useSwaRef;
    
    public Namespace(String uri)
    {
      this.uri = uri;
      assert (!XmlSchemaGenerator.this.namespaces.containsKey(uri));
      XmlSchemaGenerator.this.namespaces.put(uri, this);
    }
    
    private void processForeignNamespaces(PropertyInfo<T, C> p)
    {
      for (TypeInfo<T, C> t : p.ref())
      {
        if ((t instanceof Element)) {
          addDependencyTo(((Element)t).getElementName());
        }
        if ((t instanceof NonElement)) {
          addDependencyTo(((NonElement)t).getTypeName());
        }
      }
    }
    
    private void addDependencyTo(@Nullable QName qname)
    {
      if (qname == null) {
        return;
      }
      String nsUri = qname.getNamespaceURI();
      if (nsUri.equals("http://www.w3.org/2001/XMLSchema")) {
        return;
      }
      if (nsUri.equals(this.uri))
      {
        this.selfReference = true;
        return;
      }
      this.depends.add(XmlSchemaGenerator.this.getNamespace(nsUri));
    }
    
    private void writeTo(Result result, Map<XmlSchemaGenerator<T, C, F, M>.Namespace, String> systemIds)
      throws IOException
    {
      try
      {
        Schema schema = (Schema)TXW.create(Schema.class, ResultFactory.createSerializer(result));
        
        Map<String, String> xmlNs = XmlSchemaGenerator.this.types.getXmlNs(this.uri);
        for (Map.Entry<String, String> e : xmlNs.entrySet()) {
          schema._namespace((String)e.getValue(), (String)e.getKey());
        }
        if (this.useSwaRef) {
          schema._namespace("http://ws-i.org/profiles/basic/1.1/xsd", "swaRef");
        }
        this.attributeFormDefault = Form.get(XmlSchemaGenerator.this.types.getAttributeFormDefault(this.uri));
        this.attributeFormDefault.declare("attributeFormDefault", schema);
        
        this.elementFormDefault = Form.get(XmlSchemaGenerator.this.types.getElementFormDefault(this.uri));
        
        this.elementFormDefault.declare("elementFormDefault", schema);
        if ((!xmlNs.containsValue("http://www.w3.org/2001/XMLSchema")) && (!xmlNs.containsKey("xs"))) {
          schema._namespace("http://www.w3.org/2001/XMLSchema", "xs");
        }
        schema.version("1.0");
        if (this.uri.length() != 0) {
          schema.targetNamespace(this.uri);
        }
        for (XmlSchemaGenerator<T, C, F, M>.Namespace ns : this.depends) {
          schema._namespace(ns.uri);
        }
        if ((this.selfReference) && (this.uri.length() != 0)) {
          schema._namespace(this.uri, "tns");
        }
        schema._pcdata("\n");
        for (XmlSchemaGenerator<T, C, F, M>.Namespace n : this.depends)
        {
          Import imp = schema._import();
          if (n.uri.length() != 0) {
            imp.namespace(n.uri);
          }
          String refSystemId = (String)systemIds.get(n);
          if ((refSystemId != null) && (!refSystemId.equals(""))) {
            imp.schemaLocation(XmlSchemaGenerator.relativize(refSystemId, result.getSystemId()));
          }
          schema._pcdata("\n");
        }
        if (this.useSwaRef) {
          schema._import().namespace("http://ws-i.org/profiles/basic/1.1/xsd").schemaLocation("http://ws-i.org/profiles/basic/1.1/swaref.xsd");
        }
        for (Map.Entry<String, XmlSchemaGenerator<T, C, F, M>.Namespace.ElementDeclaration> e : this.elementDecls.entrySet())
        {
          ((ElementDeclaration)e.getValue()).writeTo((String)e.getKey(), schema);
          schema._pcdata("\n");
        }
        for (ClassInfo<T, C> c : this.classes) {
          if (c.getTypeName() != null)
          {
            if (this.uri.equals(c.getTypeName().getNamespaceURI())) {
              writeClass(c, schema);
            }
            schema._pcdata("\n");
          }
        }
        for (EnumLeafInfo<T, C> e : this.enums) {
          if (e.getTypeName() != null)
          {
            if (this.uri.equals(e.getTypeName().getNamespaceURI())) {
              writeEnum(e, schema);
            }
            schema._pcdata("\n");
          }
        }
        for (ArrayInfo<T, C> a : this.arrays)
        {
          writeArray(a, schema);
          schema._pcdata("\n");
        }
        for (Map.Entry<String, AttributePropertyInfo<T, C>> e : this.attributeDecls.entrySet())
        {
          TopLevelAttribute a = schema.attribute();
          a.name((String)e.getKey());
          if (e.getValue() == null) {
            writeTypeRef(a, XmlSchemaGenerator.this.stringType, "type");
          } else {
            writeAttributeTypeRef((AttributePropertyInfo)e.getValue(), a);
          }
          schema._pcdata("\n");
        }
        schema.commit();
      }
      catch (TxwException e)
      {
        XmlSchemaGenerator.logger.log(Level.INFO, e.getMessage(), e);
        throw new IOException(e.getMessage());
      }
    }
    
    private void writeTypeRef(TypeHost th, NonElementRef<T, C> typeRef, String refAttName)
    {
      switch (XmlSchemaGenerator.2.$SwitchMap$com$sun$xml$bind$v2$model$core$ID[typeRef.getSource().id().ordinal()])
      {
      case 1: 
        th._attribute(refAttName, new QName("http://www.w3.org/2001/XMLSchema", "ID"));
        return;
      case 2: 
        th._attribute(refAttName, new QName("http://www.w3.org/2001/XMLSchema", "IDREF")); return;
      case 3: 
        break;
      default: 
        throw new IllegalStateException();
      }
      MimeType mimeType = typeRef.getSource().getExpectedMimeType();
      if (mimeType != null) {
        th._attribute(new QName("http://www.w3.org/2005/05/xmlmime", "expectedContentTypes", "xmime"), mimeType.toString());
      }
      if (XmlSchemaGenerator.this.generateSwaRefAdapter(typeRef))
      {
        th._attribute(refAttName, new QName("http://ws-i.org/profiles/basic/1.1/xsd", "swaRef", "ref"));
        return;
      }
      if (typeRef.getSource().getSchemaType() != null)
      {
        th._attribute(refAttName, typeRef.getSource().getSchemaType());
        return;
      }
      writeTypeRef(th, typeRef.getTarget(), refAttName);
    }
    
    private void writeTypeRef(TypeHost th, NonElement<T, C> type, String refAttName)
    {
      if (type.getTypeName() == null)
      {
        th.block();
        if ((type instanceof ClassInfo))
        {
          if (XmlSchemaGenerator.this.collisionChecker.push((ClassInfo)type)) {
            XmlSchemaGenerator.this.errorListener.error(new SAXParseException(Messages.ANONYMOUS_TYPE_CYCLE.format(new Object[] { XmlSchemaGenerator.this.collisionChecker.getCycleString() }), null));
          } else {
            writeClass((ClassInfo)type, th);
          }
          XmlSchemaGenerator.this.collisionChecker.pop();
        }
        else
        {
          writeEnum((EnumLeafInfo)type, (SimpleTypeHost)th);
        }
      }
      else
      {
        th._attribute(refAttName, type.getTypeName());
      }
    }
    
    private void writeArray(ArrayInfo<T, C> a, Schema schema)
    {
      ComplexType ct = schema.complexType().name(a.getTypeName().getLocalPart());
      ct._final("#all");
      LocalElement le = ct.sequence().element().name("item");
      le.type(a.getItemType().getTypeName());
      le.minOccurs(0).maxOccurs("unbounded");
      le.nillable(true);
      ct.commit();
    }
    
    private void writeEnum(EnumLeafInfo<T, C> e, SimpleTypeHost th)
    {
      SimpleType st = th.simpleType();
      writeName(e, st);
      
      SimpleRestrictionModel base = st.restriction();
      writeTypeRef(base, e.getBaseType(), "base");
      for (EnumConstant c : e.getConstants()) {
        base.enumeration().value(c.getLexicalValue());
      }
      st.commit();
    }
    
    private void writeClass(ClassInfo<T, C> c, TypeHost parent)
    {
      if (containsValueProp(c))
      {
        if (c.getProperties().size() == 1)
        {
          ValuePropertyInfo<T, C> vp = (ValuePropertyInfo)c.getProperties().get(0);
          SimpleType st = ((SimpleTypeHost)parent).simpleType();
          writeName(c, st);
          if (vp.isCollection()) {
            writeTypeRef(st.list(), vp.getTarget(), "itemType");
          } else {
            writeTypeRef(st.restriction(), vp.getTarget(), "base");
          }
          return;
        }
        ComplexType ct = ((ComplexTypeHost)parent).complexType();
        writeName(c, ct);
        if (c.isFinal()) {
          ct._final("extension restriction");
        }
        SimpleExtension se = ct.simpleContent().extension();
        se.block();
        for (PropertyInfo<T, C> p : c.getProperties()) {
          switch (XmlSchemaGenerator.2.$SwitchMap$com$sun$xml$bind$v2$model$core$PropertyKind[p.kind().ordinal()])
          {
          case 1: 
            handleAttributeProp((AttributePropertyInfo)p, se);
            break;
          case 2: 
            TODO.checkSpec("what if vp.isCollection() == true?");
            ValuePropertyInfo vp = (ValuePropertyInfo)p;
            se.base(vp.getTarget().getTypeName());
            break;
          case 3: 
          case 4: 
          default: 
            if (!$assertionsDisabled) {
              throw new AssertionError();
            }
            throw new IllegalStateException();
          }
        }
        se.commit();
        
        TODO.schemaGenerator("figure out what to do if bc != null");
        TODO.checkSpec("handle sec 8.9.5.2, bullet #4");
        
        return;
      }
      ComplexType ct = ((ComplexTypeHost)parent).complexType();
      writeName(c, ct);
      if (c.isFinal()) {
        ct._final("extension restriction");
      }
      if (c.isAbstract()) {
        ct._abstract(true);
      }
      AttrDecls contentModel = ct;
      TypeDefParticle contentModelOwner = ct;
      
      ClassInfo<T, C> bc = c.getBaseClass();
      if (bc != null) {
        if (bc.hasValueProperty())
        {
          SimpleExtension se = ct.simpleContent().extension();
          contentModel = se;
          contentModelOwner = null;
          se.base(bc.getTypeName());
        }
        else
        {
          ComplexExtension ce = ct.complexContent().extension();
          contentModel = ce;
          contentModelOwner = ce;
          
          ce.base(bc.getTypeName());
        }
      }
      if (contentModelOwner != null)
      {
        ArrayList<Tree> children = new ArrayList();
        for (PropertyInfo<T, C> p : c.getProperties())
        {
          if (((p instanceof ReferencePropertyInfo)) && (((ReferencePropertyInfo)p).isMixed())) {
            ct.mixed(true);
          }
          Tree t = buildPropertyContentModel(p);
          if (t != null) {
            children.add(t);
          }
        }
        Tree top = Tree.makeGroup(c.isOrdered() ? GroupKind.SEQUENCE : GroupKind.ALL, children);
        
        top.write(contentModelOwner);
      }
      for (PropertyInfo<T, C> p : c.getProperties()) {
        if ((p instanceof AttributePropertyInfo)) {
          handleAttributeProp((AttributePropertyInfo)p, contentModel);
        }
      }
      if (c.hasAttributeWildcard()) {
        contentModel.anyAttribute().namespace("##other").processContents("skip");
      }
      ct.commit();
    }
    
    private void writeName(NonElement<T, C> c, TypedXmlWriter xw)
    {
      QName tn = c.getTypeName();
      if (tn != null) {
        xw._attribute("name", tn.getLocalPart());
      }
    }
    
    private boolean containsValueProp(ClassInfo<T, C> c)
    {
      for (PropertyInfo p : c.getProperties()) {
        if ((p instanceof ValuePropertyInfo)) {
          return true;
        }
      }
      return false;
    }
    
    private Tree buildPropertyContentModel(PropertyInfo<T, C> p)
    {
      switch (XmlSchemaGenerator.2.$SwitchMap$com$sun$xml$bind$v2$model$core$PropertyKind[p.kind().ordinal()])
      {
      case 3: 
        return handleElementProp((ElementPropertyInfo)p);
      case 1: 
        return null;
      case 4: 
        return handleReferenceProp((ReferencePropertyInfo)p);
      case 5: 
        return handleMapProp((MapPropertyInfo)p);
      case 2: 
        if (!$assertionsDisabled) {
          throw new AssertionError();
        }
        throw new IllegalStateException();
      }
      if (!$assertionsDisabled) {
        throw new AssertionError();
      }
      throw new IllegalStateException();
    }
    
    private Tree handleElementProp(final ElementPropertyInfo<T, C> ep)
    {
      if (ep.isValueList()) {
        new Tree.Term()
        {
          protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated)
          {
            TypeRef<T, C> t = (TypeRef)ep.getTypes().get(0);
            LocalElement e = parent.element();
            e.block();
            QName tn = t.getTagName();
            e.name(tn.getLocalPart());
            com.sun.xml.bind.v2.schemagen.xmlschema.List lst = e.simpleType().list();
            XmlSchemaGenerator.Namespace.this.writeTypeRef(lst, t, "itemType");
            XmlSchemaGenerator.Namespace.this.elementFormDefault.writeForm(e, tn);
            writeOccurs(e, (isOptional) || (!ep.isRequired()), repeated);
          }
        };
      }
      ArrayList<Tree> children = new ArrayList();
      for (final TypeRef<T, C> t : ep.getTypes()) {
        children.add(new Tree.Term()
        {
          protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated)
          {
            LocalElement e = parent.element();
            
            QName tn = t.getTagName();
            if ((XmlSchemaGenerator.Namespace.this.canBeDirectElementRef(t, tn)) || ((!tn.getNamespaceURI().equals(XmlSchemaGenerator.Namespace.this.uri)) && (tn.getNamespaceURI().length() > 0)))
            {
              e.ref(tn);
            }
            else
            {
              e.name(tn.getLocalPart());
              XmlSchemaGenerator.Namespace.this.writeTypeRef(e, t, "type");
              XmlSchemaGenerator.Namespace.this.elementFormDefault.writeForm(e, tn);
            }
            if (t.isNillable()) {
              e.nillable(true);
            }
            if (t.getDefaultValue() != null) {
              e._default(t.getDefaultValue());
            }
            writeOccurs(e, isOptional, repeated);
          }
        });
      }
      final Tree choice = Tree.makeGroup(GroupKind.CHOICE, children).makeOptional(!ep.isRequired()).makeRepeated(ep.isCollection());
      
      final QName ename = ep.getXmlName();
      if (ename != null) {
        new Tree.Term()
        {
          protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated)
          {
            LocalElement e = parent.element();
            if ((ename.getNamespaceURI().length() > 0) && 
              (!ename.getNamespaceURI().equals(XmlSchemaGenerator.Namespace.this.uri)))
            {
              e.ref(new QName(ename.getNamespaceURI(), ename.getLocalPart()));
              return;
            }
            e.name(ename.getLocalPart());
            XmlSchemaGenerator.Namespace.this.elementFormDefault.writeForm(e, ename);
            if (ep.isCollectionNillable()) {
              e.nillable(true);
            }
            writeOccurs(e, !ep.isCollectionRequired(), repeated);
            
            ComplexType p = e.complexType();
            choice.write(p);
          }
        };
      }
      return choice;
    }
    
    private boolean canBeDirectElementRef(TypeRef<T, C> t, QName tn)
    {
      if ((t.isNillable()) || (t.getDefaultValue() != null)) {
        return false;
      }
      if ((t.getTarget() instanceof Element))
      {
        Element te = (Element)t.getTarget();
        QName targetTagName = te.getElementName();
        return (targetTagName != null) && (targetTagName.equals(tn));
      }
      return false;
    }
    
    private void handleAttributeProp(AttributePropertyInfo<T, C> ap, AttrDecls attr)
    {
      LocalAttribute localAttribute = attr.attribute();
      
      String attrURI = ap.getXmlName().getNamespaceURI();
      if (attrURI.equals(""))
      {
        localAttribute.name(ap.getXmlName().getLocalPart());
        
        writeAttributeTypeRef(ap, localAttribute);
        
        this.attributeFormDefault.writeForm(localAttribute, ap.getXmlName());
      }
      else
      {
        localAttribute.ref(ap.getXmlName());
      }
      if (ap.isRequired()) {
        localAttribute.use("required");
      }
    }
    
    private void writeAttributeTypeRef(AttributePropertyInfo<T, C> ap, AttributeType a)
    {
      if (ap.isCollection()) {
        writeTypeRef(a.simpleType().list(), ap, "itemType");
      } else {
        writeTypeRef(a, ap, "type");
      }
    }
    
    private Tree handleReferenceProp(final ReferencePropertyInfo<T, C> rp)
    {
      ArrayList<Tree> children = new ArrayList();
      for (final Element<T, C> e : rp.getElements()) {
        children.add(new Tree.Term()
        {
          protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated)
          {
            LocalElement eref = parent.element();
            
            boolean local = false;
            
            QName en = e.getElementName();
            if (e.getScope() != null)
            {
              boolean qualified = en.getNamespaceURI().equals(XmlSchemaGenerator.Namespace.this.uri);
              boolean unqualified = en.getNamespaceURI().equals("");
              if ((qualified) || (unqualified))
              {
                if (unqualified)
                {
                  if (XmlSchemaGenerator.Namespace.this.elementFormDefault.isEffectivelyQualified) {
                    eref.form("unqualified");
                  }
                }
                else if (!XmlSchemaGenerator.Namespace.this.elementFormDefault.isEffectivelyQualified) {
                  eref.form("qualified");
                }
                local = true;
                eref.name(en.getLocalPart());
                if ((e instanceof ClassInfo)) {
                  XmlSchemaGenerator.Namespace.this.writeTypeRef(eref, (ClassInfo)e, "type");
                } else {
                  XmlSchemaGenerator.Namespace.this.writeTypeRef(eref, ((ElementInfo)e).getContentType(), "type");
                }
              }
            }
            if (!local) {
              eref.ref(en);
            }
            writeOccurs(eref, isOptional, repeated);
          }
        });
      }
      final WildcardMode wc = rp.getWildcard();
      if (wc != null) {
        children.add(new Tree.Term()
        {
          protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated)
          {
            Any any = parent.any();
            String pcmode = XmlSchemaGenerator.getProcessContentsModeName(wc);
            if (pcmode != null) {
              any.processContents(pcmode);
            }
            any.namespace("##other");
            writeOccurs(any, isOptional, repeated);
          }
        });
      }
      final Tree choice = Tree.makeGroup(GroupKind.CHOICE, children).makeRepeated(rp.isCollection()).makeOptional(rp.isCollection());
      
      final QName ename = rp.getXmlName();
      if (ename != null) {
        new Tree.Term()
        {
          protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated)
          {
            LocalElement e = parent.element().name(ename.getLocalPart());
            XmlSchemaGenerator.Namespace.this.elementFormDefault.writeForm(e, ename);
            if (rp.isCollectionNillable()) {
              e.nillable(true);
            }
            writeOccurs(e, true, repeated);
            
            ComplexType p = e.complexType();
            choice.write(p);
          }
        };
      }
      return choice;
    }
    
    private Tree handleMapProp(final MapPropertyInfo<T, C> mp)
    {
      new Tree.Term()
      {
        protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated)
        {
          QName ename = mp.getXmlName();
          
          LocalElement e = parent.element();
          XmlSchemaGenerator.Namespace.this.elementFormDefault.writeForm(e, ename);
          if (mp.isCollectionNillable()) {
            e.nillable(true);
          }
          e = e.name(ename.getLocalPart());
          writeOccurs(e, isOptional, repeated);
          ComplexType p = e.complexType();
          
          e = p.sequence().element();
          e.name("entry").minOccurs(0).maxOccurs("unbounded");
          
          ExplicitGroup seq = e.complexType().sequence();
          XmlSchemaGenerator.Namespace.this.writeKeyOrValue(seq, "key", mp.getKeyType());
          XmlSchemaGenerator.Namespace.this.writeKeyOrValue(seq, "value", mp.getValueType());
        }
      };
    }
    
    private void writeKeyOrValue(ExplicitGroup seq, String tagName, NonElement<T, C> typeRef)
    {
      LocalElement key = seq.element().name(tagName);
      key.minOccurs(0);
      writeTypeRef(key, typeRef, "type");
    }
    
    public void addGlobalAttribute(AttributePropertyInfo<T, C> ap)
    {
      this.attributeDecls.put(ap.getXmlName().getLocalPart(), ap);
      addDependencyTo(ap.getTarget().getTypeName());
    }
    
    public void addGlobalElement(TypeRef<T, C> tref)
    {
      this.elementDecls.put(tref.getTagName().getLocalPart(), new ElementWithType(false, tref.getTarget()));
      addDependencyTo(tref.getTarget().getTypeName());
    }
    
    public String toString()
    {
      StringBuilder buf = new StringBuilder();
      buf.append("[classes=").append(this.classes);
      buf.append(",elementDecls=").append(this.elementDecls);
      buf.append(",enums=").append(this.enums);
      buf.append("]");
      return super.toString();
    }
    
    class ElementWithType
      extends XmlSchemaGenerator.Namespace.ElementDeclaration
    {
      private final boolean nillable;
      private final NonElement<T, C> type;
      
      public ElementWithType(NonElement<T, C> nillable)
      {
        super();
        this.type = type;
        this.nillable = nillable;
      }
      
      public void writeTo(String localName, Schema schema)
      {
        TopLevelElement e = schema.element().name(localName);
        if (this.nillable) {
          e.nillable(true);
        }
        if (this.type != null) {
          XmlSchemaGenerator.Namespace.this.writeTypeRef(e, this.type, "type");
        } else {
          e.complexType();
        }
        e.commit();
      }
      
      public boolean equals(Object o)
      {
        if (this == o) {
          return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
          return false;
        }
        XmlSchemaGenerator<T, C, F, M>.Namespace.ElementWithType that = (ElementWithType)o;
        return this.type.equals(that.type);
      }
      
      public int hashCode()
      {
        return this.type.hashCode();
      }
    }
    
    abstract class ElementDeclaration
    {
      ElementDeclaration() {}
      
      public abstract boolean equals(Object paramObject);
      
      public abstract int hashCode();
      
      public abstract void writeTo(String paramString, Schema paramSchema);
    }
  }
  
  private boolean generateSwaRefAdapter(NonElementRef<T, C> typeRef)
  {
    return generateSwaRefAdapter(typeRef.getSource());
  }
  
  private boolean generateSwaRefAdapter(PropertyInfo<T, C> prop)
  {
    Adapter<T, C> adapter = prop.getAdapter();
    if (adapter == null) {
      return false;
    }
    Object o = this.navigator.asDecl(SwaRefAdapter.class);
    if (o == null) {
      return false;
    }
    return o.equals(adapter.adapterType);
  }
  
  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    for (XmlSchemaGenerator<T, C, F, M>.Namespace ns : this.namespaces.values())
    {
      if (buf.length() > 0) {
        buf.append(',');
      }
      buf.append(ns.uri).append('=').append(ns);
    }
    return super.toString() + '[' + buf + ']';
  }
  
  private static String getProcessContentsModeName(WildcardMode wc)
  {
    switch (wc)
    {
    case LAX: 
    case SKIP: 
      return wc.name().toLowerCase();
    case STRICT: 
      return null;
    }
    throw new IllegalStateException();
  }
  
  protected static String relativize(String uri, String baseUri)
  {
    try
    {
      assert (uri != null);
      if (baseUri == null) {
        return uri;
      }
      URI theUri = new URI(Util.escapeURI(uri));
      URI theBaseUri = new URI(Util.escapeURI(baseUri));
      if ((theUri.isOpaque()) || (theBaseUri.isOpaque())) {
        return uri;
      }
      if ((!Util.equalsIgnoreCase(theUri.getScheme(), theBaseUri.getScheme())) || (!Util.equal(theUri.getAuthority(), theBaseUri.getAuthority()))) {
        return uri;
      }
      String uriPath = theUri.getPath();
      String basePath = theBaseUri.getPath();
      if (!basePath.endsWith("/")) {
        basePath = Util.normalizeUriPath(basePath);
      }
      if (uriPath.equals(basePath)) {
        return ".";
      }
      String relPath = calculateRelativePath(uriPath, basePath, fixNull(theUri.getScheme()).equals("file"));
      if (relPath == null) {
        return uri;
      }
      StringBuffer relUri = new StringBuffer();
      relUri.append(relPath);
      if (theUri.getQuery() != null) {
        relUri.append('?').append(theUri.getQuery());
      }
      if (theUri.getFragment() != null) {
        relUri.append('#').append(theUri.getFragment());
      }
      return relUri.toString();
    }
    catch (URISyntaxException e)
    {
      throw new InternalError("Error escaping one of these uris:\n\t" + uri + "\n\t" + baseUri);
    }
  }
  
  private static String fixNull(String s)
  {
    if (s == null) {
      return "";
    }
    return s;
  }
  
  private static String calculateRelativePath(String uri, String base, boolean fileUrl)
  {
    boolean onWindows = File.pathSeparatorChar == ';';
    if (base == null) {
      return null;
    }
    if (((fileUrl) && (onWindows) && (startsWithIgnoreCase(uri, base))) || (uri.startsWith(base))) {
      return uri.substring(base.length());
    }
    return "../" + calculateRelativePath(uri, Util.getParentUriPath(base), fileUrl);
  }
  
  private static boolean startsWithIgnoreCase(String s, String t)
  {
    return s.toUpperCase().startsWith(t.toUpperCase());
  }
  
  private static final Comparator<String> NAMESPACE_COMPARATOR = new Comparator()
  {
    public int compare(String lhs, String rhs)
    {
      return -lhs.compareTo(rhs);
    }
  };
  private static final String newline = "\n";
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\XmlSchemaGenerator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */