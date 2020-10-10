package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.Coordinator;
import javax.xml.bind.annotation.adapters.XmlAdapter;

final class AdaptedAccessor<BeanT, InMemValueT, OnWireValueT>
  extends Accessor<BeanT, OnWireValueT>
{
  private final Accessor<BeanT, InMemValueT> core;
  private final Class<? extends XmlAdapter<OnWireValueT, InMemValueT>> adapter;
  private XmlAdapter<OnWireValueT, InMemValueT> staticAdapter;
  
  AdaptedAccessor(Class<OnWireValueT> targetType, Accessor<BeanT, InMemValueT> extThis, Class<? extends XmlAdapter<OnWireValueT, InMemValueT>> adapter)
  {
    super(targetType);
    this.core = extThis;
    this.adapter = adapter;
  }
  
  public boolean isAdapted()
  {
    return true;
  }
  
  public OnWireValueT get(BeanT bean)
    throws AccessorException
  {
    InMemValueT v = this.core.get(bean);
    
    XmlAdapter<OnWireValueT, InMemValueT> a = getAdapter();
    try
    {
      return (OnWireValueT)a.marshal(v);
    }
    catch (Exception e)
    {
      throw new AccessorException(e);
    }
  }
  
  public void set(BeanT bean, OnWireValueT o)
    throws AccessorException
  {
    XmlAdapter<OnWireValueT, InMemValueT> a = getAdapter();
    try
    {
      this.core.set(bean, a.unmarshal(o));
    }
    catch (Exception e)
    {
      throw new AccessorException(e);
    }
  }
  
  public Object getUnadapted(BeanT bean)
    throws AccessorException
  {
    return this.core.getUnadapted(bean);
  }
  
  public void setUnadapted(BeanT bean, Object value)
    throws AccessorException
  {
    this.core.setUnadapted(bean, value);
  }
  
  private XmlAdapter<OnWireValueT, InMemValueT> getAdapter()
  {
    Coordinator coordinator = Coordinator._getInstance();
    if (coordinator != null) {
      return coordinator.getAdapter(this.adapter);
    }
    synchronized (this)
    {
      if (this.staticAdapter == null) {
        this.staticAdapter = ((XmlAdapter)ClassFactory.create(this.adapter));
      }
    }
    return this.staticAdapter;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\AdaptedAccessor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */