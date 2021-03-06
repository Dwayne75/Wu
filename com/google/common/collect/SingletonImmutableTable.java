package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.Map;

@GwtCompatible
class SingletonImmutableTable<R, C, V>
  extends ImmutableTable<R, C, V>
{
  final R singleRowKey;
  final C singleColumnKey;
  final V singleValue;
  
  SingletonImmutableTable(R rowKey, C columnKey, V value)
  {
    this.singleRowKey = Preconditions.checkNotNull(rowKey);
    this.singleColumnKey = Preconditions.checkNotNull(columnKey);
    this.singleValue = Preconditions.checkNotNull(value);
  }
  
  SingletonImmutableTable(Table.Cell<R, C, V> cell)
  {
    this(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
  }
  
  public ImmutableMap<R, V> column(C columnKey)
  {
    Preconditions.checkNotNull(columnKey);
    return containsColumn(columnKey) ? ImmutableMap.of(this.singleRowKey, this.singleValue) : ImmutableMap.of();
  }
  
  public ImmutableMap<C, Map<R, V>> columnMap()
  {
    return ImmutableMap.of(this.singleColumnKey, ImmutableMap.of(this.singleRowKey, this.singleValue));
  }
  
  public ImmutableMap<R, Map<C, V>> rowMap()
  {
    return ImmutableMap.of(this.singleRowKey, ImmutableMap.of(this.singleColumnKey, this.singleValue));
  }
  
  public int size()
  {
    return 1;
  }
  
  ImmutableSet<Table.Cell<R, C, V>> createCellSet()
  {
    return ImmutableSet.of(cellOf(this.singleRowKey, this.singleColumnKey, this.singleValue));
  }
  
  ImmutableCollection<V> createValues()
  {
    return ImmutableSet.of(this.singleValue);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\SingletonImmutableTable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */