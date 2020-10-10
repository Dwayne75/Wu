package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.istack.Nullable;
import com.sun.xml.bind.Util;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.reflect.opt.OptimizedAccessorFactory;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext.State;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.xml.sax.SAXException;

public abstract class Accessor<BeanT, ValueT>
  implements Receiver
{
  public final Class<ValueT> valueType;
  
  public Class<ValueT> getValueType()
  {
    return this.valueType;
  }
  
  protected Accessor(Class<ValueT> valueType)
  {
    this.valueType = valueType;
  }
  
  public Accessor<BeanT, ValueT> optimize(@Nullable JAXBContextImpl context)
  {
    return this;
  }
  
  public abstract ValueT get(BeanT paramBeanT)
    throws AccessorException;
  
  public abstract void set(BeanT paramBeanT, ValueT paramValueT)
    throws AccessorException;
  
  public Object getUnadapted(BeanT bean)
    throws AccessorException
  {
    return get(bean);
  }
  
  public boolean isAdapted()
  {
    return false;
  }
  
  public void setUnadapted(BeanT bean, Object value)
    throws AccessorException
  {
    set(bean, value);
  }
  
  public void receive(UnmarshallingContext.State state, Object o)
    throws SAXException
  {
    try
    {
      set(state.target, o);
    }
    catch (AccessorException e)
    {
      Loader.handleGenericException(e, true);
    }
  }
  
  public final <T> Accessor<BeanT, T> adapt(Class<T> targetType, Class<? extends XmlAdapter<T, ValueT>> adapter)
  {
    return new AdaptedAccessor(targetType, this, adapter);
  }
  
  public final <T> Accessor<BeanT, T> adapt(Adapter<Type, Class> adapter)
  {
    return new AdaptedAccessor(Navigator.REFLECTION.erasure((Type)adapter.defaultType), this, (Class)adapter.adapterType);
  }
  
  private static boolean accessWarned = false;
  
  public static class FieldReflection<BeanT, ValueT>
    extends Accessor<BeanT, ValueT>
  {
    public final Field f;
    private static final Logger logger = ;
    
    public FieldReflection(Field f)
    {
      super();
      this.f = f;
      
      int mod = f.getModifiers();
      if ((!Modifier.isPublic(mod)) || (Modifier.isFinal(mod)) || (!Modifier.isPublic(f.getDeclaringClass().getModifiers()))) {
        try
        {
          f.setAccessible(true);
        }
        catch (SecurityException e)
        {
          if (!Accessor.accessWarned) {
            logger.log(Level.WARNING, Messages.UNABLE_TO_ACCESS_NON_PUBLIC_FIELD.format(new Object[] { f.getDeclaringClass().getName(), f.getName() }), e);
          }
          Accessor.access$002(true);
        }
      }
    }
    
    public ValueT get(BeanT bean)
    {
      try
      {
        return (ValueT)this.f.get(bean);
      }
      catch (IllegalAccessException e)
      {
        throw new IllegalAccessError(e.getMessage());
      }
    }
    
    public void set(BeanT bean, ValueT value)
    {
      try
      {
        if (value == null) {
          value = Accessor.uninitializedValues.get(this.valueType);
        }
        this.f.set(bean, value);
      }
      catch (IllegalAccessException e)
      {
        throw new IllegalAccessError(e.getMessage());
      }
    }
    
    public Accessor<BeanT, ValueT> optimize(JAXBContextImpl context)
    {
      if ((context != null) && (context.fastBoot)) {
        return this;
      }
      Accessor<BeanT, ValueT> acc = OptimizedAccessorFactory.get(this.f);
      if (acc != null) {
        return acc;
      }
      return this;
    }
  }
  
  public static final class ReadOnlyFieldReflection<BeanT, ValueT>
    extends Accessor.FieldReflection<BeanT, ValueT>
  {
    public ReadOnlyFieldReflection(Field f)
    {
      super();
    }
    
    public void set(BeanT bean, ValueT value) {}
    
    public Accessor<BeanT, ValueT> optimize(JAXBContextImpl context)
    {
      return this;
    }
  }
  
  public static class GetterSetterReflection<BeanT, ValueT>
    extends Accessor<BeanT, ValueT>
  {
    public final Method getter;
    public final Method setter;
    private static final Logger logger = ;
    
    public GetterSetterReflection(Method getter, Method setter)
    {
      super();
      
      this.getter = getter;
      this.setter = setter;
      if (getter != null) {
        makeAccessible(getter);
      }
      if (setter != null) {
        makeAccessible(setter);
      }
    }
    
    private void makeAccessible(Method m)
    {
      if ((!Modifier.isPublic(m.getModifiers())) || (!Modifier.isPublic(m.getDeclaringClass().getModifiers()))) {
        try
        {
          m.setAccessible(true);
        }
        catch (SecurityException e)
        {
          if (!Accessor.accessWarned) {
            logger.log(Level.WARNING, Messages.UNABLE_TO_ACCESS_NON_PUBLIC_FIELD.format(new Object[] { m.getDeclaringClass().getName(), m.getName() }), e);
          }
          Accessor.access$002(true);
        }
      }
    }
    
    public ValueT get(BeanT bean)
      throws AccessorException
    {
      try
      {
        return (ValueT)this.getter.invoke(bean, new Object[0]);
      }
      catch (IllegalAccessException e)
      {
        throw new IllegalAccessError(e.getMessage());
      }
      catch (InvocationTargetException e)
      {
        throw handleInvocationTargetException(e);
      }
    }
    
    public void set(BeanT bean, ValueT value)
      throws AccessorException
    {
      try
      {
        if (value == null) {
          value = Accessor.uninitializedValues.get(this.valueType);
        }
        this.setter.invoke(bean, new Object[] { value });
      }
      catch (IllegalAccessException e)
      {
        throw new IllegalAccessError(e.getMessage());
      }
      catch (InvocationTargetException e)
      {
        throw handleInvocationTargetException(e);
      }
    }
    
    private AccessorException handleInvocationTargetException(InvocationTargetException e)
    {
      Throwable t = e.getTargetException();
      if ((t instanceof RuntimeException)) {
        throw ((RuntimeException)t);
      }
      if ((t instanceof Error)) {
        throw ((Error)t);
      }
      return new AccessorException(t);
    }
    
    public Accessor<BeanT, ValueT> optimize(JAXBContextImpl context)
    {
      if ((this.getter == null) || (this.setter == null)) {
        return this;
      }
      if ((context != null) && (context.fastBoot)) {
        return this;
      }
      Accessor<BeanT, ValueT> acc = OptimizedAccessorFactory.get(this.getter, this.setter);
      if (acc != null) {
        return acc;
      }
      return this;
    }
  }
  
  public static class GetterOnlyReflection<BeanT, ValueT>
    extends Accessor.GetterSetterReflection<BeanT, ValueT>
  {
    public GetterOnlyReflection(Method getter)
    {
      super(null);
    }
    
    public void set(BeanT bean, ValueT value)
      throws AccessorException
    {
      throw new AccessorException(Messages.NO_SETTER.format(new Object[] { this.getter.toString() }));
    }
  }
  
  public static class SetterOnlyReflection<BeanT, ValueT>
    extends Accessor.GetterSetterReflection<BeanT, ValueT>
  {
    public SetterOnlyReflection(Method setter)
    {
      super(setter);
    }
    
    public ValueT get(BeanT bean)
      throws AccessorException
    {
      throw new AccessorException(Messages.NO_GETTER.format(new Object[] { this.setter.toString() }));
    }
  }
  
  public static <A, B> Accessor<A, B> getErrorInstance()
  {
    return ERROR;
  }
  
  private static final Accessor ERROR = new Accessor(Object.class)
  {
    public Object get(Object o)
    {
      return null;
    }
    
    public void set(Object o, Object o1) {}
  };
  public static final Accessor<JAXBElement, Object> JAXB_ELEMENT_VALUE = new Accessor(Object.class)
  {
    public Object get(JAXBElement jaxbElement)
    {
      return jaxbElement.getValue();
    }
    
    public void set(JAXBElement jaxbElement, Object o)
    {
      jaxbElement.setValue(o);
    }
  };
  private static final Map<Class, Object> uninitializedValues = new HashMap();
  
  static
  {
    uninitializedValues.put(Byte.TYPE, Byte.valueOf((byte)0));
    uninitializedValues.put(Boolean.TYPE, Boolean.valueOf(false));
    uninitializedValues.put(Character.TYPE, Character.valueOf('\000'));
    uninitializedValues.put(Float.TYPE, Float.valueOf(0.0F));
    uninitializedValues.put(Double.TYPE, Double.valueOf(0.0D));
    uninitializedValues.put(Integer.TYPE, Integer.valueOf(0));
    uninitializedValues.put(Long.TYPE, Long.valueOf(0L));
    uninitializedValues.put(Short.TYPE, Short.valueOf((short)0));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\Accessor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */