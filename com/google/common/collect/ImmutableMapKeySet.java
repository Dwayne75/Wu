package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.io.Serializable;
import java.util.Map.Entry;
import javax.annotation.Nullable;

@GwtCompatible(emulated=true)
final class ImmutableMapKeySet<K, V>
  extends ImmutableSet<K>
{
  private final ImmutableMap<K, V> map;
  
  ImmutableMapKeySet(ImmutableMap<K, V> map)
  {
    this.map = map;
  }
  
  public int size()
  {
    return this.map.size();
  }
  
  public UnmodifiableIterator<K> iterator()
  {
    return asList().iterator();
  }
  
  public boolean contains(@Nullable Object object)
  {
    return this.map.containsKey(object);
  }
  
  ImmutableList<K> createAsList()
  {
    final ImmutableList<Map.Entry<K, V>> entryList = this.map.entrySet().asList();
    new ImmutableAsList()
    {
      public K get(int index)
      {
        return (K)((Map.Entry)entryList.get(index)).getKey();
      }
      
      ImmutableCollection<K> delegateCollection()
      {
        return ImmutableMapKeySet.this;
      }
    };
  }
  
  boolean isPartialView()
  {
    return true;
  }
  
  @GwtIncompatible("serialization")
  Object writeReplace()
  {
    return new KeySetSerializedForm(this.map);
  }
  
  @GwtIncompatible("serialization")
  private static class KeySetSerializedForm<K>
    implements Serializable
  {
    final ImmutableMap<K, ?> map;
    private static final long serialVersionUID = 0L;
    
    KeySetSerializedForm(ImmutableMap<K, ?> map)
    {
      this.map = map;
    }
    
    Object readResolve()
    {
      return this.map.keySet();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\ImmutableMapKeySet.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */