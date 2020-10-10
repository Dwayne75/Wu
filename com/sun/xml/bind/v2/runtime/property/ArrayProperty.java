package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

abstract class ArrayProperty<BeanT, ListT, ItemT>
  extends PropertyImpl<BeanT>
{
  protected final Accessor<BeanT, ListT> acc;
  protected final Lister<BeanT, ListT, ItemT, Object> lister;
  
  protected ArrayProperty(JAXBContextImpl context, RuntimePropertyInfo prop)
  {
    super(context, prop);
    
    assert (prop.isCollection());
    this.lister = Lister.create(Navigator.REFLECTION.erasure(prop.getRawType()), prop.id(), prop.getAdapter());
    
    assert (this.lister != null);
    this.acc = prop.getAccessor().optimize(context);
    assert (this.acc != null);
  }
  
  public void reset(BeanT o)
    throws AccessorException
  {
    this.lister.reset(o, this.acc);
  }
  
  public final String getIdValue(BeanT bean)
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\property\ArrayProperty.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */