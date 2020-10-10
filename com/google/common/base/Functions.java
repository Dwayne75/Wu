package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nullable;

@GwtCompatible
public final class Functions
{
  public static Function<Object, String> toStringFunction()
  {
    return ToStringFunction.INSTANCE;
  }
  
  private static enum ToStringFunction
    implements Function<Object, String>
  {
    INSTANCE;
    
    private ToStringFunction() {}
    
    public String apply(Object o)
    {
      Preconditions.checkNotNull(o);
      return o.toString();
    }
    
    public String toString()
    {
      return "toString";
    }
  }
  
  public static <E> Function<E, E> identity()
  {
    return IdentityFunction.INSTANCE;
  }
  
  private static enum IdentityFunction
    implements Function<Object, Object>
  {
    INSTANCE;
    
    private IdentityFunction() {}
    
    @Nullable
    public Object apply(@Nullable Object o)
    {
      return o;
    }
    
    public String toString()
    {
      return "identity";
    }
  }
  
  public static <K, V> Function<K, V> forMap(Map<K, V> map)
  {
    return new FunctionForMapNoDefault(map);
  }
  
  private static class FunctionForMapNoDefault<K, V>
    implements Function<K, V>, Serializable
  {
    final Map<K, V> map;
    private static final long serialVersionUID = 0L;
    
    FunctionForMapNoDefault(Map<K, V> map)
    {
      this.map = ((Map)Preconditions.checkNotNull(map));
    }
    
    public V apply(@Nullable K key)
    {
      V result = this.map.get(key);
      Preconditions.checkArgument((result != null) || (this.map.containsKey(key)), "Key '%s' not present in map", new Object[] { key });
      return result;
    }
    
    public boolean equals(@Nullable Object o)
    {
      if ((o instanceof FunctionForMapNoDefault))
      {
        FunctionForMapNoDefault<?, ?> that = (FunctionForMapNoDefault)o;
        return this.map.equals(that.map);
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.map.hashCode();
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.map));return 8 + str.length() + "forMap(" + str + ")";
    }
  }
  
  public static <K, V> Function<K, V> forMap(Map<K, ? extends V> map, @Nullable V defaultValue)
  {
    return new ForMapWithDefault(map, defaultValue);
  }
  
  private static class ForMapWithDefault<K, V>
    implements Function<K, V>, Serializable
  {
    final Map<K, ? extends V> map;
    final V defaultValue;
    private static final long serialVersionUID = 0L;
    
    ForMapWithDefault(Map<K, ? extends V> map, @Nullable V defaultValue)
    {
      this.map = ((Map)Preconditions.checkNotNull(map));
      this.defaultValue = defaultValue;
    }
    
    public V apply(@Nullable K key)
    {
      V result = this.map.get(key);
      return (result != null) || (this.map.containsKey(key)) ? result : this.defaultValue;
    }
    
    public boolean equals(@Nullable Object o)
    {
      if ((o instanceof ForMapWithDefault))
      {
        ForMapWithDefault<?, ?> that = (ForMapWithDefault)o;
        return (this.map.equals(that.map)) && (Objects.equal(this.defaultValue, that.defaultValue));
      }
      return false;
    }
    
    public int hashCode()
    {
      return Objects.hashCode(new Object[] { this.map, this.defaultValue });
    }
    
    public String toString()
    {
      String str1 = String.valueOf(String.valueOf(this.map));String str2 = String.valueOf(String.valueOf(this.defaultValue));return 23 + str1.length() + str2.length() + "forMap(" + str1 + ", defaultValue=" + str2 + ")";
    }
  }
  
  public static <A, B, C> Function<A, C> compose(Function<B, C> g, Function<A, ? extends B> f)
  {
    return new FunctionComposition(g, f);
  }
  
  private static class FunctionComposition<A, B, C>
    implements Function<A, C>, Serializable
  {
    private final Function<B, C> g;
    private final Function<A, ? extends B> f;
    private static final long serialVersionUID = 0L;
    
    public FunctionComposition(Function<B, C> g, Function<A, ? extends B> f)
    {
      this.g = ((Function)Preconditions.checkNotNull(g));
      this.f = ((Function)Preconditions.checkNotNull(f));
    }
    
    public C apply(@Nullable A a)
    {
      return (C)this.g.apply(this.f.apply(a));
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof FunctionComposition))
      {
        FunctionComposition<?, ?, ?> that = (FunctionComposition)obj;
        return (this.f.equals(that.f)) && (this.g.equals(that.g));
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.f.hashCode() ^ this.g.hashCode();
    }
    
    public String toString()
    {
      String str1 = String.valueOf(String.valueOf(this.g));String str2 = String.valueOf(String.valueOf(this.f));return 2 + str1.length() + str2.length() + str1 + "(" + str2 + ")";
    }
  }
  
  public static <T> Function<T, Boolean> forPredicate(Predicate<T> predicate)
  {
    return new PredicateFunction(predicate, null);
  }
  
  private static class PredicateFunction<T>
    implements Function<T, Boolean>, Serializable
  {
    private final Predicate<T> predicate;
    private static final long serialVersionUID = 0L;
    
    private PredicateFunction(Predicate<T> predicate)
    {
      this.predicate = ((Predicate)Preconditions.checkNotNull(predicate));
    }
    
    public Boolean apply(@Nullable T t)
    {
      return Boolean.valueOf(this.predicate.apply(t));
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof PredicateFunction))
      {
        PredicateFunction<?> that = (PredicateFunction)obj;
        return this.predicate.equals(that.predicate);
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.predicate.hashCode();
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.predicate));return 14 + str.length() + "forPredicate(" + str + ")";
    }
  }
  
  public static <E> Function<Object, E> constant(@Nullable E value)
  {
    return new ConstantFunction(value);
  }
  
  private static class ConstantFunction<E>
    implements Function<Object, E>, Serializable
  {
    private final E value;
    private static final long serialVersionUID = 0L;
    
    public ConstantFunction(@Nullable E value)
    {
      this.value = value;
    }
    
    public E apply(@Nullable Object from)
    {
      return (E)this.value;
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof ConstantFunction))
      {
        ConstantFunction<?> that = (ConstantFunction)obj;
        return Objects.equal(this.value, that.value);
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.value == null ? 0 : this.value.hashCode();
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.value));return 10 + str.length() + "constant(" + str + ")";
    }
  }
  
  @Beta
  public static <T> Function<Object, T> forSupplier(Supplier<T> supplier)
  {
    return new SupplierFunction(supplier, null);
  }
  
  private static class SupplierFunction<T>
    implements Function<Object, T>, Serializable
  {
    private final Supplier<T> supplier;
    private static final long serialVersionUID = 0L;
    
    private SupplierFunction(Supplier<T> supplier)
    {
      this.supplier = ((Supplier)Preconditions.checkNotNull(supplier));
    }
    
    public T apply(@Nullable Object input)
    {
      return (T)this.supplier.get();
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof SupplierFunction))
      {
        SupplierFunction<?> that = (SupplierFunction)obj;
        return this.supplier.equals(that.supplier);
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.supplier.hashCode();
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.supplier));return 13 + str.length() + "forSupplier(" + str + ")";
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\base\Functions.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */