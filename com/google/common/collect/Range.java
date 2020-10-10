package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import javax.annotation.Nullable;

@GwtCompatible
public final class Range<C extends Comparable>
  implements Predicate<C>, Serializable
{
  private static final Function<Range, Cut> LOWER_BOUND_FN = new Function()
  {
    public Cut apply(Range range)
    {
      return range.lowerBound;
    }
  };
  
  static <C extends Comparable<?>> Function<Range<C>, Cut<C>> lowerBoundFn()
  {
    return LOWER_BOUND_FN;
  }
  
  private static final Function<Range, Cut> UPPER_BOUND_FN = new Function()
  {
    public Cut apply(Range range)
    {
      return range.upperBound;
    }
  };
  
  static <C extends Comparable<?>> Function<Range<C>, Cut<C>> upperBoundFn()
  {
    return UPPER_BOUND_FN;
  }
  
  static final Ordering<Range<?>> RANGE_LEX_ORDERING = new Ordering()
  {
    public int compare(Range<?> left, Range<?> right)
    {
      return ComparisonChain.start().compare(left.lowerBound, right.lowerBound).compare(left.upperBound, right.upperBound).result();
    }
  };
  
  static <C extends Comparable<?>> Range<C> create(Cut<C> lowerBound, Cut<C> upperBound)
  {
    return new Range(lowerBound, upperBound);
  }
  
  public static <C extends Comparable<?>> Range<C> open(C lower, C upper)
  {
    return create(Cut.aboveValue(lower), Cut.belowValue(upper));
  }
  
  public static <C extends Comparable<?>> Range<C> closed(C lower, C upper)
  {
    return create(Cut.belowValue(lower), Cut.aboveValue(upper));
  }
  
  public static <C extends Comparable<?>> Range<C> closedOpen(C lower, C upper)
  {
    return create(Cut.belowValue(lower), Cut.belowValue(upper));
  }
  
  public static <C extends Comparable<?>> Range<C> openClosed(C lower, C upper)
  {
    return create(Cut.aboveValue(lower), Cut.aboveValue(upper));
  }
  
  public static <C extends Comparable<?>> Range<C> range(C lower, BoundType lowerType, C upper, BoundType upperType)
  {
    Preconditions.checkNotNull(lowerType);
    Preconditions.checkNotNull(upperType);
    
    Cut<C> lowerBound = lowerType == BoundType.OPEN ? Cut.aboveValue(lower) : Cut.belowValue(lower);
    
    Cut<C> upperBound = upperType == BoundType.OPEN ? Cut.belowValue(upper) : Cut.aboveValue(upper);
    
    return create(lowerBound, upperBound);
  }
  
  public static <C extends Comparable<?>> Range<C> lessThan(C endpoint)
  {
    return create(Cut.belowAll(), Cut.belowValue(endpoint));
  }
  
  public static <C extends Comparable<?>> Range<C> atMost(C endpoint)
  {
    return create(Cut.belowAll(), Cut.aboveValue(endpoint));
  }
  
  public static <C extends Comparable<?>> Range<C> upTo(C endpoint, BoundType boundType)
  {
    switch (boundType)
    {
    case OPEN: 
      return lessThan(endpoint);
    case CLOSED: 
      return atMost(endpoint);
    }
    throw new AssertionError();
  }
  
  public static <C extends Comparable<?>> Range<C> greaterThan(C endpoint)
  {
    return create(Cut.aboveValue(endpoint), Cut.aboveAll());
  }
  
  public static <C extends Comparable<?>> Range<C> atLeast(C endpoint)
  {
    return create(Cut.belowValue(endpoint), Cut.aboveAll());
  }
  
  public static <C extends Comparable<?>> Range<C> downTo(C endpoint, BoundType boundType)
  {
    switch (boundType)
    {
    case OPEN: 
      return greaterThan(endpoint);
    case CLOSED: 
      return atLeast(endpoint);
    }
    throw new AssertionError();
  }
  
  private static final Range<Comparable> ALL = new Range(Cut.belowAll(), Cut.aboveAll());
  final Cut<C> lowerBound;
  final Cut<C> upperBound;
  private static final long serialVersionUID = 0L;
  
  public static <C extends Comparable<?>> Range<C> all()
  {
    return ALL;
  }
  
  public static <C extends Comparable<?>> Range<C> singleton(C value)
  {
    return closed(value, value);
  }
  
  public static <C extends Comparable<?>> Range<C> encloseAll(Iterable<C> values)
  {
    Preconditions.checkNotNull(values);
    if ((values instanceof ContiguousSet)) {
      return ((ContiguousSet)values).range();
    }
    Iterator<C> valueIterator = values.iterator();
    C min = (Comparable)Preconditions.checkNotNull(valueIterator.next());
    C max = min;
    while (valueIterator.hasNext())
    {
      C value = (Comparable)Preconditions.checkNotNull(valueIterator.next());
      min = (Comparable)Ordering.natural().min(min, value);
      max = (Comparable)Ordering.natural().max(max, value);
    }
    return closed(min, max);
  }
  
  /* Error */
  private Range(Cut<C> lowerBound, Cut<C> upperBound)
  {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial 31	java/lang/Object:<init>	()V
    //   4: aload_1
    //   5: aload_2
    //   6: invokevirtual 32	com/google/common/collect/Cut:compareTo	(Lcom/google/common/collect/Cut;)I
    //   9: ifgt +17 -> 26
    //   12: aload_1
    //   13: invokestatic 17	com/google/common/collect/Cut:aboveAll	()Lcom/google/common/collect/Cut;
    //   16: if_acmpeq +10 -> 26
    //   19: aload_2
    //   20: invokestatic 10	com/google/common/collect/Cut:belowAll	()Lcom/google/common/collect/Cut;
    //   23: if_acmpne +43 -> 66
    //   26: new 33	java/lang/IllegalArgumentException
    //   29: dup
    //   30: ldc 34
    //   32: aload_1
    //   33: aload_2
    //   34: invokestatic 35	com/google/common/collect/Range:toString	(Lcom/google/common/collect/Cut;Lcom/google/common/collect/Cut;)Ljava/lang/String;
    //   37: invokestatic 36	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   40: dup
    //   41: invokevirtual 37	java/lang/String:length	()I
    //   44: ifeq +9 -> 53
    //   47: invokevirtual 38	java/lang/String:concat	(Ljava/lang/String;)Ljava/lang/String;
    //   50: goto +12 -> 62
    //   53: pop
    //   54: new 39	java/lang/String
    //   57: dup_x1
    //   58: swap
    //   59: invokespecial 40	java/lang/String:<init>	(Ljava/lang/String;)V
    //   62: invokespecial 41	java/lang/IllegalArgumentException:<init>	(Ljava/lang/String;)V
    //   65: athrow
    //   66: aload_0
    //   67: aload_1
    //   68: invokestatic 8	com/google/common/base/Preconditions:checkNotNull	(Ljava/lang/Object;)Ljava/lang/Object;
    //   71: checkcast 42	com/google/common/collect/Cut
    //   74: putfield 43	com/google/common/collect/Range:lowerBound	Lcom/google/common/collect/Cut;
    //   77: aload_0
    //   78: aload_2
    //   79: invokestatic 8	com/google/common/base/Preconditions:checkNotNull	(Ljava/lang/Object;)Ljava/lang/Object;
    //   82: checkcast 42	com/google/common/collect/Cut
    //   85: putfield 44	com/google/common/collect/Range:upperBound	Lcom/google/common/collect/Cut;
    //   88: return
    // Line number table:
    //   Java source line #360	-> byte code offset #0
    //   Java source line #361	-> byte code offset #4
    //   Java source line #363	-> byte code offset #26
    //   Java source line #365	-> byte code offset #66
    //   Java source line #366	-> byte code offset #77
    //   Java source line #367	-> byte code offset #88
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	89	0	this	Range<C>
    //   0	89	1	lowerBound	Cut<C>
    //   0	89	2	upperBound	Cut<C>
  }
  
  public boolean hasLowerBound()
  {
    return this.lowerBound != Cut.belowAll();
  }
  
  public C lowerEndpoint()
  {
    return this.lowerBound.endpoint();
  }
  
  public BoundType lowerBoundType()
  {
    return this.lowerBound.typeAsLowerBound();
  }
  
  public boolean hasUpperBound()
  {
    return this.upperBound != Cut.aboveAll();
  }
  
  public C upperEndpoint()
  {
    return this.upperBound.endpoint();
  }
  
  public BoundType upperBoundType()
  {
    return this.upperBound.typeAsUpperBound();
  }
  
  public boolean isEmpty()
  {
    return this.lowerBound.equals(this.upperBound);
  }
  
  public boolean contains(C value)
  {
    Preconditions.checkNotNull(value);
    
    return (this.lowerBound.isLessThan(value)) && (!this.upperBound.isLessThan(value));
  }
  
  @Deprecated
  public boolean apply(C input)
  {
    return contains(input);
  }
  
  public boolean containsAll(Iterable<? extends C> values)
  {
    if (Iterables.isEmpty(values)) {
      return true;
    }
    if ((values instanceof SortedSet))
    {
      SortedSet<? extends C> set = cast(values);
      Comparator<?> comparator = set.comparator();
      if ((Ordering.natural().equals(comparator)) || (comparator == null)) {
        return (contains((Comparable)set.first())) && (contains((Comparable)set.last()));
      }
    }
    for (C value : values) {
      if (!contains(value)) {
        return false;
      }
    }
    return true;
  }
  
  public boolean encloses(Range<C> other)
  {
    return (this.lowerBound.compareTo(other.lowerBound) <= 0) && (this.upperBound.compareTo(other.upperBound) >= 0);
  }
  
  public boolean isConnected(Range<C> other)
  {
    return (this.lowerBound.compareTo(other.upperBound) <= 0) && (other.lowerBound.compareTo(this.upperBound) <= 0);
  }
  
  public Range<C> intersection(Range<C> connectedRange)
  {
    int lowerCmp = this.lowerBound.compareTo(connectedRange.lowerBound);
    int upperCmp = this.upperBound.compareTo(connectedRange.upperBound);
    if ((lowerCmp >= 0) && (upperCmp <= 0)) {
      return this;
    }
    if ((lowerCmp <= 0) && (upperCmp >= 0)) {
      return connectedRange;
    }
    Cut<C> newLower = lowerCmp >= 0 ? this.lowerBound : connectedRange.lowerBound;
    Cut<C> newUpper = upperCmp <= 0 ? this.upperBound : connectedRange.upperBound;
    return create(newLower, newUpper);
  }
  
  public Range<C> span(Range<C> other)
  {
    int lowerCmp = this.lowerBound.compareTo(other.lowerBound);
    int upperCmp = this.upperBound.compareTo(other.upperBound);
    if ((lowerCmp <= 0) && (upperCmp >= 0)) {
      return this;
    }
    if ((lowerCmp >= 0) && (upperCmp <= 0)) {
      return other;
    }
    Cut<C> newLower = lowerCmp <= 0 ? this.lowerBound : other.lowerBound;
    Cut<C> newUpper = upperCmp >= 0 ? this.upperBound : other.upperBound;
    return create(newLower, newUpper);
  }
  
  public Range<C> canonical(DiscreteDomain<C> domain)
  {
    Preconditions.checkNotNull(domain);
    Cut<C> lower = this.lowerBound.canonical(domain);
    Cut<C> upper = this.upperBound.canonical(domain);
    return (lower == this.lowerBound) && (upper == this.upperBound) ? this : create(lower, upper);
  }
  
  public boolean equals(@Nullable Object object)
  {
    if ((object instanceof Range))
    {
      Range<?> other = (Range)object;
      return (this.lowerBound.equals(other.lowerBound)) && (this.upperBound.equals(other.upperBound));
    }
    return false;
  }
  
  public int hashCode()
  {
    return this.lowerBound.hashCode() * 31 + this.upperBound.hashCode();
  }
  
  public String toString()
  {
    return toString(this.lowerBound, this.upperBound);
  }
  
  private static String toString(Cut<?> lowerBound, Cut<?> upperBound)
  {
    StringBuilder sb = new StringBuilder(16);
    lowerBound.describeAsLowerBound(sb);
    sb.append('‥');
    upperBound.describeAsUpperBound(sb);
    return sb.toString();
  }
  
  private static <T> SortedSet<T> cast(Iterable<T> iterable)
  {
    return (SortedSet)iterable;
  }
  
  Object readResolve()
  {
    if (equals(ALL)) {
      return all();
    }
    return this;
  }
  
  static int compareOrThrow(Comparable left, Comparable right)
  {
    return left.compareTo(right);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\Range.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */