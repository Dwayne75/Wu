package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;

@GwtCompatible
public abstract class ForwardingSetMultimap<K, V>
  extends ForwardingMultimap<K, V>
  implements SetMultimap<K, V>
{
  protected abstract SetMultimap<K, V> delegate();
  
  public Set<Map.Entry<K, V>> entries()
  {
    return delegate().entries();
  }
  
  public Set<V> get(@Nullable K key)
  {
    return delegate().get(key);
  }
  
  public Set<V> removeAll(@Nullable Object key)
  {
    return delegate().removeAll(key);
  }
  
  public Set<V> replaceValues(K key, Iterable<? extends V> values)
  {
    return delegate().replaceValues(key, values);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\ForwardingSetMultimap.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */