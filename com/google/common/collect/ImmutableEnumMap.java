package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;

@GwtCompatible(serializable=true, emulated=true)
final class ImmutableEnumMap<K extends Enum<K>, V>
  extends ImmutableMap<K, V>
{
  private final transient EnumMap<K, V> delegate;
  
  static <K extends Enum<K>, V> ImmutableMap<K, V> asImmutable(EnumMap<K, V> map)
  {
    switch (map.size())
    {
    case 0: 
      return ImmutableMap.of();
    case 1: 
      Map.Entry<K, V> entry = (Map.Entry)Iterables.getOnlyElement(map.entrySet());
      return ImmutableMap.of(entry.getKey(), entry.getValue());
    }
    return new ImmutableEnumMap(map);
  }
  
  private ImmutableEnumMap(EnumMap<K, V> delegate)
  {
    this.delegate = delegate;
    Preconditions.checkArgument(!delegate.isEmpty());
  }
  
  ImmutableSet<K> createKeySet()
  {
    new ImmutableSet()
    {
      public boolean contains(Object object)
      {
        return ImmutableEnumMap.this.delegate.containsKey(object);
      }
      
      public int size()
      {
        return ImmutableEnumMap.this.size();
      }
      
      public UnmodifiableIterator<K> iterator()
      {
        return Iterators.unmodifiableIterator(ImmutableEnumMap.this.delegate.keySet().iterator());
      }
      
      boolean isPartialView()
      {
        return true;
      }
    };
  }
  
  public int size()
  {
    return this.delegate.size();
  }
  
  public boolean containsKey(@Nullable Object key)
  {
    return this.delegate.containsKey(key);
  }
  
  public V get(Object key)
  {
    return (V)this.delegate.get(key);
  }
  
  ImmutableSet<Map.Entry<K, V>> createEntrySet()
  {
    new ImmutableMapEntrySet()
    {
      ImmutableMap<K, V> map()
      {
        return ImmutableEnumMap.this;
      }
      
      public UnmodifiableIterator<Map.Entry<K, V>> iterator()
      {
        new UnmodifiableIterator()
        {
          private final Iterator<Map.Entry<K, V>> backingIterator = ImmutableEnumMap.this.delegate.entrySet().iterator();
          
          public boolean hasNext()
          {
            return this.backingIterator.hasNext();
          }
          
          public Map.Entry<K, V> next()
          {
            Map.Entry<K, V> entry = (Map.Entry)this.backingIterator.next();
            return Maps.immutableEntry(entry.getKey(), entry.getValue());
          }
        };
      }
    };
  }
  
  boolean isPartialView()
  {
    return false;
  }
  
  Object writeReplace()
  {
    return new EnumSerializedForm(this.delegate);
  }
  
  private static class EnumSerializedForm<K extends Enum<K>, V>
    implements Serializable
  {
    final EnumMap<K, V> delegate;
    private static final long serialVersionUID = 0L;
    
    EnumSerializedForm(EnumMap<K, V> delegate)
    {
      this.delegate = delegate;
    }
    
    Object readResolve()
    {
      return new ImmutableEnumMap(this.delegate, null);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\ImmutableEnumMap.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */