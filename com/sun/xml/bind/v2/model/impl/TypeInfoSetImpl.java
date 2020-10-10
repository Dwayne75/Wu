package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.core.BuiltinLeafInfo;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.RuntimeUtil;
import com.sun.xml.bind.v2.runtime.RuntimeUtil.ToStringAdapter;
import com.sun.xml.bind.v2.util.FlattenIterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

class TypeInfoSetImpl<T, C, F, M>
  implements TypeInfoSet<T, C, F, M>
{
  @XmlTransient
  public final Navigator<T, C, F, M> nav;
  @XmlTransient
  public final AnnotationReader<T, C, F, M> reader;
  private final Map<T, BuiltinLeafInfo<T, C>> builtins = new LinkedHashMap();
  private final Map<C, EnumLeafInfoImpl<T, C, F, M>> enums = new LinkedHashMap();
  private final Map<T, ArrayInfoImpl<T, C, F, M>> arrays = new LinkedHashMap();
  @XmlJavaTypeAdapter(RuntimeUtil.ToStringAdapter.class)
  private final Map<C, ClassInfoImpl<T, C, F, M>> beans = new LinkedHashMap();
  @XmlTransient
  private final Map<C, ClassInfoImpl<T, C, F, M>> beansView = Collections.unmodifiableMap(this.beans);
  private final Map<C, Map<QName, ElementInfoImpl<T, C, F, M>>> elementMappings = new LinkedHashMap();
  private final Iterable<? extends ElementInfoImpl<T, C, F, M>> allElements = new Iterable()
  {
    public Iterator<ElementInfoImpl<T, C, F, M>> iterator()
    {
      return new FlattenIterator(TypeInfoSetImpl.this.elementMappings.values());
    }
  };
  private final NonElement<T, C> anyType;
  private Map<String, Map<String, String>> xmlNsCache;
  
  public TypeInfoSetImpl(Navigator<T, C, F, M> nav, AnnotationReader<T, C, F, M> reader, Map<T, ? extends BuiltinLeafInfoImpl<T, C>> leaves)
  {
    this.nav = nav;
    this.reader = reader;
    this.builtins.putAll(leaves);
    
    this.anyType = createAnyType();
    for (Map.Entry<Class, Class> e : RuntimeUtil.primitiveToBox.entrySet()) {
      this.builtins.put(nav.getPrimitive((Class)e.getKey()), leaves.get(nav.ref((Class)e.getValue())));
    }
    this.elementMappings.put(null, new LinkedHashMap());
  }
  
  protected NonElement<T, C> createAnyType()
  {
    return new AnyTypeImpl(this.nav);
  }
  
  public Navigator<T, C, F, M> getNavigator()
  {
    return this.nav;
  }
  
  public void add(ClassInfoImpl<T, C, F, M> ci)
  {
    this.beans.put(ci.getClazz(), ci);
  }
  
  public void add(EnumLeafInfoImpl<T, C, F, M> li)
  {
    this.enums.put(li.clazz, li);
  }
  
  public void add(ArrayInfoImpl<T, C, F, M> ai)
  {
    this.arrays.put(ai.getType(), ai);
  }
  
  public NonElement<T, C> getTypeInfo(T type)
  {
    type = this.nav.erasure(type);
    
    LeafInfo<T, C> l = (LeafInfo)this.builtins.get(type);
    if (l != null) {
      return l;
    }
    if (this.nav.isArray(type)) {
      return (NonElement)this.arrays.get(type);
    }
    C d = this.nav.asDecl(type);
    if (d == null) {
      return null;
    }
    return getClassInfo(d);
  }
  
  public NonElement<T, C> getAnyTypeInfo()
  {
    return this.anyType;
  }
  
  public NonElement<T, C> getTypeInfo(Ref<T, C> ref)
  {
    assert (!ref.valueList);
    C c = this.nav.asDecl(ref.type);
    if ((c != null) && (this.reader.getClassAnnotation(XmlRegistry.class, c, null) != null)) {
      return null;
    }
    return getTypeInfo(ref.type);
  }
  
  public Map<C, ? extends ClassInfoImpl<T, C, F, M>> beans()
  {
    return this.beansView;
  }
  
  public Map<T, ? extends BuiltinLeafInfo<T, C>> builtins()
  {
    return this.builtins;
  }
  
  public Map<C, ? extends EnumLeafInfoImpl<T, C, F, M>> enums()
  {
    return this.enums;
  }
  
  public Map<? extends T, ? extends ArrayInfoImpl<T, C, F, M>> arrays()
  {
    return this.arrays;
  }
  
  public NonElement<T, C> getClassInfo(C type)
  {
    LeafInfo<T, C> l = (LeafInfo)this.builtins.get(this.nav.use(type));
    if (l != null) {
      return l;
    }
    l = (LeafInfo)this.enums.get(type);
    if (l != null) {
      return l;
    }
    if (this.nav.asDecl(Object.class).equals(type)) {
      return this.anyType;
    }
    return (NonElement)this.beans.get(type);
  }
  
  public ElementInfoImpl<T, C, F, M> getElementInfo(C scope, QName name)
  {
    while (scope != null)
    {
      Map<QName, ElementInfoImpl<T, C, F, M>> m = (Map)this.elementMappings.get(scope);
      if (m != null)
      {
        ElementInfoImpl<T, C, F, M> r = (ElementInfoImpl)m.get(name);
        if (r != null) {
          return r;
        }
      }
      scope = this.nav.getSuperClass(scope);
    }
    return (ElementInfoImpl)((Map)this.elementMappings.get(null)).get(name);
  }
  
  public final void add(ElementInfoImpl<T, C, F, M> ei, ModelBuilder<T, C, F, M> builder)
  {
    C scope = null;
    if (ei.getScope() != null) {
      scope = ei.getScope().getClazz();
    }
    Map<QName, ElementInfoImpl<T, C, F, M>> m = (Map)this.elementMappings.get(scope);
    if (m == null) {
      this.elementMappings.put(scope, m = new LinkedHashMap());
    }
    ElementInfoImpl<T, C, F, M> existing = (ElementInfoImpl)m.put(ei.getElementName(), ei);
    if (existing != null)
    {
      QName en = ei.getElementName();
      builder.reportError(new IllegalAnnotationException(Messages.CONFLICTING_XML_ELEMENT_MAPPING.format(new Object[] { en.getNamespaceURI(), en.getLocalPart() }), ei, existing));
    }
  }
  
  public Map<QName, ? extends ElementInfoImpl<T, C, F, M>> getElementMappings(C scope)
  {
    return (Map)this.elementMappings.get(scope);
  }
  
  public Iterable<? extends ElementInfoImpl<T, C, F, M>> getAllElements()
  {
    return this.allElements;
  }
  
  public Map<String, String> getXmlNs(String namespaceUri)
  {
    if (this.xmlNsCache == null)
    {
      this.xmlNsCache = new HashMap();
      for (ClassInfoImpl<T, C, F, M> ci : beans().values())
      {
        XmlSchema xs = (XmlSchema)this.reader.getPackageAnnotation(XmlSchema.class, ci.getClazz(), null);
        if (xs != null)
        {
          String uri = xs.namespace();
          Map<String, String> m = (Map)this.xmlNsCache.get(uri);
          if (m == null) {
            this.xmlNsCache.put(uri, m = new HashMap());
          }
          for (XmlNs xns : xs.xmlns()) {
            m.put(xns.prefix(), xns.namespaceURI());
          }
        }
      }
    }
    Map<String, String> r = (Map)this.xmlNsCache.get(namespaceUri);
    if (r != null) {
      return r;
    }
    return Collections.emptyMap();
  }
  
  public Map<String, String> getSchemaLocations()
  {
    Map<String, String> r = new HashMap();
    for (ClassInfoImpl<T, C, F, M> ci : beans().values())
    {
      XmlSchema xs = (XmlSchema)this.reader.getPackageAnnotation(XmlSchema.class, ci.getClazz(), null);
      if (xs != null)
      {
        String loc = xs.location();
        if (!loc.equals("##generate")) {
          r.put(xs.namespace(), loc);
        }
      }
    }
    return r;
  }
  
  public final XmlNsForm getElementFormDefault(String nsUri)
  {
    for (ClassInfoImpl<T, C, F, M> ci : beans().values())
    {
      XmlSchema xs = (XmlSchema)this.reader.getPackageAnnotation(XmlSchema.class, ci.getClazz(), null);
      if ((xs != null) && 
      
        (xs.namespace().equals(nsUri)))
      {
        XmlNsForm xnf = xs.elementFormDefault();
        if (xnf != XmlNsForm.UNSET) {
          return xnf;
        }
      }
    }
    return XmlNsForm.UNSET;
  }
  
  public final XmlNsForm getAttributeFormDefault(String nsUri)
  {
    for (ClassInfoImpl<T, C, F, M> ci : beans().values())
    {
      XmlSchema xs = (XmlSchema)this.reader.getPackageAnnotation(XmlSchema.class, ci.getClazz(), null);
      if ((xs != null) && 
      
        (xs.namespace().equals(nsUri)))
      {
        XmlNsForm xnf = xs.attributeFormDefault();
        if (xnf != XmlNsForm.UNSET) {
          return xnf;
        }
      }
    }
    return XmlNsForm.UNSET;
  }
  
  public void dump(Result out)
    throws JAXBException
  {
    JAXBContext context = JAXBContext.newInstance(new Class[] { getClass() });
    Marshaller m = context.createMarshaller();
    m.marshal(this, out);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\TypeInfoSetImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */