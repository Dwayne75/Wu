package org.fourthline.cling.support.shared;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class AbstractMap<K, V>
  implements Map<K, V>
{
  Set<K> keySet;
  Collection<V> valuesCollection;
  
  public static class SimpleImmutableEntry<K, V>
    implements Map.Entry<K, V>, Serializable
  {
    private static final long serialVersionUID = 7138329143949025153L;
    private final K key;
    private final V value;
    
    public SimpleImmutableEntry(K theKey, V theValue)
    {
      this.key = theKey;
      this.value = theValue;
    }
    
    public SimpleImmutableEntry(Map.Entry<? extends K, ? extends V> copyFrom)
    {
      this.key = copyFrom.getKey();
      this.value = copyFrom.getValue();
    }
    
    public K getKey()
    {
      return (K)this.key;
    }
    
    public V getValue()
    {
      return (V)this.value;
    }
    
    public V setValue(V object)
    {
      throw new UnsupportedOperationException();
    }
    
    public boolean equals(Object object)
    {
      if (this == object) {
        return true;
      }
      if ((object instanceof Map.Entry))
      {
        Map.Entry<?, ?> entry = (Map.Entry)object;
        
        return (this.key == null ? entry.getKey() == null : this.key.equals(entry.getKey())) && (this.value == null ? entry.getValue() == null : this.value.equals(entry.getValue()));
      }
      return false;
    }
    
    public int hashCode()
    {
      return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
    }
    
    public String toString()
    {
      return this.key + "=" + this.value;
    }
  }
  
  public static class SimpleEntry<K, V>
    implements Map.Entry<K, V>, Serializable
  {
    private static final long serialVersionUID = -8499721149061103585L;
    private final K key;
    private V value;
    
    public SimpleEntry(K theKey, V theValue)
    {
      this.key = theKey;
      this.value = theValue;
    }
    
    public SimpleEntry(Map.Entry<? extends K, ? extends V> copyFrom)
    {
      this.key = copyFrom.getKey();
      this.value = copyFrom.getValue();
    }
    
    public K getKey()
    {
      return (K)this.key;
    }
    
    public V getValue()
    {
      return (V)this.value;
    }
    
    public V setValue(V object)
    {
      V result = this.value;
      this.value = object;
      return result;
    }
    
    public boolean equals(Object object)
    {
      if (this == object) {
        return true;
      }
      if ((object instanceof Map.Entry))
      {
        Map.Entry<?, ?> entry = (Map.Entry)object;
        
        return (this.key == null ? entry.getKey() == null : this.key.equals(entry.getKey())) && (this.value == null ? entry.getValue() == null : this.value.equals(entry.getValue()));
      }
      return false;
    }
    
    public int hashCode()
    {
      return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
    }
    
    public String toString()
    {
      return this.key + "=" + this.value;
    }
  }
  
  public void clear()
  {
    entrySet().clear();
  }
  
  public boolean containsKey(Object key)
  {
    Iterator<Map.Entry<K, V>> it = entrySet().iterator();
    if (key != null)
    {
      do
      {
        if (!it.hasNext()) {
          break;
        }
      } while (!key.equals(((Map.Entry)it.next()).getKey()));
      return true;
    }
    while (it.hasNext()) {
      if (((Map.Entry)it.next()).getKey() == null) {
        return true;
      }
    }
    return false;
  }
  
  public boolean containsValue(Object value)
  {
    Iterator<Map.Entry<K, V>> it = entrySet().iterator();
    if (value != null)
    {
      do
      {
        if (!it.hasNext()) {
          break;
        }
      } while (!value.equals(((Map.Entry)it.next()).getValue()));
      return true;
    }
    while (it.hasNext()) {
      if (((Map.Entry)it.next()).getValue() == null) {
        return true;
      }
    }
    return false;
  }
  
  public abstract Set<Map.Entry<K, V>> entrySet();
  
  public boolean equals(Object object)
  {
    if (this == object) {
      return true;
    }
    if ((object instanceof Map))
    {
      Map<?, ?> map = (Map)object;
      if (size() != map.size()) {
        return false;
      }
      try
      {
        for (Map.Entry<K, V> entry : entrySet())
        {
          K key = entry.getKey();
          V mine = entry.getValue();
          Object theirs = map.get(key);
          if (mine == null)
          {
            if ((theirs != null) || (!map.containsKey(key))) {
              return false;
            }
          }
          else if (!mine.equals(theirs)) {
            return false;
          }
        }
      }
      catch (NullPointerException ignored)
      {
        return false;
      }
      catch (ClassCastException ignored)
      {
        return false;
      }
      return true;
    }
    return false;
  }
  
  public V get(Object key)
  {
    Iterator<Map.Entry<K, V>> it = entrySet().iterator();
    if (key != null) {
      while (it.hasNext())
      {
        Map.Entry<K, V> entry = (Map.Entry)it.next();
        if (key.equals(entry.getKey())) {
          return (V)entry.getValue();
        }
      }
    }
    while (it.hasNext())
    {
      Map.Entry<K, V> entry = (Map.Entry)it.next();
      if (entry.getKey() == null) {
        return (V)entry.getValue();
      }
    }
    return null;
  }
  
  public int hashCode()
  {
    int result = 0;
    Iterator<Map.Entry<K, V>> it = entrySet().iterator();
    while (it.hasNext()) {
      result += ((Map.Entry)it.next()).hashCode();
    }
    return result;
  }
  
  public boolean isEmpty()
  {
    return size() == 0;
  }
  
  public Set<K> keySet()
  {
    if (this.keySet == null) {
      this.keySet = new AbstractSet()
      {
        public boolean contains(Object object)
        {
          return AbstractMap.this.containsKey(object);
        }
        
        public int size()
        {
          return AbstractMap.this.size();
        }
        
        public Iterator<K> iterator()
        {
          new Iterator()
          {
            Iterator<Map.Entry<K, V>> setIterator = AbstractMap.this.entrySet().iterator();
            
            public boolean hasNext()
            {
              return this.setIterator.hasNext();
            }
            
            public K next()
            {
              return (K)((Map.Entry)this.setIterator.next()).getKey();
            }
            
            public void remove()
            {
              this.setIterator.remove();
            }
          };
        }
      };
    }
    return this.keySet;
  }
  
  public V put(K key, V value)
  {
    throw new UnsupportedOperationException();
  }
  
  public void putAll(Map<? extends K, ? extends V> map)
  {
    for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }
  
  public V remove(Object key)
  {
    Iterator<Map.Entry<K, V>> it = entrySet().iterator();
    if (key != null) {
      while (it.hasNext())
      {
        Map.Entry<K, V> entry = (Map.Entry)it.next();
        if (key.equals(entry.getKey()))
        {
          it.remove();
          return (V)entry.getValue();
        }
      }
    }
    while (it.hasNext())
    {
      Map.Entry<K, V> entry = (Map.Entry)it.next();
      if (entry.getKey() == null)
      {
        it.remove();
        return (V)entry.getValue();
      }
    }
    return null;
  }
  
  public int size()
  {
    return entrySet().size();
  }
  
  public String toString()
  {
    if (isEmpty()) {
      return "{}";
    }
    StringBuilder buffer = new StringBuilder(size() * 28);
    buffer.append('{');
    Iterator<Map.Entry<K, V>> it = entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry<K, V> entry = (Map.Entry)it.next();
      Object key = entry.getKey();
      if (key != this) {
        buffer.append(key);
      } else {
        buffer.append("(this Map)");
      }
      buffer.append('=');
      Object value = entry.getValue();
      if (value != this) {
        buffer.append(value);
      } else {
        buffer.append("(this Map)");
      }
      if (it.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append('}');
    return buffer.toString();
  }
  
  public Collection<V> values()
  {
    if (this.valuesCollection == null) {
      this.valuesCollection = new AbstractCollection()
      {
        public int size()
        {
          return AbstractMap.this.size();
        }
        
        public boolean contains(Object object)
        {
          return AbstractMap.this.containsValue(object);
        }
        
        public Iterator<V> iterator()
        {
          new Iterator()
          {
            Iterator<Map.Entry<K, V>> setIterator = AbstractMap.this.entrySet().iterator();
            
            public boolean hasNext()
            {
              return this.setIterator.hasNext();
            }
            
            public V next()
            {
              return (V)((Map.Entry)this.setIterator.next()).getValue();
            }
            
            public void remove()
            {
              this.setIterator.remove();
            }
          };
        }
      };
    }
    return this.valuesCollection;
  }
  
  protected Object clone()
    throws CloneNotSupportedException
  {
    AbstractMap<K, V> result = (AbstractMap)super.clone();
    result.keySet = null;
    result.valuesCollection = null;
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\shared\AbstractMap.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */