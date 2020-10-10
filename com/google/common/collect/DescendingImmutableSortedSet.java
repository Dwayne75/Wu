package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import javax.annotation.Nullable;

class DescendingImmutableSortedSet<E>
  extends ImmutableSortedSet<E>
{
  private final ImmutableSortedSet<E> forward;
  
  DescendingImmutableSortedSet(ImmutableSortedSet<E> forward)
  {
    super(Ordering.from(forward.comparator()).reverse());
    this.forward = forward;
  }
  
  public int size()
  {
    return this.forward.size();
  }
  
  public UnmodifiableIterator<E> iterator()
  {
    return this.forward.descendingIterator();
  }
  
  ImmutableSortedSet<E> headSetImpl(E toElement, boolean inclusive)
  {
    return this.forward.tailSet(toElement, inclusive).descendingSet();
  }
  
  ImmutableSortedSet<E> subSetImpl(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive)
  {
    return this.forward.subSet(toElement, toInclusive, fromElement, fromInclusive).descendingSet();
  }
  
  ImmutableSortedSet<E> tailSetImpl(E fromElement, boolean inclusive)
  {
    return this.forward.headSet(fromElement, inclusive).descendingSet();
  }
  
  @GwtIncompatible("NavigableSet")
  public ImmutableSortedSet<E> descendingSet()
  {
    return this.forward;
  }
  
  @GwtIncompatible("NavigableSet")
  public UnmodifiableIterator<E> descendingIterator()
  {
    return this.forward.iterator();
  }
  
  @GwtIncompatible("NavigableSet")
  ImmutableSortedSet<E> createDescendingSet()
  {
    throw new AssertionError("should never be called");
  }
  
  public E lower(E element)
  {
    return (E)this.forward.higher(element);
  }
  
  public E floor(E element)
  {
    return (E)this.forward.ceiling(element);
  }
  
  public E ceiling(E element)
  {
    return (E)this.forward.floor(element);
  }
  
  public E higher(E element)
  {
    return (E)this.forward.lower(element);
  }
  
  int indexOf(@Nullable Object target)
  {
    int index = this.forward.indexOf(target);
    if (index == -1) {
      return index;
    }
    return size() - 1 - index;
  }
  
  boolean isPartialView()
  {
    return this.forward.isPartialView();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\DescendingImmutableSortedSet.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */