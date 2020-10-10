package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.runtime.RuntimeLeafInfo;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TextLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader;
import java.io.IOException;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class LeafBeanInfoImpl<BeanT>
  extends JaxBeanInfo<BeanT>
{
  private final Loader loader;
  private final Loader loaderWithSubst;
  private final Transducer<BeanT> xducer;
  private final Name tagName;
  
  public LeafBeanInfoImpl(JAXBContextImpl grammar, RuntimeLeafInfo li)
  {
    super(grammar, li, li.getClazz(), li.getTypeNames(), li.isElement(), true, false);
    
    this.xducer = li.getTransducer();
    this.loader = new TextLoader(this.xducer);
    this.loaderWithSubst = new XsiTypeLoader(this);
    if (isElement()) {
      this.tagName = grammar.nameBuilder.createElementName(li.getElementName());
    } else {
      this.tagName = null;
    }
  }
  
  public QName getTypeName(BeanT instance)
  {
    QName tn = this.xducer.getTypeName(instance);
    if (tn != null) {
      return tn;
    }
    return super.getTypeName(instance);
  }
  
  public final String getElementNamespaceURI(BeanT _)
  {
    return this.tagName.nsUri;
  }
  
  public final String getElementLocalName(BeanT _)
  {
    return this.tagName.localName;
  }
  
  public BeanT createInstance(UnmarshallingContext context)
  {
    throw new UnsupportedOperationException();
  }
  
  public final boolean reset(BeanT bean, UnmarshallingContext context)
  {
    return false;
  }
  
  public final String getId(BeanT bean, XMLSerializer target)
  {
    return null;
  }
  
  public final void serializeBody(BeanT bean, XMLSerializer w)
    throws SAXException, IOException, XMLStreamException
  {
    try
    {
      this.xducer.writeText(w, bean, null);
    }
    catch (AccessorException e)
    {
      w.reportError(null, e);
    }
  }
  
  public final void serializeAttributes(BeanT bean, XMLSerializer target) {}
  
  public final void serializeRoot(BeanT bean, XMLSerializer target)
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
  
  public final void serializeURIs(BeanT bean, XMLSerializer target)
    throws SAXException
  {
    if (this.xducer.useNamespace()) {
      try
      {
        this.xducer.declareNamespace(bean, target);
      }
      catch (AccessorException e)
      {
        target.reportError(null, e);
      }
    }
  }
  
  public final Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable)
  {
    if (typeSubstitutionCapable) {
      return this.loaderWithSubst;
    }
    return this.loader;
  }
  
  public Transducer<BeanT> getTransducer()
  {
    return this.xducer;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\LeafBeanInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */