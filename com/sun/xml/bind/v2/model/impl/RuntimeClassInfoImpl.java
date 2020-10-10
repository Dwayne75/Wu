package com.sun.xml.bind.v2.model.impl;

import com.sun.istack.NotNull;
import com.sun.xml.bind.AccessorFactory;
import com.sun.xml.bind.AccessorFactoryImpl;
import com.sun.xml.bind.XmlAccessorFactory;
import com.sun.xml.bind.annotation.XmlLocation;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.Accessor.FieldReflection;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class RuntimeClassInfoImpl
  extends ClassInfoImpl<Type, Class, Field, Method>
  implements RuntimeClassInfo, RuntimeElement
{
  private Accessor<?, Locator> xmlLocationAccessor;
  private AccessorFactory accessorFactory;
  private Accessor<?, Map<QName, String>> attributeWildcardAccessor;
  
  public RuntimeClassInfoImpl(RuntimeModelBuilder modelBuilder, Locatable upstream, Class clazz)
  {
    super(modelBuilder, upstream, clazz);
    this.accessorFactory = createAccessorFactory(clazz);
  }
  
  protected AccessorFactory createAccessorFactory(Class clazz)
  {
    AccessorFactory accFactory = null;
    if (((RuntimeModelBuilder)this.builder).context.xmlAccessorFactorySupport)
    {
      XmlAccessorFactory factoryAnn = findXmlAccessorFactoryAnnotation(clazz);
      if (factoryAnn != null) {
        try
        {
          accFactory = (AccessorFactory)factoryAnn.value().newInstance();
        }
        catch (InstantiationException e)
        {
          this.builder.reportError(new IllegalAnnotationException(Messages.ACCESSORFACTORY_INSTANTIATION_EXCEPTION.format(new Object[] { factoryAnn.getClass().getName(), nav().getClassName(clazz) }), this));
        }
        catch (IllegalAccessException e)
        {
          this.builder.reportError(new IllegalAnnotationException(Messages.ACCESSORFACTORY_ACCESS_EXCEPTION.format(new Object[] { factoryAnn.getClass().getName(), nav().getClassName(clazz) }), this));
        }
      }
    }
    if (accFactory == null) {
      accFactory = AccessorFactoryImpl.getInstance();
    }
    return accFactory;
  }
  
  protected XmlAccessorFactory findXmlAccessorFactoryAnnotation(Class clazz)
  {
    XmlAccessorFactory factoryAnn = (XmlAccessorFactory)reader().getClassAnnotation(XmlAccessorFactory.class, clazz, this);
    if (factoryAnn == null) {
      factoryAnn = (XmlAccessorFactory)reader().getPackageAnnotation(XmlAccessorFactory.class, clazz, this);
    }
    return factoryAnn;
  }
  
  public Method getFactoryMethod()
  {
    return super.getFactoryMethod();
  }
  
  public final RuntimeClassInfoImpl getBaseClass()
  {
    return (RuntimeClassInfoImpl)super.getBaseClass();
  }
  
  protected ReferencePropertyInfoImpl createReferenceProperty(PropertySeed<Type, Class, Field, Method> seed)
  {
    return new RuntimeReferencePropertyInfoImpl(this, seed);
  }
  
  protected AttributePropertyInfoImpl createAttributeProperty(PropertySeed<Type, Class, Field, Method> seed)
  {
    return new RuntimeAttributePropertyInfoImpl(this, seed);
  }
  
  protected ValuePropertyInfoImpl createValueProperty(PropertySeed<Type, Class, Field, Method> seed)
  {
    return new RuntimeValuePropertyInfoImpl(this, seed);
  }
  
  protected ElementPropertyInfoImpl createElementProperty(PropertySeed<Type, Class, Field, Method> seed)
  {
    return new RuntimeElementPropertyInfoImpl(this, seed);
  }
  
  protected MapPropertyInfoImpl createMapProperty(PropertySeed<Type, Class, Field, Method> seed)
  {
    return new RuntimeMapPropertyInfoImpl(this, seed);
  }
  
  public List<? extends RuntimePropertyInfo> getProperties()
  {
    return super.getProperties();
  }
  
  public RuntimePropertyInfo getProperty(String name)
  {
    return (RuntimePropertyInfo)super.getProperty(name);
  }
  
  public void link()
  {
    getTransducer();
    super.link();
  }
  
  public <B> Accessor<B, Map<QName, String>> getAttributeWildcard()
  {
    for (RuntimeClassInfoImpl c = this; c != null; c = c.getBaseClass()) {
      if (c.attributeWildcard != null)
      {
        if (c.attributeWildcardAccessor == null) {
          c.attributeWildcardAccessor = c.createAttributeWildcardAccessor();
        }
        return c.attributeWildcardAccessor;
      }
    }
    return null;
  }
  
  private boolean computedTransducer = false;
  private Transducer xducer = null;
  
  public Transducer getTransducer()
  {
    if (!this.computedTransducer)
    {
      this.computedTransducer = true;
      this.xducer = calcTransducer();
    }
    return this.xducer;
  }
  
  private Transducer calcTransducer()
  {
    RuntimeValuePropertyInfo valuep = null;
    if (hasAttributeWildcard()) {
      return null;
    }
    for (RuntimeClassInfoImpl ci = this; ci != null; ci = ci.getBaseClass()) {
      for (RuntimePropertyInfo pi : ci.getProperties()) {
        if (pi.kind() == PropertyKind.VALUE) {
          valuep = (RuntimeValuePropertyInfo)pi;
        } else {
          return null;
        }
      }
    }
    if (valuep == null) {
      return null;
    }
    if (!valuep.getTarget().isSimpleType()) {
      return null;
    }
    return new TransducerImpl((Class)getClazz(), TransducedAccessor.get(((RuntimeModelBuilder)this.builder).context, valuep));
  }
  
  private Accessor<?, Map<QName, String>> createAttributeWildcardAccessor()
  {
    assert (this.attributeWildcard != null);
    return ((RuntimePropertySeed)this.attributeWildcard).getAccessor();
  }
  
  protected RuntimePropertySeed createFieldSeed(Field field)
  {
    boolean readOnly = Modifier.isStatic(field.getModifiers());
    Accessor acc;
    try
    {
      acc = this.accessorFactory.createFieldAccessor((Class)this.clazz, field, readOnly);
    }
    catch (JAXBException e)
    {
      this.builder.reportError(new IllegalAnnotationException(Messages.CUSTOM_ACCESSORFACTORY_FIELD_ERROR.format(new Object[] { nav().getClassName(this.clazz), e.toString() }), this));
      
      acc = Accessor.getErrorInstance();
    }
    return new RuntimePropertySeed(super.createFieldSeed(field), acc);
  }
  
  public RuntimePropertySeed createAccessorSeed(Method getter, Method setter)
  {
    Accessor acc;
    try
    {
      acc = this.accessorFactory.createPropertyAccessor((Class)this.clazz, getter, setter);
    }
    catch (JAXBException e)
    {
      this.builder.reportError(new IllegalAnnotationException(Messages.CUSTOM_ACCESSORFACTORY_PROPERTY_ERROR.format(new Object[] { nav().getClassName(this.clazz), e.toString() }), this));
      
      acc = Accessor.getErrorInstance();
    }
    return new RuntimePropertySeed(super.createAccessorSeed(getter, setter), acc);
  }
  
  protected void checkFieldXmlLocation(Field f)
  {
    if (reader().hasFieldAnnotation(XmlLocation.class, f)) {
      this.xmlLocationAccessor = new Accessor.FieldReflection(f);
    }
  }
  
  public Accessor<?, Locator> getLocatorField()
  {
    return this.xmlLocationAccessor;
  }
  
  static final class RuntimePropertySeed
    implements PropertySeed<Type, Class, Field, Method>
  {
    private final Accessor acc;
    private final PropertySeed<Type, Class, Field, Method> core;
    
    public RuntimePropertySeed(PropertySeed<Type, Class, Field, Method> core, Accessor acc)
    {
      this.core = core;
      this.acc = acc;
    }
    
    public String getName()
    {
      return this.core.getName();
    }
    
    public <A extends Annotation> A readAnnotation(Class<A> annotationType)
    {
      return this.core.readAnnotation(annotationType);
    }
    
    public boolean hasAnnotation(Class<? extends Annotation> annotationType)
    {
      return this.core.hasAnnotation(annotationType);
    }
    
    public Type getRawType()
    {
      return (Type)this.core.getRawType();
    }
    
    public Location getLocation()
    {
      return this.core.getLocation();
    }
    
    public Locatable getUpstream()
    {
      return this.core.getUpstream();
    }
    
    public Accessor getAccessor()
    {
      return this.acc;
    }
  }
  
  private static final class TransducerImpl<BeanT>
    implements Transducer<BeanT>
  {
    private final TransducedAccessor<BeanT> xacc;
    private final Class<BeanT> ownerClass;
    
    public TransducerImpl(Class<BeanT> ownerClass, TransducedAccessor<BeanT> xacc)
    {
      this.xacc = xacc;
      this.ownerClass = ownerClass;
    }
    
    public boolean useNamespace()
    {
      return this.xacc.useNamespace();
    }
    
    public boolean isDefault()
    {
      return false;
    }
    
    public void declareNamespace(BeanT bean, XMLSerializer w)
      throws AccessorException
    {
      try
      {
        this.xacc.declareNamespace(bean, w);
      }
      catch (SAXException e)
      {
        throw new AccessorException(e);
      }
    }
    
    @NotNull
    public CharSequence print(BeanT o)
      throws AccessorException
    {
      try
      {
        CharSequence value = this.xacc.print(o);
        if (value == null) {
          throw new AccessorException(Messages.THERE_MUST_BE_VALUE_IN_XMLVALUE.format(new Object[] { o }));
        }
        return value;
      }
      catch (SAXException e)
      {
        throw new AccessorException(e);
      }
    }
    
    public BeanT parse(CharSequence lexical)
      throws AccessorException, SAXException
    {
      UnmarshallingContext ctxt = UnmarshallingContext.getInstance();
      BeanT inst;
      BeanT inst;
      if (ctxt != null) {
        inst = ctxt.createInstance(this.ownerClass);
      } else {
        inst = ClassFactory.create(this.ownerClass);
      }
      this.xacc.parse(inst, lexical);
      return inst;
    }
    
    public void writeText(XMLSerializer w, BeanT o, String fieldName)
      throws IOException, SAXException, XMLStreamException, AccessorException
    {
      if (!this.xacc.hasValue(o)) {
        throw new AccessorException(Messages.THERE_MUST_BE_VALUE_IN_XMLVALUE.format(new Object[] { o }));
      }
      this.xacc.writeText(w, o, fieldName);
    }
    
    public void writeLeafElement(XMLSerializer w, Name tagName, BeanT o, String fieldName)
      throws IOException, SAXException, XMLStreamException, AccessorException
    {
      if (!this.xacc.hasValue(o)) {
        throw new AccessorException(Messages.THERE_MUST_BE_VALUE_IN_XMLVALUE.format(new Object[] { o }));
      }
      this.xacc.writeLeafElement(w, tagName, o, fieldName);
    }
    
    public QName getTypeName(BeanT instance)
    {
      return null;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\RuntimeClassInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */