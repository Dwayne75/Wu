package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Collection;
import javax.annotation.Nullable;

@GwtCompatible(emulated=true)
final class RegularContiguousSet<C extends Comparable>
  extends ContiguousSet<C>
{
  private final Range<C> range;
  private static final long serialVersionUID = 0L;
  
  RegularContiguousSet(Range<C> range, DiscreteDomain<C> domain)
  {
    super(domain);
    this.range = range;
  }
  
  private ContiguousSet<C> intersectionInCurrentDomain(Range<C> other)
  {
    return this.range.isConnected(other) ? ContiguousSet.create(this.range.intersection(other), this.domain) : new EmptyContiguousSet(this.domain);
  }
  
  ContiguousSet<C> headSetImpl(C toElement, boolean inclusive)
  {
    return intersectionInCurrentDomain(Range.upTo(toElement, BoundType.forBoolean(inclusive)));
  }
  
  ContiguousSet<C> subSetImpl(C fromElement, boolean fromInclusive, C toElement, boolean toInclusive)
  {
    if ((fromElement.compareTo(toElement) == 0) && (!fromInclusive) && (!toInclusive)) {
      return new EmptyContiguousSet(this.domain);
    }
    return intersectionInCurrentDomain(Range.range(fromElement, BoundType.forBoolean(fromInclusive), toElement, BoundType.forBoolean(toInclusive)));
  }
  
  ContiguousSet<C> tailSetImpl(C fromElement, boolean inclusive)
  {
    return intersectionInCurrentDomain(Range.downTo(fromElement, BoundType.forBoolean(inclusive)));
  }
  
  @GwtIncompatible("not used by GWT emulation")
  int indexOf(Object target)
  {
    return contains(target) ? (int)this.domain.distance(first(), (Comparable)target) : -1;
  }
  
  public UnmodifiableIterator<C> iterator()
  {
    new AbstractSequentialIterator(first())
    {
      final C last = RegularContiguousSet.this.last();
      
      protected C computeNext(C previous)
      {
        return RegularContiguousSet.equalsOrThrow(previous, this.last) ? null : RegularContiguousSet.this.domain.next(previous);
      }
    };
  }
  
  @GwtIncompatible("NavigableSet")
  public UnmodifiableIterator<C> descendingIterator()
  {
    new AbstractSequentialIterator(last())
    {
      final C first = RegularContiguousSet.this.first();
      
      protected C computeNext(C previous)
      {
        return RegularContiguousSet.equalsOrThrow(previous, this.first) ? null : RegularContiguousSet.this.domain.previous(previous);
      }
    };
  }
  
  private static boolean equalsOrThrow(Comparable<?> left, @Nullable Comparable<?> right)
  {
    return (right != null) && (Range.compareOrThrow(left, right) == 0);
  }
  
  boolean isPartialView()
  {
    return false;
  }
  
  public C first()
  {
    return this.range.lowerBound.leastValueAbove(this.domain);
  }
  
  public C last()
  {
    return this.range.upperBound.greatestValueBelow(this.domain);
  }
  
  public int size()
  {
    long distance = this.domain.distance(first(), last());
    return distance >= 2147483647L ? Integer.MAX_VALUE : (int)distance + 1;
  }
  
  public boolean contains(@Nullable Object object)
  {
    if (object == null) {
      return false;
    }
    try
    {
      return this.range.contains((Comparable)object);
    }
    catch (ClassCastException e) {}
    return false;
  }
  
  public boolean containsAll(Collection<?> targets)
  {
    return Collections2.containsAllImpl(this, targets);
  }
  
  public boolean isEmpty()
  {
    return false;
  }
  
  public ContiguousSet<C> intersection(ContiguousSet<C> other)
  {
    Preconditions.checkNotNull(other);
    Preconditions.checkArgument(this.domain.equals(other.domain));
    if (other.isEmpty()) {
      return other;
    }
    C lowerEndpoint = (Comparable)Ordering.natural().max(first(), other.first());
    C upperEndpoint = (Comparable)Ordering.natural().min(last(), other.last());
    return lowerEndpoint.compareTo(upperEndpoint) < 0 ? ContiguousSet.create(Range.closed(lowerEndpoint, upperEndpoint), this.domain) : new EmptyContiguousSet(this.domain);
  }
  
  public Range<C> range()
  {
    return range(BoundType.CLOSED, BoundType.CLOSED);
  }
  
  public Range<C> range(BoundType lowerBoundType, BoundType upperBoundType)
  {
    return Range.create(this.range.lowerBound.withLowerBoundType(lowerBoundType, this.domain), this.range.upperBound.withUpperBoundType(upperBoundType, this.domain));
  }
  
  public boolean equals(@Nullable Object object)
  {
    if (object == this) {
      return true;
    }
    if ((object instanceof RegularContiguousSet))
    {
      RegularContiguousSet<?> that = (RegularContiguousSet)object;
      if (this.domain.equals(that.domain)) {
        return (first().equals(that.first())) && (last().equals(that.last()));
      }
    }
    return super.equals(object);
  }
  
  public int hashCode()
  {
    return Sets.hashCodeImpl(this);
  }
  
  @GwtIncompatible("serialization")
  private static final class SerializedForm<C extends Comparable>
    implements Serializable
  {
    final Range<C> range;
    final DiscreteDomain<C> domain;
    
    private SerializedForm(Range<C> range, DiscreteDomain<C> domain)
    {
      this.range = range;
      this.domain = domain;
    }
    
    private Object readResolve()
    {
      return new RegularContiguousSet(this.range, this.domain);
    }
  }
  
  @GwtIncompatible("serialization")
  Object writeReplace()
  {
    return new SerializedForm(this.range, this.domain, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\RegularContiguousSet.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */