package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@GwtCompatible
final class WellBehavedMap<K, V>
  extends ForwardingMap<K, V>
{
  private final Map<K, V> delegate;
  private Set<Map.Entry<K, V>> entrySet;
  
  private WellBehavedMap(Map<K, V> delegate)
  {
    this.delegate = delegate;
  }
  
  static <K, V> WellBehavedMap<K, V> wrap(Map<K, V> delegate)
  {
    return new WellBehavedMap(delegate);
  }
  
  protected Map<K, V> delegate()
  {
    return this.delegate;
  }
  
  public Set<Map.Entry<K, V>> entrySet()
  {
    Set<Map.Entry<K, V>> es = this.entrySet;
    if (es != null) {
      return es;
    }
    return this.entrySet = new EntrySet(null);
  }
  
  private final class EntrySet
    extends Maps.EntrySet<K, V>
  {
    private EntrySet() {}
    
    Map<K, V> map()
    {
      return WellBehavedMap.this;
    }
    
    public Iterator<Map.Entry<K, V>> iterator()
    {
      new TransformedIterator(WellBehavedMap.this.keySet().iterator())
      {
        Map.Entry<K, V> transform(final K key)
        {
          new AbstractMapEntry()
          {
            public K getKey()
            {
              return (K)key;
            }
            
            public V getValue()
            {
              return (V)WellBehavedMap.this.get(key);
            }
            
            public V setValue(V value)
            {
              return (V)WellBehavedMap.this.put(key, value);
            }
          };
        }
      };
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\WellBehavedMap.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */