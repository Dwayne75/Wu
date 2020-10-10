package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.NameBuilder;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.DefaultValueLoaderDecorator;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Single;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class SingleElementNodeProperty<BeanT, ValueT>
  extends PropertyImpl<BeanT>
{
  private final Accessor<BeanT, ValueT> acc;
  private final boolean nillable;
  private final QName[] acceptedElements;
  private final Map<Class, TagAndType> typeNames = new HashMap();
  private RuntimeElementPropertyInfo prop;
  private final Name nullTagName;
  
  public SingleElementNodeProperty(JAXBContextImpl context, RuntimeElementPropertyInfo prop)
  {
    super(context, prop);
    this.acc = prop.getAccessor().optimize(context);
    this.prop = prop;
    
    QName nt = null;
    boolean nil = false;
    
    this.acceptedElements = new QName[prop.getTypes().size()];
    for (int i = 0; i < this.acceptedElements.length; i++) {
      this.acceptedElements[i] = ((RuntimeTypeRef)prop.getTypes().get(i)).getTagName();
    }
    for (RuntimeTypeRef e : prop.getTypes())
    {
      JaxBeanInfo beanInfo = context.getOrCreate(e.getTarget());
      if (nt == null) {
        nt = e.getTagName();
      }
      this.typeNames.put(beanInfo.jaxbType, new TagAndType(context.nameBuilder.createElementName(e.getTagName()), beanInfo));
      
      nil |= e.isNillable();
    }
    this.nullTagName = context.nameBuilder.createElementName(nt);
    
    this.nillable = nil;
  }
  
  public void wrapUp()
  {
    super.wrapUp();
    this.prop = null;
  }
  
  public void reset(BeanT bean)
    throws AccessorException
  {
    this.acc.set(bean, null);
  }
  
  public String getIdValue(BeanT beanT)
  {
    return null;
  }
  
  public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer)
    throws SAXException, AccessorException, IOException, XMLStreamException
  {
    ValueT v = this.acc.get(o);
    if (v != null)
    {
      Class vtype = v.getClass();
      TagAndType tt = (TagAndType)this.typeNames.get(vtype);
      if (tt == null) {
        for (Map.Entry<Class, TagAndType> e : this.typeNames.entrySet()) {
          if (((Class)e.getKey()).isAssignableFrom(vtype))
          {
            tt = (TagAndType)e.getValue();
            break;
          }
        }
      }
      if (tt == null)
      {
        w.startElement(((TagAndType)this.typeNames.values().iterator().next()).tagName, null);
        w.childAsXsiType(v, this.fieldName, w.grammar.getBeanInfo(Object.class));
      }
      else
      {
        w.startElement(tt.tagName, null);
        w.childAsXsiType(v, this.fieldName, tt.beanInfo);
      }
      w.endElement();
    }
    else if (this.nillable)
    {
      w.startElement(this.nullTagName, null);
      w.writeXsiNilTrue();
      w.endElement();
    }
  }
  
  public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers)
  {
    JAXBContextImpl context = chain.context;
    for (TypeRef<Type, Class> e : this.prop.getTypes())
    {
      JaxBeanInfo bi = context.getOrCreate((RuntimeTypeInfo)e.getTarget());
      
      Loader l = bi.getLoader(context, !Modifier.isFinal(bi.jaxbType.getModifiers()));
      if (e.getDefaultValue() != null) {
        l = new DefaultValueLoaderDecorator(l, e.getDefaultValue());
      }
      if ((this.nillable) || (chain.context.allNillable)) {
        l = new XsiNilLoader.Single(l, this.acc);
      }
      handlers.put(e.getTagName(), new ChildLoader(l, this.acc));
    }
  }
  
  public PropertyKind getKind()
  {
    return PropertyKind.ELEMENT;
  }
  
  public Accessor getElementPropertyAccessor(String nsUri, String localName)
  {
    for (QName n : this.acceptedElements) {
      if ((n.getNamespaceURI().equals(nsUri)) && (n.getLocalPart().equals(localName))) {
        return this.acc;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\property\SingleElementNodeProperty.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */