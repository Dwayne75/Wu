package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Coordinator;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.xml.sax.SAXException;

final class AdaptedLister<BeanT, PropT, InMemItemT, OnWireItemT, PackT>
  extends Lister<BeanT, PropT, OnWireItemT, PackT>
{
  private final Lister<BeanT, PropT, InMemItemT, PackT> core;
  private final Class<? extends XmlAdapter<OnWireItemT, InMemItemT>> adapter;
  
  AdaptedLister(Lister<BeanT, PropT, InMemItemT, PackT> core, Class<? extends XmlAdapter<OnWireItemT, InMemItemT>> adapter)
  {
    this.core = core;
    this.adapter = adapter;
  }
  
  private XmlAdapter<OnWireItemT, InMemItemT> getAdapter()
  {
    return Coordinator._getInstance().getAdapter(this.adapter);
  }
  
  public ListIterator<OnWireItemT> iterator(PropT prop, XMLSerializer context)
  {
    return new ListIteratorImpl(this.core.iterator(prop, context), context);
  }
  
  public PackT startPacking(BeanT bean, Accessor<BeanT, PropT> accessor)
    throws AccessorException
  {
    return (PackT)this.core.startPacking(bean, accessor);
  }
  
  public void addToPack(PackT pack, OnWireItemT item)
    throws AccessorException
  {
    InMemItemT r;
    try
    {
      r = getAdapter().unmarshal(item);
    }
    catch (Exception e)
    {
      throw new AccessorException(e);
    }
    this.core.addToPack(pack, r);
  }
  
  public void endPacking(PackT pack, BeanT bean, Accessor<BeanT, PropT> accessor)
    throws AccessorException
  {
    this.core.endPacking(pack, bean, accessor);
  }
  
  public void reset(BeanT bean, Accessor<BeanT, PropT> accessor)
    throws AccessorException
  {
    this.core.reset(bean, accessor);
  }
  
  private final class ListIteratorImpl
    implements ListIterator<OnWireItemT>
  {
    private final ListIterator<InMemItemT> core;
    private final XMLSerializer serializer;
    
    public ListIteratorImpl(XMLSerializer core)
    {
      this.core = core;
      this.serializer = serializer;
    }
    
    public boolean hasNext()
    {
      return this.core.hasNext();
    }
    
    public OnWireItemT next()
      throws SAXException, JAXBException
    {
      InMemItemT next = this.core.next();
      try
      {
        return (OnWireItemT)AdaptedLister.this.getAdapter().marshal(next);
      }
      catch (Exception e)
      {
        this.serializer.reportError(null, e);
      }
      return null;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\AdaptedLister.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */