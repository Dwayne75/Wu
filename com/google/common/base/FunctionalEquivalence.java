package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import java.io.Serializable;
import javax.annotation.Nullable;

@Beta
@GwtCompatible
final class FunctionalEquivalence<F, T>
  extends Equivalence<F>
  implements Serializable
{
  private static final long serialVersionUID = 0L;
  private final Function<F, ? extends T> function;
  private final Equivalence<T> resultEquivalence;
  
  FunctionalEquivalence(Function<F, ? extends T> function, Equivalence<T> resultEquivalence)
  {
    this.function = ((Function)Preconditions.checkNotNull(function));
    this.resultEquivalence = ((Equivalence)Preconditions.checkNotNull(resultEquivalence));
  }
  
  protected boolean doEquivalent(F a, F b)
  {
    return this.resultEquivalence.equivalent(this.function.apply(a), this.function.apply(b));
  }
  
  protected int doHash(F a)
  {
    return this.resultEquivalence.hash(this.function.apply(a));
  }
  
  public boolean equals(@Nullable Object obj)
  {
    if (obj == this) {
      return true;
    }
    if ((obj instanceof FunctionalEquivalence))
    {
      FunctionalEquivalence<?, ?> that = (FunctionalEquivalence)obj;
      return (this.function.equals(that.function)) && (this.resultEquivalence.equals(that.resultEquivalence));
    }
    return false;
  }
  
  public int hashCode()
  {
    return Objects.hashCode(new Object[] { this.function, this.resultEquivalence });
  }
  
  public String toString()
  {
    String str1 = String.valueOf(String.valueOf(this.resultEquivalence));String str2 = String.valueOf(String.valueOf(this.function));return 13 + str1.length() + str2.length() + str1 + ".onResultOf(" + str2 + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\base\FunctionalEquivalence.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */