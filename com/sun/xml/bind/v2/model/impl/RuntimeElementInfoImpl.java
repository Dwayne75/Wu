package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

final class RuntimeElementInfoImpl
  extends ElementInfoImpl<Type, Class, Field, Method>
  implements RuntimeElementInfo
{
  private final Class<? extends XmlAdapter> adapterType;
  
  public RuntimeElementInfoImpl(RuntimeModelBuilder modelBuilder, RegistryInfoImpl registry, Method method)
    throws IllegalAnnotationException
  {
    super(modelBuilder, registry, method);
    
    Adapter<Type, Class> a = getProperty().getAdapter();
    if (a != null) {
      this.adapterType = ((Class)a.adapterType);
    } else {
      this.adapterType = null;
    }
  }
  
  protected ElementInfoImpl<Type, Class, Field, Method>.PropertyImpl createPropertyImpl()
  {
    return new RuntimePropertyImpl();
  }
  
  class RuntimePropertyImpl
    extends ElementInfoImpl.PropertyImpl
    implements RuntimeElementPropertyInfo, RuntimeTypeRef
  {
    RuntimePropertyImpl()
    {
      super();
    }
    
    public Accessor getAccessor()
    {
      if (RuntimeElementInfoImpl.this.adapterType == null) {
        return Accessor.JAXB_ELEMENT_VALUE;
      }
      return Accessor.JAXB_ELEMENT_VALUE.adapt((Class)getAdapter().defaultType, RuntimeElementInfoImpl.this.adapterType);
    }
    
    public Type getRawType()
    {
      return Collection.class;
    }
    
    public Type getIndividualType()
    {
      return (Type)RuntimeElementInfoImpl.this.getContentType().getType();
    }
    
    public boolean elementOnlyContent()
    {
      return false;
    }
    
    public List<? extends RuntimeTypeRef> getTypes()
    {
      return Collections.singletonList(this);
    }
    
    public List<? extends RuntimeNonElement> ref()
    {
      return super.ref();
    }
    
    public RuntimeNonElement getTarget()
    {
      return (RuntimeNonElement)super.getTarget();
    }
    
    public RuntimePropertyInfo getSource()
    {
      return this;
    }
    
    public Transducer getTransducer()
    {
      return RuntimeModelBuilder.createTransducer(this);
    }
  }
  
  public RuntimeElementPropertyInfo getProperty()
  {
    return (RuntimeElementPropertyInfo)super.getProperty();
  }
  
  public Class<? extends JAXBElement> getType()
  {
    return Navigator.REFLECTION.erasure((Type)super.getType());
  }
  
  public RuntimeClassInfo getScope()
  {
    return (RuntimeClassInfo)super.getScope();
  }
  
  public RuntimeNonElement getContentType()
  {
    return (RuntimeNonElement)super.getContentType();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\RuntimeElementInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */