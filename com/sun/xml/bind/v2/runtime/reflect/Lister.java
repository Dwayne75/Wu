package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.istack.SAXException2;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx.Snapshot;
import com.sun.xml.bind.v2.runtime.unmarshaller.Patcher;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;

public abstract class Lister<BeanT, PropT, ItemT, PackT>
{
  private static final Map<Class, WeakReference<Lister>> arrayListerCache;
  static final Map<Class, Lister> primitiveArrayListers;
  
  public static <BeanT, PropT, ItemT, PackT> Lister<BeanT, PropT, ItemT, PackT> create(Type fieldType, ID idness, Adapter<Type, Class> adapter)
  {
    Class rawType = Navigator.REFLECTION.erasure(fieldType);
    Lister l;
    if (rawType.isArray())
    {
      Class itemType = rawType.getComponentType();
      l = getArrayLister(itemType);
    }
    else
    {
      Lister l;
      if (Collection.class.isAssignableFrom(rawType))
      {
        Type bt = Navigator.REFLECTION.getBaseClass(fieldType, Collection.class);
        Class itemType;
        Class itemType;
        if ((bt instanceof ParameterizedType)) {
          itemType = Navigator.REFLECTION.erasure(((ParameterizedType)bt).getActualTypeArguments()[0]);
        } else {
          itemType = Object.class;
        }
        l = new CollectionLister(getImplClass(rawType));
      }
      else
      {
        return null;
      }
    }
    Lister l;
    Class itemType;
    if (idness == ID.IDREF) {
      l = new IDREFS(l, itemType);
    }
    if (adapter != null) {
      l = new AdaptedLister(l, (Class)adapter.adapterType);
    }
    return l;
  }
  
  private static Class getImplClass(Class<?> fieldType)
  {
    return ClassFactory.inferImplClass(fieldType, COLLECTION_IMPL_CLASSES);
  }
  
  private static Lister getArrayLister(Class componentType)
  {
    Lister l = null;
    if (componentType.isPrimitive())
    {
      l = (Lister)primitiveArrayListers.get(componentType);
    }
    else
    {
      WeakReference<Lister> wr = (WeakReference)arrayListerCache.get(componentType);
      if (wr != null) {
        l = (Lister)wr.get();
      }
      if (l == null)
      {
        l = new ArrayLister(componentType);
        arrayListerCache.put(componentType, new WeakReference(l));
      }
    }
    assert (l != null);
    return l;
  }
  
  private static final class ArrayLister<BeanT, ItemT>
    extends Lister<BeanT, ItemT[], ItemT, Lister.Pack<ItemT>>
  {
    private final Class<ItemT> itemType;
    
    public ArrayLister(Class<ItemT> itemType)
    {
      this.itemType = itemType;
    }
    
    public ListIterator<ItemT> iterator(final ItemT[] objects, XMLSerializer context)
    {
      new ListIterator()
      {
        int idx = 0;
        
        public boolean hasNext()
        {
          return this.idx < objects.length;
        }
        
        public ItemT next()
        {
          return (ItemT)objects[(this.idx++)];
        }
      };
    }
    
    public Lister.Pack startPacking(BeanT current, Accessor<BeanT, ItemT[]> acc)
    {
      return new Lister.Pack(this.itemType);
    }
    
    public void addToPack(Lister.Pack<ItemT> objects, ItemT o)
    {
      objects.add(o);
    }
    
    public void endPacking(Lister.Pack<ItemT> pack, BeanT bean, Accessor<BeanT, ItemT[]> acc)
      throws AccessorException
    {
      acc.set(bean, pack.build());
    }
    
    public void reset(BeanT o, Accessor<BeanT, ItemT[]> acc)
      throws AccessorException
    {
      acc.set(o, (Object[])Array.newInstance(this.itemType, 0));
    }
  }
  
  public static final class Pack<ItemT>
    extends ArrayList<ItemT>
  {
    private final Class<ItemT> itemType;
    
    public Pack(Class<ItemT> itemType)
    {
      this.itemType = itemType;
    }
    
    public ItemT[] build()
    {
      return super.toArray((Object[])Array.newInstance(this.itemType, size()));
    }
  }
  
  static
  {
    arrayListerCache = Collections.synchronizedMap(new WeakHashMap());
    
    primitiveArrayListers = new HashMap();
    
    PrimitiveArrayListerBoolean.register();
    PrimitiveArrayListerByte.register();
    PrimitiveArrayListerCharacter.register();
    PrimitiveArrayListerDouble.register();
    PrimitiveArrayListerFloat.register();
    PrimitiveArrayListerInteger.register();
    PrimitiveArrayListerLong.register();
    PrimitiveArrayListerShort.register();
  }
  
  public static final class CollectionLister<BeanT, T extends Collection>
    extends Lister<BeanT, T, Object, T>
  {
    private final Class<? extends T> implClass;
    
    public CollectionLister(Class<? extends T> implClass)
    {
      this.implClass = implClass;
    }
    
    public ListIterator iterator(T collection, XMLSerializer context)
    {
      final Iterator itr = collection.iterator();
      new ListIterator()
      {
        public boolean hasNext()
        {
          return itr.hasNext();
        }
        
        public Object next()
        {
          return itr.next();
        }
      };
    }
    
    public T startPacking(BeanT bean, Accessor<BeanT, T> acc)
      throws AccessorException
    {
      T collection = (Collection)acc.get(bean);
      if (collection == null)
      {
        collection = (Collection)ClassFactory.create(this.implClass);
        if (!acc.isAdapted()) {
          acc.set(bean, collection);
        }
      }
      collection.clear();
      return collection;
    }
    
    public void addToPack(T collection, Object o)
    {
      collection.add(o);
    }
    
    public void endPacking(T collection, BeanT bean, Accessor<BeanT, T> acc)
      throws AccessorException
    {
      if (acc.isAdapted()) {
        acc.set(bean, collection);
      }
    }
    
    public void reset(BeanT bean, Accessor<BeanT, T> acc)
      throws AccessorException
    {
      T collection = (Collection)acc.get(bean);
      if (collection == null) {
        return;
      }
      collection.clear();
    }
  }
  
  private static final class IDREFS<BeanT, PropT>
    extends Lister<BeanT, PropT, String, IDREFS<BeanT, PropT>.Pack>
  {
    private final Lister<BeanT, PropT, Object, Object> core;
    private final Class itemType;
    
    public IDREFS(Lister core, Class itemType)
    {
      this.core = core;
      this.itemType = itemType;
    }
    
    public ListIterator<String> iterator(PropT prop, XMLSerializer context)
    {
      ListIterator i = this.core.iterator(prop, context);
      
      return new Lister.IDREFSIterator(i, context, null);
    }
    
    public IDREFS<BeanT, PropT>.Pack startPacking(BeanT bean, Accessor<BeanT, PropT> acc)
    {
      return new Pack(bean, acc);
    }
    
    public void addToPack(IDREFS<BeanT, PropT>.Pack pack, String item)
    {
      pack.add(item);
    }
    
    public void endPacking(IDREFS<BeanT, PropT>.Pack pack, BeanT bean, Accessor<BeanT, PropT> acc) {}
    
    public void reset(BeanT bean, Accessor<BeanT, PropT> acc)
      throws AccessorException
    {
      this.core.reset(bean, acc);
    }
    
    private class Pack
      implements Patcher
    {
      private final BeanT bean;
      private final List<String> idrefs = new ArrayList();
      private final UnmarshallingContext context;
      private final Accessor<BeanT, PropT> acc;
      private final LocatorEx location;
      
      public Pack(Accessor<BeanT, PropT> bean)
      {
        this.bean = bean;
        this.acc = acc;
        this.context = UnmarshallingContext.getInstance();
        this.location = new LocatorEx.Snapshot(this.context.getLocator());
        this.context.addPatcher(this);
      }
      
      public void add(String item)
      {
        this.idrefs.add(item);
      }
      
      public void run()
        throws SAXException
      {
        try
        {
          Object pack = Lister.this.startPacking(this.bean, this.acc);
          for (String id : this.idrefs)
          {
            Callable callable = this.context.getObjectFromId(id, Lister.IDREFS.this.itemType);
            Object t;
            try
            {
              t = callable != null ? callable.call() : null;
            }
            catch (SAXException e)
            {
              throw e;
            }
            catch (Exception e)
            {
              throw new SAXException2(e);
            }
            if (t == null)
            {
              this.context.errorUnresolvedIDREF(this.bean, id, this.location);
            }
            else
            {
              TODO.prototype();
              Lister.this.addToPack(pack, t);
            }
          }
          Lister.this.endPacking(pack, this.bean, this.acc);
        }
        catch (AccessorException e)
        {
          this.context.handleError(e);
        }
      }
    }
  }
  
  public static final class IDREFSIterator
    implements ListIterator<String>
  {
    private final ListIterator i;
    private final XMLSerializer context;
    private Object last;
    
    private IDREFSIterator(ListIterator i, XMLSerializer context)
    {
      this.i = i;
      this.context = context;
    }
    
    public boolean hasNext()
    {
      return this.i.hasNext();
    }
    
    public Object last()
    {
      return this.last;
    }
    
    public String next()
      throws SAXException, JAXBException
    {
      this.last = this.i.next();
      String id = this.context.grammar.getBeanInfo(this.last, true).getId(this.last, this.context);
      if (id == null) {
        this.context.errorMissingId(this.last);
      }
      return id;
    }
  }
  
  public static <A, B, C, D> Lister<A, B, C, D> getErrorInstance()
  {
    return ERROR;
  }
  
  public static final Lister ERROR = new Lister()
  {
    public ListIterator iterator(Object o, XMLSerializer context)
    {
      return Lister.EMPTY_ITERATOR;
    }
    
    public Object startPacking(Object o, Accessor accessor)
    {
      return null;
    }
    
    public void addToPack(Object o, Object o1) {}
    
    public void endPacking(Object o, Object o1, Accessor accessor) {}
    
    public void reset(Object o, Accessor accessor) {}
  };
  private static final ListIterator EMPTY_ITERATOR = new ListIterator()
  {
    public boolean hasNext()
    {
      return false;
    }
    
    public Object next()
    {
      throw new IllegalStateException();
    }
  };
  private static final Class[] COLLECTION_IMPL_CLASSES = { ArrayList.class, LinkedList.class, HashSet.class, TreeSet.class, Stack.class };
  
  public abstract ListIterator<ItemT> iterator(PropT paramPropT, XMLSerializer paramXMLSerializer);
  
  public abstract PackT startPacking(BeanT paramBeanT, Accessor<BeanT, PropT> paramAccessor)
    throws AccessorException;
  
  public abstract void addToPack(PackT paramPackT, ItemT paramItemT)
    throws AccessorException;
  
  public abstract void endPacking(PackT paramPackT, BeanT paramBeanT, Accessor<BeanT, PropT> paramAccessor)
    throws AccessorException;
  
  public abstract void reset(BeanT paramBeanT, Accessor<BeanT, PropT> paramAccessor)
    throws AccessorException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\Lister.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */