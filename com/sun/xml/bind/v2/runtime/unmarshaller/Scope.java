package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import org.xml.sax.SAXException;

public final class Scope<BeanT, PropT, ItemT, PackT>
{
  public final UnmarshallingContext context;
  private BeanT bean;
  private Accessor<BeanT, PropT> acc;
  private PackT pack;
  private Lister<BeanT, PropT, ItemT, PackT> lister;
  
  Scope(UnmarshallingContext context)
  {
    this.context = context;
  }
  
  public boolean hasStarted()
  {
    return this.bean != null;
  }
  
  public void reset()
  {
    if (this.bean == null)
    {
      assert (clean());
      return;
    }
    this.bean = null;
    this.acc = null;
    this.pack = null;
    this.lister = null;
  }
  
  public void finish()
    throws AccessorException
  {
    if (hasStarted())
    {
      this.lister.endPacking(this.pack, this.bean, this.acc);
      reset();
    }
    assert (clean());
  }
  
  private boolean clean()
  {
    return (this.bean == null) && (this.acc == null) && (this.pack == null) && (this.lister == null);
  }
  
  public void add(Accessor<BeanT, PropT> acc, Lister<BeanT, PropT, ItemT, PackT> lister, ItemT value)
    throws SAXException
  {
    try
    {
      if (!hasStarted())
      {
        this.bean = this.context.getCurrentState().target;
        this.acc = acc;
        this.lister = lister;
        this.pack = lister.startPacking(this.bean, acc);
      }
      lister.addToPack(this.pack, value);
    }
    catch (AccessorException e)
    {
      Loader.handleGenericException(e, true);
      
      this.lister = Lister.getErrorInstance();
      this.acc = Accessor.getErrorInstance();
    }
  }
  
  public void start(Accessor<BeanT, PropT> acc, Lister<BeanT, PropT, ItemT, PackT> lister)
    throws SAXException
  {
    try
    {
      if (!hasStarted())
      {
        this.bean = this.context.getCurrentState().target;
        this.acc = acc;
        this.lister = lister;
        this.pack = lister.startPacking(this.bean, acc);
      }
    }
    catch (AccessorException e)
    {
      Loader.handleGenericException(e, true);
      
      this.lister = Lister.getErrorInstance();
      this.acc = Accessor.getErrorInstance();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\Scope.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */