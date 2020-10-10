package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.bind.Util;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public abstract class JaxBeanInfo<BeanT>
{
  protected short flag;
  private static final short FLAG_IS_ELEMENT = 1;
  private static final short FLAG_IS_IMMUTABLE = 2;
  private static final short FLAG_HAS_ELEMENT_ONLY_CONTENTMODEL = 4;
  private static final short FLAG_HAS_BEFORE_UNMARSHAL_METHOD = 8;
  private static final short FLAG_HAS_AFTER_UNMARSHAL_METHOD = 16;
  private static final short FLAG_HAS_BEFORE_MARSHAL_METHOD = 32;
  private static final short FLAG_HAS_AFTER_MARSHAL_METHOD = 64;
  private static final short FLAG_HAS_LIFECYCLE_EVENTS = 128;
  
  protected JaxBeanInfo(JAXBContextImpl grammar, RuntimeTypeInfo rti, Class<BeanT> jaxbType, QName[] typeNames, boolean isElement, boolean isImmutable, boolean hasLifecycleEvents)
  {
    this(grammar, rti, jaxbType, typeNames, isElement, isImmutable, hasLifecycleEvents);
  }
  
  protected JaxBeanInfo(JAXBContextImpl grammar, RuntimeTypeInfo rti, Class<BeanT> jaxbType, QName typeName, boolean isElement, boolean isImmutable, boolean hasLifecycleEvents)
  {
    this(grammar, rti, jaxbType, typeName, isElement, isImmutable, hasLifecycleEvents);
  }
  
  protected JaxBeanInfo(JAXBContextImpl grammar, RuntimeTypeInfo rti, Class<BeanT> jaxbType, boolean isElement, boolean isImmutable, boolean hasLifecycleEvents)
  {
    this(grammar, rti, jaxbType, (Object)null, isElement, isImmutable, hasLifecycleEvents);
  }
  
  private JaxBeanInfo(JAXBContextImpl grammar, RuntimeTypeInfo rti, Class<BeanT> jaxbType, Object typeName, boolean isElement, boolean isImmutable, boolean hasLifecycleEvents)
  {
    grammar.beanInfos.put(rti, this);
    
    this.jaxbType = jaxbType;
    this.typeName = typeName;
    this.flag = ((short)((isElement ? 1 : 0) | (isImmutable ? 2 : 0) | (hasLifecycleEvents ? 128 : 0)));
  }
  
  private LifecycleMethods lcm = null;
  public final Class<BeanT> jaxbType;
  private final Object typeName;
  
  public final boolean hasBeforeUnmarshalMethod()
  {
    return (this.flag & 0x8) != 0;
  }
  
  public final boolean hasAfterUnmarshalMethod()
  {
    return (this.flag & 0x10) != 0;
  }
  
  public final boolean hasBeforeMarshalMethod()
  {
    return (this.flag & 0x20) != 0;
  }
  
  public final boolean hasAfterMarshalMethod()
  {
    return (this.flag & 0x40) != 0;
  }
  
  public final boolean isElement()
  {
    return (this.flag & 0x1) != 0;
  }
  
  public final boolean isImmutable()
  {
    return (this.flag & 0x2) != 0;
  }
  
  public final boolean hasElementOnlyContentModel()
  {
    return (this.flag & 0x4) != 0;
  }
  
  protected final void hasElementOnlyContentModel(boolean value)
  {
    if (value) {
      this.flag = ((short)(this.flag | 0x4));
    } else {
      this.flag = ((short)(this.flag & 0xFFFFFFFB));
    }
  }
  
  public boolean lookForLifecycleMethods()
  {
    return (this.flag & 0x80) != 0;
  }
  
  public abstract String getElementNamespaceURI(BeanT paramBeanT);
  
  public abstract String getElementLocalName(BeanT paramBeanT);
  
  public Collection<QName> getTypeNames()
  {
    if (this.typeName == null) {
      return Collections.emptyList();
    }
    if ((this.typeName instanceof QName)) {
      return Collections.singletonList((QName)this.typeName);
    }
    return Arrays.asList((QName[])this.typeName);
  }
  
  public QName getTypeName(@NotNull BeanT instance)
  {
    if (this.typeName == null) {
      return null;
    }
    if ((this.typeName instanceof QName)) {
      return (QName)this.typeName;
    }
    return ((QName[])(QName[])this.typeName)[0];
  }
  
  private static final Class[] unmarshalEventParams = { Unmarshaller.class, Object.class };
  private static Class[] marshalEventParams = { Marshaller.class };
  
  public abstract BeanT createInstance(UnmarshallingContext paramUnmarshallingContext)
    throws IllegalAccessException, InvocationTargetException, InstantiationException, SAXException;
  
  public abstract boolean reset(BeanT paramBeanT, UnmarshallingContext paramUnmarshallingContext)
    throws SAXException;
  
  public abstract String getId(BeanT paramBeanT, XMLSerializer paramXMLSerializer)
    throws SAXException;
  
  public abstract void serializeBody(BeanT paramBeanT, XMLSerializer paramXMLSerializer)
    throws SAXException, IOException, XMLStreamException;
  
  public abstract void serializeAttributes(BeanT paramBeanT, XMLSerializer paramXMLSerializer)
    throws SAXException, IOException, XMLStreamException;
  
  public abstract void serializeRoot(BeanT paramBeanT, XMLSerializer paramXMLSerializer)
    throws SAXException, IOException, XMLStreamException;
  
  public abstract void serializeURIs(BeanT paramBeanT, XMLSerializer paramXMLSerializer)
    throws SAXException;
  
  public abstract Loader getLoader(JAXBContextImpl paramJAXBContextImpl, boolean paramBoolean);
  
  public abstract Transducer<BeanT> getTransducer();
  
  protected void link(JAXBContextImpl grammar) {}
  
  public void wrapUp() {}
  
  protected final void setLifecycleFlags()
  {
    try
    {
      for (Method m : this.jaxbType.getDeclaredMethods())
      {
        String name = m.getName();
        if (name.equals("beforeUnmarshal"))
        {
          if (match(m, unmarshalEventParams)) {
            cacheLifecycleMethod(m, (short)8);
          }
        }
        else if (name.equals("afterUnmarshal"))
        {
          if (match(m, unmarshalEventParams)) {
            cacheLifecycleMethod(m, (short)16);
          }
        }
        else if (name.equals("beforeMarshal"))
        {
          if (match(m, marshalEventParams)) {
            cacheLifecycleMethod(m, (short)32);
          }
        }
        else if ((name.equals("afterMarshal")) && 
          (match(m, marshalEventParams))) {
          cacheLifecycleMethod(m, (short)64);
        }
      }
    }
    catch (SecurityException e)
    {
      logger.log(Level.WARNING, Messages.UNABLE_TO_DISCOVER_EVENTHANDLER.format(new Object[] { this.jaxbType.getName(), e }));
    }
  }
  
  private boolean match(Method m, Class[] params)
  {
    return Arrays.equals(m.getParameterTypes(), params);
  }
  
  private void cacheLifecycleMethod(Method m, short lifecycleFlag)
  {
    if (this.lcm == null) {
      this.lcm = new LifecycleMethods();
    }
    m.setAccessible(true);
    
    this.flag = ((short)(this.flag | lifecycleFlag));
    switch (lifecycleFlag)
    {
    case 8: 
      this.lcm.beforeUnmarshal = m;
      break;
    case 16: 
      this.lcm.afterUnmarshal = m;
      break;
    case 32: 
      this.lcm.beforeMarshal = m;
      break;
    case 64: 
      this.lcm.afterMarshal = m;
    }
  }
  
  public final LifecycleMethods getLifecycleMethods()
  {
    return this.lcm;
  }
  
  public final void invokeBeforeUnmarshalMethod(UnmarshallerImpl unm, Object child, Object parent)
    throws SAXException
  {
    Method m = getLifecycleMethods().beforeUnmarshal;
    invokeUnmarshallCallback(m, child, unm, parent);
  }
  
  public final void invokeAfterUnmarshalMethod(UnmarshallerImpl unm, Object child, Object parent)
    throws SAXException
  {
    Method m = getLifecycleMethods().afterUnmarshal;
    invokeUnmarshallCallback(m, child, unm, parent);
  }
  
  private void invokeUnmarshallCallback(Method m, Object child, UnmarshallerImpl unm, Object parent)
    throws SAXException
  {
    try
    {
      m.invoke(child, new Object[] { unm, parent });
    }
    catch (IllegalAccessException e)
    {
      UnmarshallingContext.getInstance().handleError(e);
    }
    catch (InvocationTargetException e)
    {
      UnmarshallingContext.getInstance().handleError(e);
    }
  }
  
  private static final Logger logger = Util.getClassLogger();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\JaxBeanInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */