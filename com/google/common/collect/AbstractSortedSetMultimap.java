package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nullable;

@GwtCompatible
abstract class AbstractSortedSetMultimap<K, V>
  extends AbstractSetMultimap<K, V>
  implements SortedSetMultimap<K, V>
{
  private static final long serialVersionUID = 430848587173315748L;
  
  protected AbstractSortedSetMultimap(Map<K, Collection<V>> map)
  {
    super(map);
  }
  
  abstract SortedSet<V> createCollection();
  
  SortedSet<V> createUnmodifiableEmptyCollection()
  {
    Comparator<? super V> comparator = valueComparator();
    if (comparator == null) {
      return Collections.unmodifiableSortedSet(createCollection());
    }
    return ImmutableSortedSet.emptySet(valueComparator());
  }
  
  public SortedSet<V> get(@Nullable K key)
  {
    return (SortedSet)super.get(key);
  }
  
  public SortedSet<V> removeAll(@Nullable Object key)
  {
    return (SortedSet)super.removeAll(key);
  }
  
  public SortedSet<V> replaceValues(@Nullable K key, Iterable<? extends V> values)
  {
    return (SortedSet)super.replaceValues(key, values);
  }
  
  public Map<K, Collection<V>> asMap()
  {
    return super.asMap();
  }
  
  public Collection<V> values()
  {
    return super.values();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\AbstractSortedSetMultimap.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */