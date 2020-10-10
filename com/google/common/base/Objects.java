package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import java.util.Arrays;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

@GwtCompatible
public final class Objects
{
  @CheckReturnValue
  public static boolean equal(@Nullable Object a, @Nullable Object b)
  {
    return (a == b) || ((a != null) && (a.equals(b)));
  }
  
  public static int hashCode(@Nullable Object... objects)
  {
    return Arrays.hashCode(objects);
  }
  
  @Deprecated
  public static ToStringHelper toStringHelper(Object self)
  {
    return new ToStringHelper(MoreObjects.simpleName(self.getClass()), null);
  }
  
  @Deprecated
  public static ToStringHelper toStringHelper(Class<?> clazz)
  {
    return new ToStringHelper(MoreObjects.simpleName(clazz), null);
  }
  
  @Deprecated
  public static ToStringHelper toStringHelper(String className)
  {
    return new ToStringHelper(className, null);
  }
  
  @Deprecated
  public static <T> T firstNonNull(@Nullable T first, @Nullable T second)
  {
    return (T)MoreObjects.firstNonNull(first, second);
  }
  
  @Deprecated
  public static final class ToStringHelper
  {
    private final String className;
    private ValueHolder holderHead = new ValueHolder(null);
    private ValueHolder holderTail = this.holderHead;
    private boolean omitNullValues = false;
    
    private ToStringHelper(String className)
    {
      this.className = ((String)Preconditions.checkNotNull(className));
    }
    
    public ToStringHelper omitNullValues()
    {
      this.omitNullValues = true;
      return this;
    }
    
    public ToStringHelper add(String name, @Nullable Object value)
    {
      return addHolder(name, value);
    }
    
    public ToStringHelper add(String name, boolean value)
    {
      return addHolder(name, String.valueOf(value));
    }
    
    public ToStringHelper add(String name, char value)
    {
      return addHolder(name, String.valueOf(value));
    }
    
    public ToStringHelper add(String name, double value)
    {
      return addHolder(name, String.valueOf(value));
    }
    
    public ToStringHelper add(String name, float value)
    {
      return addHolder(name, String.valueOf(value));
    }
    
    public ToStringHelper add(String name, int value)
    {
      return addHolder(name, String.valueOf(value));
    }
    
    public ToStringHelper add(String name, long value)
    {
      return addHolder(name, String.valueOf(value));
    }
    
    public ToStringHelper addValue(@Nullable Object value)
    {
      return addHolder(value);
    }
    
    public ToStringHelper addValue(boolean value)
    {
      return addHolder(String.valueOf(value));
    }
    
    public ToStringHelper addValue(char value)
    {
      return addHolder(String.valueOf(value));
    }
    
    public ToStringHelper addValue(double value)
    {
      return addHolder(String.valueOf(value));
    }
    
    public ToStringHelper addValue(float value)
    {
      return addHolder(String.valueOf(value));
    }
    
    public ToStringHelper addValue(int value)
    {
      return addHolder(String.valueOf(value));
    }
    
    public ToStringHelper addValue(long value)
    {
      return addHolder(String.valueOf(value));
    }
    
    public String toString()
    {
      boolean omitNullValuesSnapshot = this.omitNullValues;
      String nextSeparator = "";
      StringBuilder builder = new StringBuilder(32).append(this.className).append('{');
      for (ValueHolder valueHolder = this.holderHead.next; valueHolder != null; valueHolder = valueHolder.next) {
        if ((!omitNullValuesSnapshot) || (valueHolder.value != null))
        {
          builder.append(nextSeparator);
          nextSeparator = ", ";
          if (valueHolder.name != null) {
            builder.append(valueHolder.name).append('=');
          }
          builder.append(valueHolder.value);
        }
      }
      return '}';
    }
    
    private ValueHolder addHolder()
    {
      ValueHolder valueHolder = new ValueHolder(null);
      this.holderTail = (this.holderTail.next = valueHolder);
      return valueHolder;
    }
    
    private ToStringHelper addHolder(@Nullable Object value)
    {
      ValueHolder valueHolder = addHolder();
      valueHolder.value = value;
      return this;
    }
    
    private ToStringHelper addHolder(String name, @Nullable Object value)
    {
      ValueHolder valueHolder = addHolder();
      valueHolder.value = value;
      valueHolder.name = ((String)Preconditions.checkNotNull(name));
      return this;
    }
    
    private static final class ValueHolder
    {
      String name;
      Object value;
      ValueHolder next;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\base\Objects.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */