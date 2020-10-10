package com.sun.xml.bind.v2.runtime;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext.State;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class ValueListBeanInfoImpl
  extends JaxBeanInfo
{
  private final Class itemType;
  private final Transducer xducer;
  
  public ValueListBeanInfoImpl(JAXBContextImpl owner, Class arrayType)
    throws JAXBException
  {
    super(owner, null, arrayType, false, true, false);
    this.itemType = this.jaxbType.getComponentType();
    this.xducer = owner.getBeanInfo(arrayType.getComponentType(), true).getTransducer();
    assert (this.xducer != null);
  }
  
  private final Loader loader = new Loader(true)
  {
    public void text(UnmarshallingContext.State state, CharSequence text)
      throws SAXException
    {
      List<Object> r = new FinalArrayList();
      
      int idx = 0;
      int len = text.length();
      for (;;)
      {
        int p = idx;
        while ((p < len) && (!WhiteSpaceProcessor.isWhiteSpace(text.charAt(p)))) {
          p++;
        }
        CharSequence token = text.subSequence(idx, p);
        if (!token.equals(""))
        {
          try
          {
            r.add(ValueListBeanInfoImpl.this.xducer.parse(token));
          }
          catch (AccessorException e)
          {
            handleGenericException(e, true);
          }
          continue;
        }
        if (p == len) {
          break;
        }
        while ((p < len) && (WhiteSpaceProcessor.isWhiteSpace(text.charAt(p)))) {
          p++;
        }
        if (p == len) {
          break;
        }
        idx = p;
      }
      state.target = ValueListBeanInfoImpl.this.toArray(r);
    }
  };
  
  private Object toArray(List list)
  {
    int len = list.size();
    Object array = Array.newInstance(this.itemType, len);
    for (int i = 0; i < len; i++) {
      Array.set(array, i, list.get(i));
    }
    return array;
  }
  
  public void serializeBody(Object array, XMLSerializer target)
    throws SAXException, IOException, XMLStreamException
  {
    int len = Array.getLength(array);
    for (int i = 0; i < len; i++)
    {
      Object item = Array.get(array, i);
      try
      {
        this.xducer.writeText(target, item, "arrayItem");
      }
      catch (AccessorException e)
      {
        target.reportError("arrayItem", e);
      }
    }
  }
  
  public final void serializeURIs(Object array, XMLSerializer target)
    throws SAXException
  {
    if (this.xducer.useNamespace())
    {
      int len = Array.getLength(array);
      for (int i = 0; i < len; i++)
      {
        Object item = Array.get(array, i);
        try
        {
          this.xducer.declareNamespace(item, target);
        }
        catch (AccessorException e)
        {
          target.reportError("arrayItem", e);
        }
      }
    }
  }
  
  public final String getElementNamespaceURI(Object array)
  {
    throw new UnsupportedOperationException();
  }
  
  public final String getElementLocalName(Object array)
  {
    throw new UnsupportedOperationException();
  }
  
  public final Object createInstance(UnmarshallingContext context)
  {
    throw new UnsupportedOperationException();
  }
  
  public final boolean reset(Object array, UnmarshallingContext context)
  {
    return false;
  }
  
  public final String getId(Object array, XMLSerializer target)
  {
    return null;
  }
  
  public final void serializeAttributes(Object array, XMLSerializer target) {}
  
  public final void serializeRoot(Object array, XMLSerializer target)
    throws SAXException
  {
    target.reportError(new ValidationEventImpl(1, Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(new Object[] { array.getClass().getName() }), null, null));
  }
  
  public final Transducer getTransducer()
  {
    return null;
  }
  
  public final Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable)
  {
    return this.loader;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\ValueListBeanInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */