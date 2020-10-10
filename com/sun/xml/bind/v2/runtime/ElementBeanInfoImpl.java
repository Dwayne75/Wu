package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.property.PropertyFactory;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Discarder;
import com.sun.xml.bind.v2.runtime.unmarshaller.Intercepter;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext.State;
import com.sun.xml.bind.v2.util.QNameMap;
import com.sun.xml.bind.v2.util.QNameMap.Entry;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBElement.GlobalScope;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class ElementBeanInfoImpl
  extends JaxBeanInfo<JAXBElement>
{
  private Loader loader;
  private final Property property;
  private final QName tagName;
  public final Class expectedType;
  private final Class scope;
  private final Constructor<? extends JAXBElement> constructor;
  
  ElementBeanInfoImpl(JAXBContextImpl grammar, RuntimeElementInfo rei)
  {
    super(grammar, rei, rei.getType(), true, false, true);
    
    this.property = PropertyFactory.create(grammar, rei.getProperty());
    
    this.tagName = rei.getElementName();
    this.expectedType = Navigator.REFLECTION.erasure((Type)rei.getContentInMemoryType());
    this.scope = (rei.getScope() == null ? JAXBElement.GlobalScope.class : (Class)rei.getScope().getClazz());
    
    Class type = Navigator.REFLECTION.erasure(rei.getType());
    if (type == JAXBElement.class) {
      this.constructor = null;
    } else {
      try
      {
        this.constructor = type.getConstructor(new Class[] { this.expectedType });
      }
      catch (NoSuchMethodException e)
      {
        NoSuchMethodError x = new NoSuchMethodError("Failed to find the constructor for " + type + " with " + this.expectedType);
        x.initCause(e);
        throw x;
      }
    }
  }
  
  protected ElementBeanInfoImpl(final JAXBContextImpl grammar)
  {
    super(grammar, null, JAXBElement.class, true, false, true);
    this.tagName = null;
    this.expectedType = null;
    this.scope = null;
    this.constructor = null;
    
    this.property = new Property()
    {
      public void reset(JAXBElement o)
      {
        throw new UnsupportedOperationException();
      }
      
      public void serializeBody(JAXBElement e, XMLSerializer target, Object outerPeer)
        throws SAXException, IOException, XMLStreamException
      {
        Class scope = e.getScope();
        if (e.isGlobalScope()) {
          scope = null;
        }
        QName n = e.getName();
        ElementBeanInfoImpl bi = grammar.getElement(scope, n);
        if (bi == null)
        {
          JaxBeanInfo tbi;
          try
          {
            tbi = grammar.getBeanInfo(e.getDeclaredType(), true);
          }
          catch (JAXBException x)
          {
            target.reportError(null, x);
            return;
          }
          Object value = e.getValue();
          target.startElement(n.getNamespaceURI(), n.getLocalPart(), n.getPrefix(), null);
          if (value == null) {
            target.writeXsiNilTrue();
          } else {
            target.childAsXsiType(value, "value", tbi);
          }
          target.endElement();
        }
        else
        {
          try
          {
            bi.property.serializeBody(e, target, e);
          }
          catch (AccessorException x)
          {
            target.reportError(null, x);
          }
        }
      }
      
      public void serializeURIs(JAXBElement o, XMLSerializer target) {}
      
      public boolean hasSerializeURIAction()
      {
        return false;
      }
      
      public String getIdValue(JAXBElement o)
      {
        return null;
      }
      
      public PropertyKind getKind()
      {
        return PropertyKind.ELEMENT;
      }
      
      public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers) {}
      
      public Accessor getElementPropertyAccessor(String nsUri, String localName)
      {
        throw new UnsupportedOperationException();
      }
      
      public void wrapUp() {}
    };
  }
  
  private final class IntercepterLoader
    extends Loader
    implements Intercepter
  {
    private final Loader core;
    
    public IntercepterLoader(Loader core)
    {
      this.core = core;
    }
    
    public final void startElement(UnmarshallingContext.State state, TagName ea)
      throws SAXException
    {
      state.loader = this.core;
      state.intercepter = this;
      
      UnmarshallingContext context = state.getContext();
      
      Object child = context.getOuterPeer();
      if ((child != null) && (ElementBeanInfoImpl.this.jaxbType != child.getClass())) {
        child = null;
      }
      if (child != null) {
        ElementBeanInfoImpl.this.reset((JAXBElement)child, context);
      }
      if (child == null) {
        child = context.createInstance(ElementBeanInfoImpl.this);
      }
      fireBeforeUnmarshal(ElementBeanInfoImpl.this, child, state);
      
      context.recordOuterPeer(child);
      UnmarshallingContext.State p = state.prev;
      p.backup = p.target;
      p.target = child;
      
      this.core.startElement(state, ea);
    }
    
    public Object intercept(UnmarshallingContext.State state, Object o)
      throws SAXException
    {
      JAXBElement e = (JAXBElement)state.target;
      state.target = state.backup;
      state.backup = null;
      if (o != null) {
        e.setValue(o);
      }
      fireAfterUnmarshal(ElementBeanInfoImpl.this, e, state);
      
      return e;
    }
  }
  
  public String getElementNamespaceURI(JAXBElement e)
  {
    return e.getName().getNamespaceURI();
  }
  
  public String getElementLocalName(JAXBElement e)
  {
    return e.getName().getLocalPart();
  }
  
  public Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable)
  {
    if (this.loader == null)
    {
      UnmarshallerChain c = new UnmarshallerChain(context);
      QNameMap<ChildLoader> result = new QNameMap();
      this.property.buildChildElementUnmarshallers(c, result);
      if (result.size() == 1) {
        this.loader = new IntercepterLoader(((ChildLoader)result.getOne().getValue()).loader);
      } else {
        this.loader = Discarder.INSTANCE;
      }
    }
    return this.loader;
  }
  
  public final JAXBElement createInstance(UnmarshallingContext context)
    throws IllegalAccessException, InvocationTargetException, InstantiationException
  {
    return createInstanceFromValue(null);
  }
  
  public final JAXBElement createInstanceFromValue(Object o)
    throws IllegalAccessException, InvocationTargetException, InstantiationException
  {
    if (this.constructor == null) {
      return new JAXBElement(this.tagName, this.expectedType, this.scope, o);
    }
    return (JAXBElement)this.constructor.newInstance(new Object[] { o });
  }
  
  public boolean reset(JAXBElement e, UnmarshallingContext context)
  {
    e.setValue(null);
    return true;
  }
  
  public String getId(JAXBElement e, XMLSerializer target)
  {
    Object o = e.getValue();
    if ((o instanceof String)) {
      return (String)o;
    }
    return null;
  }
  
  public void serializeBody(JAXBElement element, XMLSerializer target)
    throws SAXException, IOException, XMLStreamException
  {
    try
    {
      this.property.serializeBody(element, target, null);
    }
    catch (AccessorException x)
    {
      target.reportError(null, x);
    }
  }
  
  public void serializeRoot(JAXBElement e, XMLSerializer target)
    throws SAXException, IOException, XMLStreamException
  {
    serializeBody(e, target);
  }
  
  public void serializeAttributes(JAXBElement e, XMLSerializer target) {}
  
  public void serializeURIs(JAXBElement e, XMLSerializer target) {}
  
  public final Transducer<JAXBElement> getTransducer()
  {
    return null;
  }
  
  public void wrapUp()
  {
    super.wrapUp();
    this.property.wrapUp();
  }
  
  public void link(JAXBContextImpl grammar)
  {
    super.link(grammar);
    getLoader(grammar, true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\ElementBeanInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */