package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import javax.annotation.Nullable;

@GwtCompatible(serializable=true)
final class ByFunctionOrdering<F, T>
  extends Ordering<F>
  implements Serializable
{
  final Function<F, ? extends T> function;
  final Ordering<T> ordering;
  private static final long serialVersionUID = 0L;
  
  ByFunctionOrdering(Function<F, ? extends T> function, Ordering<T> ordering)
  {
    this.function = ((Function)Preconditions.checkNotNull(function));
    this.ordering = ((Ordering)Preconditions.checkNotNull(ordering));
  }
  
  public int compare(F left, F right)
  {
    return this.ordering.compare(this.function.apply(left), this.function.apply(right));
  }
  
  public boolean equals(@Nullable Object object)
  {
    if (object == this) {
      return true;
    }
    if ((object instanceof ByFunctionOrdering))
    {
      ByFunctionOrdering<?, ?> that = (ByFunctionOrdering)object;
      return (this.function.equals(that.function)) && (this.ordering.equals(that.ordering));
    }
    return false;
  }
  
  public int hashCode()
  {
    return Objects.hashCode(new Object[] { this.function, this.ordering });
  }
  
  public String toString()
  {
    String str1 = String.valueOf(String.valueOf(this.ordering));String str2 = String.valueOf(String.valueOf(this.function));return 13 + str1.length() + str2.length() + str1 + ".onResultOf(" + str2 + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\ByFunctionOrdering.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */