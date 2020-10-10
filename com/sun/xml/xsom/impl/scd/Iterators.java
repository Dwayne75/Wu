package com.sun.xml.xsom.impl.scd;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class Iterators
{
  static abstract class ReadOnly<T>
    implements Iterator<T>
  {
    public final void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
  
  private static final Iterator EMPTY = Collections.EMPTY_LIST.iterator();
  
  public static <T> Iterator<T> empty()
  {
    return EMPTY;
  }
  
  public static <T> Iterator<T> singleton(T value)
  {
    return new Singleton(value);
  }
  
  static final class Singleton<T>
    extends Iterators.ReadOnly<T>
  {
    private T next;
    
    Singleton(T next)
    {
      this.next = next;
    }
    
    public boolean hasNext()
    {
      return this.next != null;
    }
    
    public T next()
    {
      T r = this.next;
      this.next = null;
      return r;
    }
  }
  
  public static abstract class Adapter<T, U>
    extends Iterators.ReadOnly<T>
  {
    private final Iterator<? extends U> core;
    
    public Adapter(Iterator<? extends U> core)
    {
      this.core = core;
    }
    
    public boolean hasNext()
    {
      return this.core.hasNext();
    }
    
    public T next()
    {
      return (T)filter(this.core.next());
    }
    
    protected abstract T filter(U paramU);
  }
  
  public static abstract class Map<T, U>
    extends Iterators.ReadOnly<T>
  {
    private final Iterator<? extends U> core;
    private Iterator<? extends T> current;
    
    protected Map(Iterator<? extends U> core)
    {
      this.core = core;
    }
    
    public boolean hasNext()
    {
      while ((this.current == null) || (!this.current.hasNext()))
      {
        if (!this.core.hasNext()) {
          return false;
        }
        this.current = apply(this.core.next());
      }
      return true;
    }
    
    public T next()
    {
      return (T)this.current.next();
    }
    
    protected abstract Iterator<? extends T> apply(U paramU);
  }
  
  public static abstract class Filter<T>
    extends Iterators.ReadOnly<T>
  {
    private final Iterator<? extends T> core;
    private T next;
    
    protected Filter(Iterator<? extends T> core)
    {
      this.core = core;
    }
    
    protected abstract boolean matches(T paramT);
    
    public boolean hasNext()
    {
      while ((this.core.hasNext()) && (this.next == null))
      {
        this.next = this.core.next();
        if (!matches(this.next)) {
          this.next = null;
        }
      }
      return this.next != null;
    }
    
    public T next()
    {
      if (this.next == null) {
        throw new NoSuchElementException();
      }
      T r = this.next;
      this.next = null;
      return r;
    }
  }
  
  static final class Unique<T>
    extends Iterators.Filter<T>
  {
    private Set<T> values = new HashSet();
    
    public Unique(Iterator<? extends T> core)
    {
      super();
    }
    
    protected boolean matches(T value)
    {
      return this.values.add(value);
    }
  }
  
  public static final class Union<T>
    extends Iterators.ReadOnly<T>
  {
    private final Iterator<? extends T> first;
    private final Iterator<? extends T> second;
    
    public Union(Iterator<? extends T> first, Iterator<? extends T> second)
    {
      this.first = first;
      this.second = second;
    }
    
    public boolean hasNext()
    {
      return (this.first.hasNext()) || (this.second.hasNext());
    }
    
    public T next()
    {
      if (this.first.hasNext()) {
        return (T)this.first.next();
      }
      return (T)this.second.next();
    }
  }
  
  public static final class Array<T>
    extends Iterators.ReadOnly<T>
  {
    private final T[] items;
    private int index = 0;
    
    public Array(T[] items)
    {
      this.items = items;
    }
    
    public boolean hasNext()
    {
      return this.index < this.items.length;
    }
    
    public T next()
    {
      return (T)this.items[(this.index++)];
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\scd\Iterators.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */