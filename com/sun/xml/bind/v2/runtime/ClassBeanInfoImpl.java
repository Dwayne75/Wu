package com.sun.xml.bind.v2.runtime;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.Util;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.property.AttributeProperty;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.property.PropertyFactory;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.StructureLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

public final class ClassBeanInfoImpl<BeanT>
  extends JaxBeanInfo<BeanT>
{
  public final Property<BeanT>[] properties;
  private Property<? super BeanT> idProperty;
  private Loader loader;
  private Loader loaderWithTypeSubst;
  private RuntimeClassInfo ci;
  private final Accessor<? super BeanT, Map<QName, String>> inheritedAttWildcard;
  private final Transducer<BeanT> xducer;
  public final ClassBeanInfoImpl<? super BeanT> superClazz;
  private final Accessor<? super BeanT, Locator> xmlLocatorField;
  private final Name tagName;
  private AttributeProperty<BeanT>[] attributeProperties;
  private Property<BeanT>[] uriProperties;
  private final Method factoryMethod;
  
  ClassBeanInfoImpl(JAXBContextImpl owner, RuntimeClassInfo ci)
  {
    super(owner, ci, (Class)ci.getClazz(), ci.getTypeName(), ci.isElement(), false, true);
    
    this.ci = ci;
    this.inheritedAttWildcard = ci.getAttributeWildcard();
    this.xducer = ci.getTransducer();
    this.factoryMethod = ci.getFactoryMethod();
    if (this.factoryMethod != null)
    {
      int classMod = this.factoryMethod.getDeclaringClass().getModifiers();
      if ((!Modifier.isPublic(classMod)) || (!Modifier.isPublic(this.factoryMethod.getModifiers()))) {
        try
        {
          this.factoryMethod.setAccessible(true);
        }
        catch (SecurityException e)
        {
          logger.log(Level.FINE, "Unable to make the method of " + this.factoryMethod + " accessible", e);
          throw e;
        }
      }
    }
    if (ci.getBaseClass() == null) {
      this.superClazz = null;
    } else {
      this.superClazz = owner.getOrCreate(ci.getBaseClass());
    }
    if ((this.superClazz != null) && (this.superClazz.xmlLocatorField != null)) {
      this.xmlLocatorField = this.superClazz.xmlLocatorField;
    } else {
      this.xmlLocatorField = ci.getLocatorField();
    }
    Collection<? extends RuntimePropertyInfo> ps = ci.getProperties();
    this.properties = new Property[ps.size()];
    int idx = 0;
    boolean elementOnly = true;
    for (RuntimePropertyInfo info : ps)
    {
      Property p = PropertyFactory.create(owner, info);
      if (info.id() == ID.ID) {
        this.idProperty = p;
      }
      this.properties[(idx++)] = p;
      elementOnly &= info.elementOnlyContent();
    }
    hasElementOnlyContentModel(elementOnly);
    if (ci.isElement()) {
      this.tagName = owner.nameBuilder.createElementName(ci.getElementName());
    } else {
      this.tagName = null;
    }
    setLifecycleFlags();
  }
  
  protected void link(JAXBContextImpl grammar)
  {
    if (this.uriProperties != null) {
      return;
    }
    super.link(grammar);
    if (this.superClazz != null) {
      this.superClazz.link(grammar);
    }
    getLoader(grammar, true);
    if (this.superClazz != null)
    {
      if (this.idProperty == null) {
        this.idProperty = this.superClazz.idProperty;
      }
      if (!this.superClazz.hasElementOnlyContentModel()) {
        hasElementOnlyContentModel(false);
      }
    }
    List<AttributeProperty> attProps = new FinalArrayList();
    List<Property> uriProps = new FinalArrayList();
    for (ClassBeanInfoImpl bi = this; bi != null; bi = bi.superClazz) {
      for (int i = bi.properties.length - 1; i >= 0; i--)
      {
        Property p = bi.properties[i];
        if ((p instanceof AttributeProperty)) {
          attProps.add((AttributeProperty)p);
        }
        if (p.hasSerializeURIAction()) {
          uriProps.add(p);
        }
      }
    }
    if (grammar.c14nSupport) {
      Collections.sort(attProps);
    }
    if (attProps.isEmpty()) {
      this.attributeProperties = EMPTY_PROPERTIES;
    } else {
      this.attributeProperties = ((AttributeProperty[])attProps.toArray(new AttributeProperty[attProps.size()]));
    }
    if (uriProps.isEmpty()) {
      this.uriProperties = EMPTY_PROPERTIES;
    } else {
      this.uriProperties = ((Property[])uriProps.toArray(new Property[uriProps.size()]));
    }
  }
  
  public void wrapUp()
  {
    for (Property p : this.properties) {
      p.wrapUp();
    }
    this.ci = null;
    super.wrapUp();
  }
  
  public String getElementNamespaceURI(BeanT bean)
  {
    return this.tagName.nsUri;
  }
  
  public String getElementLocalName(BeanT bean)
  {
    return this.tagName.localName;
  }
  
  public BeanT createInstance(UnmarshallingContext context)
    throws IllegalAccessException, InvocationTargetException, InstantiationException, SAXException
  {
    BeanT bean = null;
    if (this.factoryMethod == null)
    {
      bean = ClassFactory.create0(this.jaxbType);
    }
    else
    {
      Object o = ClassFactory.create(this.factoryMethod);
      if (this.jaxbType.isInstance(o)) {
        bean = (BeanT)o;
      } else {
        throw new InstantiationException("The factory method didn't return a correct object");
      }
    }
    if (this.xmlLocatorField != null) {
      try
      {
        this.xmlLocatorField.set(bean, new LocatorImpl(context.getLocator()));
      }
      catch (AccessorException e)
      {
        context.handleError(e);
      }
    }
    return bean;
  }
  
  public boolean reset(BeanT bean, UnmarshallingContext context)
    throws SAXException
  {
    try
    {
      if (this.superClazz != null) {
        this.superClazz.reset(bean, context);
      }
      for (Property<BeanT> p : this.properties) {
        p.reset(bean);
      }
      return true;
    }
    catch (AccessorException e)
    {
      context.handleError(e);
    }
    return false;
  }
  
  public String getId(BeanT bean, XMLSerializer target)
    throws SAXException
  {
    if (this.idProperty != null) {
      try
      {
        return this.idProperty.getIdValue(bean);
      }
      catch (AccessorException e)
      {
        target.reportError(null, e);
      }
    }
    return null;
  }
  
  public void serializeRoot(BeanT bean, XMLSerializer target)
    throws SAXException, IOException, XMLStreamException
  {
    if (this.tagName == null)
    {
      target.reportError(new ValidationEventImpl(1, Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(new Object[] { bean.getClass().getName() }), null, null));
    }
    else
    {
      target.startElement(this.tagName, bean);
      target.childAsSoleContent(bean, null);
      target.endElement();
    }
  }
  
  public void serializeBody(BeanT bean, XMLSerializer target)
    throws SAXException, IOException, XMLStreamException
  {
    if (this.superClazz != null) {
      this.superClazz.serializeBody(bean, target);
    }
    try
    {
      for (Property<BeanT> p : this.properties) {
        p.serializeBody(bean, target, null);
      }
    }
    catch (AccessorException e)
    {
      target.reportError(null, e);
    }
  }
  
  public void serializeAttributes(BeanT bean, XMLSerializer target)
    throws SAXException, IOException, XMLStreamException
  {
    for (AttributeProperty<BeanT> p : this.attributeProperties) {
      try
      {
        p.serializeAttributes(bean, target);
      }
      catch (AccessorException e)
      {
        target.reportError(null, e);
      }
    }
    try
    {
      if (this.inheritedAttWildcard != null)
      {
        Map<QName, String> map = (Map)this.inheritedAttWildcard.get(bean);
        target.attWildcardAsAttributes(map, null);
      }
    }
    catch (AccessorException e)
    {
      target.reportError(null, e);
    }
  }
  
  public void serializeURIs(BeanT bean, XMLSerializer target)
    throws SAXException
  {
    try
    {
      for (Property<BeanT> p : this.uriProperties) {
        p.serializeURIs(bean, target);
      }
      if (this.inheritedAttWildcard != null)
      {
        Map<QName, String> map = (Map)this.inheritedAttWildcard.get(bean);
        target.attWildcardAsURIs(map, null);
      }
    }
    catch (AccessorException e)
    {
      target.reportError(null, e);
    }
  }
  
  public Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable)
  {
    if (this.loader == null)
    {
      StructureLoader sl = new StructureLoader(this);
      this.loader = sl;
      if (this.ci.hasSubClasses()) {
        this.loaderWithTypeSubst = new XsiTypeLoader(this);
      } else {
        this.loaderWithTypeSubst = this.loader;
      }
      sl.init(context, this, this.ci.getAttributeWildcard());
    }
    if (typeSubstitutionCapable) {
      return this.loaderWithTypeSubst;
    }
    return this.loader;
  }
  
  public Transducer<BeanT> getTransducer()
  {
    return this.xducer;
  }
  
  private static final AttributeProperty[] EMPTY_PROPERTIES = new AttributeProperty[0];
  private static final Logger logger = Util.getClassLogger();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\ClassBeanInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */