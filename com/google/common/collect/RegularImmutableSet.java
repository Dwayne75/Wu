package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;

@GwtCompatible(serializable=true, emulated=true)
final class RegularImmutableSet<E>
  extends ImmutableSet<E>
{
  private final Object[] elements;
  @VisibleForTesting
  final transient Object[] table;
  private final transient int mask;
  private final transient int hashCode;
  
  RegularImmutableSet(Object[] elements, int hashCode, Object[] table, int mask)
  {
    this.elements = elements;
    this.table = table;
    this.mask = mask;
    this.hashCode = hashCode;
  }
  
  public boolean contains(Object target)
  {
    if (target == null) {
      return false;
    }
    for (int i = Hashing.smear(target.hashCode());; i++)
    {
      Object candidate = this.table[(i & this.mask)];
      if (candidate == null) {
        return false;
      }
      if (candidate.equals(target)) {
        return true;
      }
    }
  }
  
  public int size()
  {
    return this.elements.length;
  }
  
  public UnmodifiableIterator<E> iterator()
  {
    return Iterators.forArray(this.elements);
  }
  
  int copyIntoArray(Object[] dst, int offset)
  {
    System.arraycopy(this.elements, 0, dst, offset, this.elements.length);
    return offset + this.elements.length;
  }
  
  ImmutableList<E> createAsList()
  {
    return new RegularImmutableAsList(this, this.elements);
  }
  
  boolean isPartialView()
  {
    return false;
  }
  
  public int hashCode()
  {
    return this.hashCode;
  }
  
  boolean isHashCodeFast()
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\RegularImmutableSet.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */