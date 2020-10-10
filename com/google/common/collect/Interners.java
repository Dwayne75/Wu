package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import java.util.concurrent.ConcurrentMap;

@Beta
public final class Interners
{
  public static <E> Interner<E> newStrongInterner()
  {
    ConcurrentMap<E, E> map = new MapMaker().makeMap();
    new Interner()
    {
      public E intern(E sample)
      {
        E canonical = this.val$map.putIfAbsent(Preconditions.checkNotNull(sample), sample);
        return canonical == null ? sample : canonical;
      }
    };
  }
  
  @GwtIncompatible("java.lang.ref.WeakReference")
  public static <E> Interner<E> newWeakInterner()
  {
    return new WeakInterner(null);
  }
  
  private static class WeakInterner<E>
    implements Interner<E>
  {
    private final MapMakerInternalMap<E, Dummy> map;
    
    private WeakInterner()
    {
      this.map = new MapMaker().weakKeys().keyEquivalence(Equivalence.equals()).makeCustomMap();
    }
    
    public E intern(E sample)
    {
      for (;;)
      {
        MapMakerInternalMap.ReferenceEntry<E, Dummy> entry = this.map.getEntry(sample);
        if (entry != null)
        {
          E canonical = entry.getKey();
          if (canonical != null) {
            return canonical;
          }
        }
        Dummy sneaky = (Dummy)this.map.putIfAbsent(sample, Dummy.VALUE);
        if (sneaky == null) {
          return sample;
        }
      }
    }
    
    private static enum Dummy
    {
      VALUE;
      
      private Dummy() {}
    }
  }
  
  public static <E> Function<E, E> asFunction(Interner<E> interner)
  {
    return new InternerFunction((Interner)Preconditions.checkNotNull(interner));
  }
  
  private static class InternerFunction<E>
    implements Function<E, E>
  {
    private final Interner<E> interner;
    
    public InternerFunction(Interner<E> interner)
    {
      this.interner = interner;
    }
    
    public E apply(E input)
    {
      return (E)this.interner.intern(input);
    }
    
    public int hashCode()
    {
      return this.interner.hashCode();
    }
    
    public boolean equals(Object other)
    {
      if ((other instanceof InternerFunction))
      {
        InternerFunction<?> that = (InternerFunction)other;
        return this.interner.equals(that.interner);
      }
      return false;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\Interners.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */