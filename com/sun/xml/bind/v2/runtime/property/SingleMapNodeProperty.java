package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeMapPropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.NameBuilder;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext.State;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class SingleMapNodeProperty<BeanT, ValueT extends Map>
  extends PropertyImpl<BeanT>
{
  private final Accessor<BeanT, ValueT> acc;
  private final Name tagName;
  private final Name entryTag;
  private final Name keyTag;
  private final Name valueTag;
  private final boolean nillable;
  private JaxBeanInfo keyBeanInfo;
  private JaxBeanInfo valueBeanInfo;
  private final Class<? extends ValueT> mapImplClass;
  
  public SingleMapNodeProperty(JAXBContextImpl context, RuntimeMapPropertyInfo prop)
  {
    super(context, prop);
    this.acc = prop.getAccessor().optimize(context);
    this.tagName = context.nameBuilder.createElementName(prop.getXmlName());
    this.entryTag = context.nameBuilder.createElementName("", "entry");
    this.keyTag = context.nameBuilder.createElementName("", "key");
    this.valueTag = context.nameBuilder.createElementName("", "value");
    this.nillable = prop.isCollectionNillable();
    this.keyBeanInfo = context.getOrCreate(prop.getKeyType());
    this.valueBeanInfo = context.getOrCreate(prop.getValueType());
    
    Class<ValueT> sig = ReflectionNavigator.REFLECTION.erasure(prop.getRawType());
    this.mapImplClass = ClassFactory.inferImplClass(sig, knownImplClasses);
  }
  
  private static final Class[] knownImplClasses = { HashMap.class, TreeMap.class, LinkedHashMap.class };
  private Loader keyLoader;
  private Loader valueLoader;
  
  public void reset(BeanT bean)
    throws AccessorException
  {
    this.acc.set(bean, null);
  }
  
  public String getIdValue(BeanT bean)
  {
    return null;
  }
  
  public PropertyKind getKind()
  {
    return PropertyKind.MAP;
  }
  
  public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers)
  {
    this.keyLoader = this.keyBeanInfo.getLoader(chain.context, true);
    this.valueLoader = this.valueBeanInfo.getLoader(chain.context, true);
    handlers.put(this.tagName, new ChildLoader(this.itemsLoader, null));
  }
  
  private final Loader itemsLoader = new Loader(false)
  {
    public void startElement(UnmarshallingContext.State state, TagName ea)
      throws SAXException
    {
      try
      {
        BeanT target = state.prev.target;
        ValueT map = (Map)SingleMapNodeProperty.this.acc.get(target);
        if (map == null)
        {
          map = (Map)ClassFactory.create(SingleMapNodeProperty.this.mapImplClass);
          SingleMapNodeProperty.this.acc.set(target, map);
        }
        map.clear();
        state.target = map;
      }
      catch (AccessorException e)
      {
        handleGenericException(e, true);
        state.target = new HashMap();
      }
    }
    
    public void childElement(UnmarshallingContext.State state, TagName ea)
      throws SAXException
    {
      if (ea.matches(SingleMapNodeProperty.this.entryTag)) {
        state.loader = SingleMapNodeProperty.this.entryLoader;
      } else {
        super.childElement(state, ea);
      }
    }
    
    public Collection<QName> getExpectedChildElements()
    {
      return Collections.singleton(SingleMapNodeProperty.this.entryTag.toQName());
    }
  };
  private final Loader entryLoader = new Loader(false)
  {
    public void startElement(UnmarshallingContext.State state, TagName ea)
    {
      state.target = new Object[2];
    }
    
    public void leaveElement(UnmarshallingContext.State state, TagName ea)
    {
      Object[] keyValue = (Object[])state.target;
      Map map = (Map)state.prev.target;
      map.put(keyValue[0], keyValue[1]);
    }
    
    public void childElement(UnmarshallingContext.State state, TagName ea)
      throws SAXException
    {
      if (ea.matches(SingleMapNodeProperty.this.keyTag))
      {
        state.loader = SingleMapNodeProperty.this.keyLoader;
        state.receiver = SingleMapNodeProperty.keyReceiver;
        return;
      }
      if (ea.matches(SingleMapNodeProperty.this.valueTag))
      {
        state.loader = SingleMapNodeProperty.this.valueLoader;
        state.receiver = SingleMapNodeProperty.valueReceiver;
        return;
      }
      super.childElement(state, ea);
    }
    
    public Collection<QName> getExpectedChildElements()
    {
      return Arrays.asList(new QName[] { SingleMapNodeProperty.this.keyTag.toQName(), SingleMapNodeProperty.this.valueTag.toQName() });
    }
  };
  
  private static final class ReceiverImpl
    implements Receiver
  {
    private final int index;
    
    public ReceiverImpl(int index)
    {
      this.index = index;
    }
    
    public void receive(UnmarshallingContext.State state, Object o)
    {
      ((Object[])state.target)[this.index] = o;
    }
  }
  
  private static final Receiver keyReceiver = new ReceiverImpl(0);
  private static final Receiver valueReceiver = new ReceiverImpl(1);
  
  public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer)
    throws SAXException, AccessorException, IOException, XMLStreamException
  {
    ValueT v = (Map)this.acc.get(o);
    if (v != null)
    {
      bareStartTag(w, this.tagName, v);
      for (Map.Entry e : v.entrySet())
      {
        bareStartTag(w, this.entryTag, null);
        
        Object key = e.getKey();
        if (key != null)
        {
          w.startElement(this.keyTag, key);
          w.childAsXsiType(key, this.fieldName, this.keyBeanInfo);
          w.endElement();
        }
        Object value = e.getValue();
        if (value != null)
        {
          w.startElement(this.valueTag, value);
          w.childAsXsiType(value, this.fieldName, this.valueBeanInfo);
          w.endElement();
        }
        w.endElement();
      }
      w.endElement();
    }
    else if (this.nillable)
    {
      w.startElement(this.tagName, null);
      w.writeXsiNilTrue();
      w.endElement();
    }
  }
  
  private void bareStartTag(XMLSerializer w, Name tagName, Object peer)
    throws IOException, XMLStreamException, SAXException
  {
    w.startElement(tagName, peer);
    w.endNamespaceDecls(peer);
    w.endAttributes();
  }
  
  public Accessor getElementPropertyAccessor(String nsUri, String localName)
  {
    if (this.tagName.equals(nsUri, localName)) {
      return this.acc;
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\property\SingleMapNodeProperty.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */