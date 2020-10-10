package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Iterator;
import javax.annotation.Nullable;

@GwtCompatible(serializable=true)
final class ReverseOrdering<T>
  extends Ordering<T>
  implements Serializable
{
  final Ordering<? super T> forwardOrder;
  private static final long serialVersionUID = 0L;
  
  ReverseOrdering(Ordering<? super T> forwardOrder)
  {
    this.forwardOrder = ((Ordering)Preconditions.checkNotNull(forwardOrder));
  }
  
  public int compare(T a, T b)
  {
    return this.forwardOrder.compare(b, a);
  }
  
  public <S extends T> Ordering<S> reverse()
  {
    return this.forwardOrder;
  }
  
  public <E extends T> E min(E a, E b)
  {
    return (E)this.forwardOrder.max(a, b);
  }
  
  public <E extends T> E min(E a, E b, E c, E... rest)
  {
    return (E)this.forwardOrder.max(a, b, c, rest);
  }
  
  public <E extends T> E min(Iterator<E> iterator)
  {
    return (E)this.forwardOrder.max(iterator);
  }
  
  public <E extends T> E min(Iterable<E> iterable)
  {
    return (E)this.forwardOrder.max(iterable);
  }
  
  public <E extends T> E max(E a, E b)
  {
    return (E)this.forwardOrder.min(a, b);
  }
  
  public <E extends T> E max(E a, E b, E c, E... rest)
  {
    return (E)this.forwardOrder.min(a, b, c, rest);
  }
  
  public <E extends T> E max(Iterator<E> iterator)
  {
    return (E)this.forwardOrder.min(iterator);
  }
  
  public <E extends T> E max(Iterable<E> iterable)
  {
    return (E)this.forwardOrder.min(iterable);
  }
  
  public int hashCode()
  {
    return -this.forwardOrder.hashCode();
  }
  
  public boolean equals(@Nullable Object object)
  {
    if (object == this) {
      return true;
    }
    if ((object instanceof ReverseOrdering))
    {
      ReverseOrdering<?> that = (ReverseOrdering)object;
      return this.forwardOrder.equals(that.forwardOrder);
    }
    return false;
  }
  
  public String toString()
  {
    String str = String.valueOf(String.valueOf(this.forwardOrder));return 10 + str.length() + str + ".reverse()";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\ReverseOrdering.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */