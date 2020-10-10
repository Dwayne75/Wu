package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

@GwtCompatible(emulated=true)
@Beta
public final class Enums
{
  @GwtIncompatible("reflection")
  public static Field getField(Enum<?> enumValue)
  {
    Class<?> clazz = enumValue.getDeclaringClass();
    try
    {
      return clazz.getDeclaredField(enumValue.name());
    }
    catch (NoSuchFieldException impossible)
    {
      throw new AssertionError(impossible);
    }
  }
  
  public static <T extends Enum<T>> Optional<T> getIfPresent(Class<T> enumClass, String value)
  {
    Preconditions.checkNotNull(enumClass);
    Preconditions.checkNotNull(value);
    return Platform.getEnumIfPresent(enumClass, value);
  }
  
  @GwtIncompatible("java.lang.ref.WeakReference")
  private static final Map<Class<? extends Enum<?>>, Map<String, WeakReference<? extends Enum<?>>>> enumConstantCache = new WeakHashMap();
  
  @GwtIncompatible("java.lang.ref.WeakReference")
  private static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> populateCache(Class<T> enumClass)
  {
    Map<String, WeakReference<? extends Enum<?>>> result = new HashMap();
    for (T enumInstance : EnumSet.allOf(enumClass)) {
      result.put(enumInstance.name(), new WeakReference(enumInstance));
    }
    enumConstantCache.put(enumClass, result);
    return result;
  }
  
  @GwtIncompatible("java.lang.ref.WeakReference")
  static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> getEnumConstants(Class<T> enumClass)
  {
    synchronized (enumConstantCache)
    {
      Map<String, WeakReference<? extends Enum<?>>> constants = (Map)enumConstantCache.get(enumClass);
      if (constants == null) {
        constants = populateCache(enumClass);
      }
      return constants;
    }
  }
  
  public static <T extends Enum<T>> Converter<String, T> stringConverter(Class<T> enumClass)
  {
    return new StringConverter(enumClass);
  }
  
  private static final class StringConverter<T extends Enum<T>>
    extends Converter<String, T>
    implements Serializable
  {
    private final Class<T> enumClass;
    private static final long serialVersionUID = 0L;
    
    StringConverter(Class<T> enumClass)
    {
      this.enumClass = ((Class)Preconditions.checkNotNull(enumClass));
    }
    
    protected T doForward(String value)
    {
      return Enum.valueOf(this.enumClass, value);
    }
    
    protected String doBackward(T enumValue)
    {
      return enumValue.name();
    }
    
    public boolean equals(@Nullable Object object)
    {
      if ((object instanceof StringConverter))
      {
        StringConverter<?> that = (StringConverter)object;
        return this.enumClass.equals(that.enumClass);
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.enumClass.hashCode();
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.enumClass.getName()));return 29 + str.length() + "Enums.stringConverter(" + str + ".class)";
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\base\Enums.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */