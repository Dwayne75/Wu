package com.sun.xml.bind.v2.schemagen;

import java.util.Map;
import java.util.TreeMap;

final class MultiMap<K extends Comparable<K>, V>
  extends TreeMap<K, V>
{
  private final V many;
  
  public MultiMap(V many)
  {
    this.many = many;
  }
  
  public V put(K key, V value)
  {
    V old = super.put(key, value);
    if ((old != null) && (!old.equals(value))) {
      super.put(key, this.many);
    }
    return old;
  }
  
  public void putAll(Map<? extends K, ? extends V> map)
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\MultiMap.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */