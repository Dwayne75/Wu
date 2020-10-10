package com.sun.tools.xjc.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SubList
  extends AbstractList
{
  private final List l;
  private final int offset;
  private int size;
  
  public SubList(List list, int fromIndex, int toIndex)
  {
    if (fromIndex < 0) {
      throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
    }
    if (toIndex > list.size()) {
      throw new IndexOutOfBoundsException("toIndex = " + toIndex);
    }
    if (fromIndex > toIndex) {
      throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
    }
    this.l = list;
    this.offset = fromIndex;
    this.size = (toIndex - fromIndex);
  }
  
  public Object set(int index, Object element)
  {
    rangeCheck(index);
    return this.l.set(index + this.offset, element);
  }
  
  public Object get(int index)
  {
    rangeCheck(index);
    return this.l.get(index + this.offset);
  }
  
  public int size()
  {
    return this.size;
  }
  
  public void add(int index, Object element)
  {
    if ((index < 0) || (index > this.size)) {
      throw new IndexOutOfBoundsException();
    }
    this.l.add(index + this.offset, element);
    this.size += 1;
    this.modCount += 1;
  }
  
  public Object remove(int index)
  {
    rangeCheck(index);
    Object result = this.l.remove(index + this.offset);
    this.size -= 1;
    this.modCount += 1;
    return result;
  }
  
  public boolean addAll(Collection c)
  {
    return addAll(this.size, c);
  }
  
  public boolean addAll(int index, Collection c)
  {
    if ((index < 0) || (index > this.size)) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
    }
    int cSize = c.size();
    if (cSize == 0) {
      return false;
    }
    this.l.addAll(this.offset + index, c);
    this.size += cSize;
    this.modCount += 1;
    return true;
  }
  
  public Iterator iterator()
  {
    return listIterator();
  }
  
  public ListIterator listIterator(int index)
  {
    if ((index < 0) || (index > this.size)) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
    }
    return new SubList.1(this, index);
  }
  
  public List subList(int fromIndex, int toIndex)
  {
    return new SubList(this, fromIndex, toIndex);
  }
  
  private void rangeCheck(int index)
  {
    if ((index < 0) || (index >= this.size)) {
      throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + this.size);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\util\SubList.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */