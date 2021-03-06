package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

@GwtCompatible(emulated=true)
public final class Predicates
{
  @GwtCompatible(serializable=true)
  public static <T> Predicate<T> alwaysTrue()
  {
    return ObjectPredicate.ALWAYS_TRUE.withNarrowedType();
  }
  
  @GwtCompatible(serializable=true)
  public static <T> Predicate<T> alwaysFalse()
  {
    return ObjectPredicate.ALWAYS_FALSE.withNarrowedType();
  }
  
  @GwtCompatible(serializable=true)
  public static <T> Predicate<T> isNull()
  {
    return ObjectPredicate.IS_NULL.withNarrowedType();
  }
  
  @GwtCompatible(serializable=true)
  public static <T> Predicate<T> notNull()
  {
    return ObjectPredicate.NOT_NULL.withNarrowedType();
  }
  
  public static <T> Predicate<T> not(Predicate<T> predicate)
  {
    return new NotPredicate(predicate);
  }
  
  public static <T> Predicate<T> and(Iterable<? extends Predicate<? super T>> components)
  {
    return new AndPredicate(defensiveCopy(components), null);
  }
  
  public static <T> Predicate<T> and(Predicate<? super T>... components)
  {
    return new AndPredicate(defensiveCopy(components), null);
  }
  
  public static <T> Predicate<T> and(Predicate<? super T> first, Predicate<? super T> second)
  {
    return new AndPredicate(asList((Predicate)Preconditions.checkNotNull(first), (Predicate)Preconditions.checkNotNull(second)), null);
  }
  
  public static <T> Predicate<T> or(Iterable<? extends Predicate<? super T>> components)
  {
    return new OrPredicate(defensiveCopy(components), null);
  }
  
  public static <T> Predicate<T> or(Predicate<? super T>... components)
  {
    return new OrPredicate(defensiveCopy(components), null);
  }
  
  public static <T> Predicate<T> or(Predicate<? super T> first, Predicate<? super T> second)
  {
    return new OrPredicate(asList((Predicate)Preconditions.checkNotNull(first), (Predicate)Preconditions.checkNotNull(second)), null);
  }
  
  public static <T> Predicate<T> equalTo(@Nullable T target)
  {
    return target == null ? isNull() : new IsEqualToPredicate(target, null);
  }
  
  @GwtIncompatible("Class.isInstance")
  public static Predicate<Object> instanceOf(Class<?> clazz)
  {
    return new InstanceOfPredicate(clazz, null);
  }
  
  @GwtIncompatible("Class.isAssignableFrom")
  @Beta
  public static Predicate<Class<?>> assignableFrom(Class<?> clazz)
  {
    return new AssignableFromPredicate(clazz, null);
  }
  
  public static <T> Predicate<T> in(Collection<? extends T> target)
  {
    return new InPredicate(target, null);
  }
  
  public static <A, B> Predicate<A> compose(Predicate<B> predicate, Function<A, ? extends B> function)
  {
    return new CompositionPredicate(predicate, function, null);
  }
  
  @GwtIncompatible("java.util.regex.Pattern")
  public static Predicate<CharSequence> containsPattern(String pattern)
  {
    return new ContainsPatternFromStringPredicate(pattern);
  }
  
  @GwtIncompatible("java.util.regex.Pattern")
  public static Predicate<CharSequence> contains(Pattern pattern)
  {
    return new ContainsPatternPredicate(pattern);
  }
  
  static abstract enum ObjectPredicate
    implements Predicate<Object>
  {
    ALWAYS_TRUE,  ALWAYS_FALSE,  IS_NULL,  NOT_NULL;
    
    private ObjectPredicate() {}
    
    <T> Predicate<T> withNarrowedType()
    {
      return this;
    }
  }
  
  private static class NotPredicate<T>
    implements Predicate<T>, Serializable
  {
    final Predicate<T> predicate;
    private static final long serialVersionUID = 0L;
    
    NotPredicate(Predicate<T> predicate)
    {
      this.predicate = ((Predicate)Preconditions.checkNotNull(predicate));
    }
    
    public boolean apply(@Nullable T t)
    {
      return !this.predicate.apply(t);
    }
    
    public int hashCode()
    {
      return this.predicate.hashCode() ^ 0xFFFFFFFF;
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof NotPredicate))
      {
        NotPredicate<?> that = (NotPredicate)obj;
        return this.predicate.equals(that.predicate);
      }
      return false;
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.predicate.toString()));return 16 + str.length() + "Predicates.not(" + str + ")";
    }
  }
  
  private static final Joiner COMMA_JOINER = Joiner.on(',');
  
  private static class AndPredicate<T>
    implements Predicate<T>, Serializable
  {
    private final List<? extends Predicate<? super T>> components;
    private static final long serialVersionUID = 0L;
    
    private AndPredicate(List<? extends Predicate<? super T>> components)
    {
      this.components = components;
    }
    
    public boolean apply(@Nullable T t)
    {
      for (int i = 0; i < this.components.size(); i++) {
        if (!((Predicate)this.components.get(i)).apply(t)) {
          return false;
        }
      }
      return true;
    }
    
    public int hashCode()
    {
      return this.components.hashCode() + 306654252;
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof AndPredicate))
      {
        AndPredicate<?> that = (AndPredicate)obj;
        return this.components.equals(that.components);
      }
      return false;
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(Predicates.COMMA_JOINER.join(this.components)));return 16 + str.length() + "Predicates.and(" + str + ")";
    }
  }
  
  private static class OrPredicate<T>
    implements Predicate<T>, Serializable
  {
    private final List<? extends Predicate<? super T>> components;
    private static final long serialVersionUID = 0L;
    
    private OrPredicate(List<? extends Predicate<? super T>> components)
    {
      this.components = components;
    }
    
    public boolean apply(@Nullable T t)
    {
      for (int i = 0; i < this.components.size(); i++) {
        if (((Predicate)this.components.get(i)).apply(t)) {
          return true;
        }
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.components.hashCode() + 87855567;
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof OrPredicate))
      {
        OrPredicate<?> that = (OrPredicate)obj;
        return this.components.equals(that.components);
      }
      return false;
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(Predicates.COMMA_JOINER.join(this.components)));return 15 + str.length() + "Predicates.or(" + str + ")";
    }
  }
  
  private static class IsEqualToPredicate<T>
    implements Predicate<T>, Serializable
  {
    private final T target;
    private static final long serialVersionUID = 0L;
    
    private IsEqualToPredicate(T target)
    {
      this.target = target;
    }
    
    public boolean apply(T t)
    {
      return this.target.equals(t);
    }
    
    public int hashCode()
    {
      return this.target.hashCode();
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof IsEqualToPredicate))
      {
        IsEqualToPredicate<?> that = (IsEqualToPredicate)obj;
        return this.target.equals(that.target);
      }
      return false;
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.target));return 20 + str.length() + "Predicates.equalTo(" + str + ")";
    }
  }
  
  @GwtIncompatible("Class.isInstance")
  private static class InstanceOfPredicate
    implements Predicate<Object>, Serializable
  {
    private final Class<?> clazz;
    private static final long serialVersionUID = 0L;
    
    private InstanceOfPredicate(Class<?> clazz)
    {
      this.clazz = ((Class)Preconditions.checkNotNull(clazz));
    }
    
    public boolean apply(@Nullable Object o)
    {
      return this.clazz.isInstance(o);
    }
    
    public int hashCode()
    {
      return this.clazz.hashCode();
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof InstanceOfPredicate))
      {
        InstanceOfPredicate that = (InstanceOfPredicate)obj;
        return this.clazz == that.clazz;
      }
      return false;
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.clazz.getName()));return 23 + str.length() + "Predicates.instanceOf(" + str + ")";
    }
  }
  
  @GwtIncompatible("Class.isAssignableFrom")
  private static class AssignableFromPredicate
    implements Predicate<Class<?>>, Serializable
  {
    private final Class<?> clazz;
    private static final long serialVersionUID = 0L;
    
    private AssignableFromPredicate(Class<?> clazz)
    {
      this.clazz = ((Class)Preconditions.checkNotNull(clazz));
    }
    
    public boolean apply(Class<?> input)
    {
      return this.clazz.isAssignableFrom(input);
    }
    
    public int hashCode()
    {
      return this.clazz.hashCode();
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof AssignableFromPredicate))
      {
        AssignableFromPredicate that = (AssignableFromPredicate)obj;
        return this.clazz == that.clazz;
      }
      return false;
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.clazz.getName()));return 27 + str.length() + "Predicates.assignableFrom(" + str + ")";
    }
  }
  
  private static class InPredicate<T>
    implements Predicate<T>, Serializable
  {
    private final Collection<?> target;
    private static final long serialVersionUID = 0L;
    
    private InPredicate(Collection<?> target)
    {
      this.target = ((Collection)Preconditions.checkNotNull(target));
    }
    
    public boolean apply(@Nullable T t)
    {
      try
      {
        return this.target.contains(t);
      }
      catch (NullPointerException e)
      {
        return false;
      }
      catch (ClassCastException e) {}
      return false;
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof InPredicate))
      {
        InPredicate<?> that = (InPredicate)obj;
        return this.target.equals(that.target);
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.target.hashCode();
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.target));return 15 + str.length() + "Predicates.in(" + str + ")";
    }
  }
  
  private static class CompositionPredicate<A, B>
    implements Predicate<A>, Serializable
  {
    final Predicate<B> p;
    final Function<A, ? extends B> f;
    private static final long serialVersionUID = 0L;
    
    private CompositionPredicate(Predicate<B> p, Function<A, ? extends B> f)
    {
      this.p = ((Predicate)Preconditions.checkNotNull(p));
      this.f = ((Function)Preconditions.checkNotNull(f));
    }
    
    public boolean apply(@Nullable A a)
    {
      return this.p.apply(this.f.apply(a));
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof CompositionPredicate))
      {
        CompositionPredicate<?, ?> that = (CompositionPredicate)obj;
        return (this.f.equals(that.f)) && (this.p.equals(that.p));
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.f.hashCode() ^ this.p.hashCode();
    }
    
    public String toString()
    {
      String str1 = String.valueOf(String.valueOf(this.p.toString()));String str2 = String.valueOf(String.valueOf(this.f.toString()));return 2 + str1.length() + str2.length() + str1 + "(" + str2 + ")";
    }
  }
  
  @GwtIncompatible("Only used by other GWT-incompatible code.")
  private static class ContainsPatternPredicate
    implements Predicate<CharSequence>, Serializable
  {
    final Pattern pattern;
    private static final long serialVersionUID = 0L;
    
    ContainsPatternPredicate(Pattern pattern)
    {
      this.pattern = ((Pattern)Preconditions.checkNotNull(pattern));
    }
    
    public boolean apply(CharSequence t)
    {
      return this.pattern.matcher(t).find();
    }
    
    public int hashCode()
    {
      return Objects.hashCode(new Object[] { this.pattern.pattern(), Integer.valueOf(this.pattern.flags()) });
    }
    
    public boolean equals(@Nullable Object obj)
    {
      if ((obj instanceof ContainsPatternPredicate))
      {
        ContainsPatternPredicate that = (ContainsPatternPredicate)obj;
        
        return (Objects.equal(this.pattern.pattern(), that.pattern.pattern())) && (Objects.equal(Integer.valueOf(this.pattern.flags()), Integer.valueOf(that.pattern.flags())));
      }
      return false;
    }
    
    public String toString()
    {
      String patternString = Objects.toStringHelper(this.pattern).add("pattern", this.pattern.pattern()).add("pattern.flags", this.pattern.flags()).toString();
      
      String str1 = String.valueOf(String.valueOf(patternString));return 21 + str1.length() + "Predicates.contains(" + str1 + ")";
    }
  }
  
  @GwtIncompatible("Only used by other GWT-incompatible code.")
  private static class ContainsPatternFromStringPredicate
    extends Predicates.ContainsPatternPredicate
  {
    private static final long serialVersionUID = 0L;
    
    ContainsPatternFromStringPredicate(String string)
    {
      super();
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.pattern.pattern()));return 28 + str.length() + "Predicates.containsPattern(" + str + ")";
    }
  }
  
  private static <T> List<Predicate<? super T>> asList(Predicate<? super T> first, Predicate<? super T> second)
  {
    return Arrays.asList(new Predicate[] { first, second });
  }
  
  private static <T> List<T> defensiveCopy(T... array)
  {
    return defensiveCopy(Arrays.asList(array));
  }
  
  static <T> List<T> defensiveCopy(Iterable<T> iterable)
  {
    ArrayList<T> list = new ArrayList();
    for (T element : iterable) {
      list.add(Preconditions.checkNotNull(element));
    }
    return list;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\base\Predicates.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */