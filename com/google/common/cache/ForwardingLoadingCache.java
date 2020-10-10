package com.google.common.cache;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.concurrent.ExecutionException;

@Beta
public abstract class ForwardingLoadingCache<K, V>
  extends ForwardingCache<K, V>
  implements LoadingCache<K, V>
{
  protected abstract LoadingCache<K, V> delegate();
  
  public V get(K key)
    throws ExecutionException
  {
    return (V)delegate().get(key);
  }
  
  public V getUnchecked(K key)
  {
    return (V)delegate().getUnchecked(key);
  }
  
  public ImmutableMap<K, V> getAll(Iterable<? extends K> keys)
    throws ExecutionException
  {
    return delegate().getAll(keys);
  }
  
  public V apply(K key)
  {
    return (V)delegate().apply(key);
  }
  
  public void refresh(K key)
  {
    delegate().refresh(key);
  }
  
  @Beta
  public static abstract class SimpleForwardingLoadingCache<K, V>
    extends ForwardingLoadingCache<K, V>
  {
    private final LoadingCache<K, V> delegate;
    
    protected SimpleForwardingLoadingCache(LoadingCache<K, V> delegate)
    {
      this.delegate = ((LoadingCache)Preconditions.checkNotNull(delegate));
    }
    
    protected final LoadingCache<K, V> delegate()
    {
      return this.delegate;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\cache\ForwardingLoadingCache.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */