package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;

@GwtCompatible(emulated=true)
class RegularImmutableAsList<E>
  extends ImmutableAsList<E>
{
  private final ImmutableCollection<E> delegate;
  private final ImmutableList<? extends E> delegateList;
  
  RegularImmutableAsList(ImmutableCollection<E> delegate, ImmutableList<? extends E> delegateList)
  {
    this.delegate = delegate;
    this.delegateList = delegateList;
  }
  
  RegularImmutableAsList(ImmutableCollection<E> delegate, Object[] array)
  {
    this(delegate, ImmutableList.asImmutableList(array));
  }
  
  ImmutableCollection<E> delegateCollection()
  {
    return this.delegate;
  }
  
  ImmutableList<? extends E> delegateList()
  {
    return this.delegateList;
  }
  
  public UnmodifiableListIterator<E> listIterator(int index)
  {
    return this.delegateList.listIterator(index);
  }
  
  @GwtIncompatible("not present in emulated superclass")
  int copyIntoArray(Object[] dst, int offset)
  {
    return this.delegateList.copyIntoArray(dst, offset);
  }
  
  public E get(int index)
  {
    return (E)this.delegateList.get(index);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\RegularImmutableAsList.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */