package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@GwtCompatible
@Immutable
final class DenseImmutableTable<R, C, V>
  extends RegularImmutableTable<R, C, V>
{
  private final ImmutableMap<R, Integer> rowKeyToIndex;
  private final ImmutableMap<C, Integer> columnKeyToIndex;
  private final ImmutableMap<R, Map<C, V>> rowMap;
  private final ImmutableMap<C, Map<R, V>> columnMap;
  private final int[] rowCounts;
  private final int[] columnCounts;
  private final V[][] values;
  private final int[] iterationOrderRow;
  private final int[] iterationOrderColumn;
  
  private static <E> ImmutableMap<E, Integer> makeIndex(ImmutableSet<E> set)
  {
    ImmutableMap.Builder<E, Integer> indexBuilder = ImmutableMap.builder();
    int i = 0;
    for (E key : set)
    {
      indexBuilder.put(key, Integer.valueOf(i));
      i++;
    }
    return indexBuilder.build();
  }
  
  DenseImmutableTable(ImmutableList<Table.Cell<R, C, V>> cellList, ImmutableSet<R> rowSpace, ImmutableSet<C> columnSpace)
  {
    V[][] array = (Object[][])new Object[rowSpace.size()][columnSpace.size()];
    this.values = array;
    this.rowKeyToIndex = makeIndex(rowSpace);
    this.columnKeyToIndex = makeIndex(columnSpace);
    this.rowCounts = new int[this.rowKeyToIndex.size()];
    this.columnCounts = new int[this.columnKeyToIndex.size()];
    int[] iterationOrderRow = new int[cellList.size()];
    int[] iterationOrderColumn = new int[cellList.size()];
    for (int i = 0; i < cellList.size(); i++)
    {
      Table.Cell<R, C, V> cell = (Table.Cell)cellList.get(i);
      R rowKey = cell.getRowKey();
      C columnKey = cell.getColumnKey();
      int rowIndex = ((Integer)this.rowKeyToIndex.get(rowKey)).intValue();
      int columnIndex = ((Integer)this.columnKeyToIndex.get(columnKey)).intValue();
      V existingValue = this.values[rowIndex][columnIndex];
      Preconditions.checkArgument(existingValue == null, "duplicate key: (%s, %s)", new Object[] { rowKey, columnKey });
      this.values[rowIndex][columnIndex] = cell.getValue();
      this.rowCounts[rowIndex] += 1;
      this.columnCounts[columnIndex] += 1;
      iterationOrderRow[i] = rowIndex;
      iterationOrderColumn[i] = columnIndex;
    }
    this.iterationOrderRow = iterationOrderRow;
    this.iterationOrderColumn = iterationOrderColumn;
    this.rowMap = new RowMap(null);
    this.columnMap = new ColumnMap(null);
  }
  
  private static abstract class ImmutableArrayMap<K, V>
    extends ImmutableMap<K, V>
  {
    private final int size;
    
    ImmutableArrayMap(int size)
    {
      this.size = size;
    }
    
    abstract ImmutableMap<K, Integer> keyToIndex();
    
    private boolean isFull()
    {
      return this.size == keyToIndex().size();
    }
    
    K getKey(int index)
    {
      return (K)keyToIndex().keySet().asList().get(index);
    }
    
    @Nullable
    abstract V getValue(int paramInt);
    
    ImmutableSet<K> createKeySet()
    {
      return isFull() ? keyToIndex().keySet() : super.createKeySet();
    }
    
    public int size()
    {
      return this.size;
    }
    
    public V get(@Nullable Object key)
    {
      Integer keyIndex = (Integer)keyToIndex().get(key);
      return keyIndex == null ? null : getValue(keyIndex.intValue());
    }
    
    ImmutableSet<Map.Entry<K, V>> createEntrySet()
    {
      new ImmutableMapEntrySet()
      {
        ImmutableMap<K, V> map()
        {
          return DenseImmutableTable.ImmutableArrayMap.this;
        }
        
        public UnmodifiableIterator<Map.Entry<K, V>> iterator()
        {
          new AbstractIterator()
          {
            private int index = -1;
            private final int maxIndex = DenseImmutableTable.ImmutableArrayMap.this.keyToIndex().size();
            
            protected Map.Entry<K, V> computeNext()
            {
              for (this.index += 1; this.index < this.maxIndex; this.index += 1)
              {
                V value = DenseImmutableTable.ImmutableArrayMap.this.getValue(this.index);
                if (value != null) {
                  return Maps.immutableEntry(DenseImmutableTable.ImmutableArrayMap.this.getKey(this.index), value);
                }
              }
              return (Map.Entry)endOfData();
            }
          };
        }
      };
    }
  }
  
  private final class Row
    extends DenseImmutableTable.ImmutableArrayMap<C, V>
  {
    private final int rowIndex;
    
    Row(int rowIndex)
    {
      super();
      this.rowIndex = rowIndex;
    }
    
    ImmutableMap<C, Integer> keyToIndex()
    {
      return DenseImmutableTable.this.columnKeyToIndex;
    }
    
    V getValue(int keyIndex)
    {
      return (V)DenseImmutableTable.this.values[this.rowIndex][keyIndex];
    }
    
    boolean isPartialView()
    {
      return true;
    }
  }
  
  private final class Column
    extends DenseImmutableTable.ImmutableArrayMap<R, V>
  {
    private final int columnIndex;
    
    Column(int columnIndex)
    {
      super();
      this.columnIndex = columnIndex;
    }
    
    ImmutableMap<R, Integer> keyToIndex()
    {
      return DenseImmutableTable.this.rowKeyToIndex;
    }
    
    V getValue(int keyIndex)
    {
      return (V)DenseImmutableTable.this.values[keyIndex][this.columnIndex];
    }
    
    boolean isPartialView()
    {
      return true;
    }
  }
  
  private final class RowMap
    extends DenseImmutableTable.ImmutableArrayMap<R, Map<C, V>>
  {
    private RowMap()
    {
      super();
    }
    
    ImmutableMap<R, Integer> keyToIndex()
    {
      return DenseImmutableTable.this.rowKeyToIndex;
    }
    
    Map<C, V> getValue(int keyIndex)
    {
      return new DenseImmutableTable.Row(DenseImmutableTable.this, keyIndex);
    }
    
    boolean isPartialView()
    {
      return false;
    }
  }
  
  private final class ColumnMap
    extends DenseImmutableTable.ImmutableArrayMap<C, Map<R, V>>
  {
    private ColumnMap()
    {
      super();
    }
    
    ImmutableMap<C, Integer> keyToIndex()
    {
      return DenseImmutableTable.this.columnKeyToIndex;
    }
    
    Map<R, V> getValue(int keyIndex)
    {
      return new DenseImmutableTable.Column(DenseImmutableTable.this, keyIndex);
    }
    
    boolean isPartialView()
    {
      return false;
    }
  }
  
  public ImmutableMap<C, Map<R, V>> columnMap()
  {
    return this.columnMap;
  }
  
  public ImmutableMap<R, Map<C, V>> rowMap()
  {
    return this.rowMap;
  }
  
  public V get(@Nullable Object rowKey, @Nullable Object columnKey)
  {
    Integer rowIndex = (Integer)this.rowKeyToIndex.get(rowKey);
    Integer columnIndex = (Integer)this.columnKeyToIndex.get(columnKey);
    return (rowIndex == null) || (columnIndex == null) ? null : this.values[rowIndex.intValue()][columnIndex.intValue()];
  }
  
  public int size()
  {
    return this.iterationOrderRow.length;
  }
  
  Table.Cell<R, C, V> getCell(int index)
  {
    int rowIndex = this.iterationOrderRow[index];
    int columnIndex = this.iterationOrderColumn[index];
    R rowKey = rowKeySet().asList().get(rowIndex);
    C columnKey = columnKeySet().asList().get(columnIndex);
    V value = this.values[rowIndex][columnIndex];
    return cellOf(rowKey, columnKey, value);
  }
  
  V getValue(int index)
  {
    return (V)this.values[this.iterationOrderRow[index]][this.iterationOrderColumn[index]];
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\DenseImmutableTable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */