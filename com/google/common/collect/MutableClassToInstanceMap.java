package com.google.common.collect;

import com.google.common.primitives.Primitives;
import java.util.HashMap;
import java.util.Map;

public final class MutableClassToInstanceMap<B>
  extends MapConstraints.ConstrainedMap<Class<? extends B>, B>
  implements ClassToInstanceMap<B>
{
  public static <B> MutableClassToInstanceMap<B> create()
  {
    return new MutableClassToInstanceMap(new HashMap());
  }
  
  public static <B> MutableClassToInstanceMap<B> create(Map<Class<? extends B>, B> backingMap)
  {
    return new MutableClassToInstanceMap(backingMap);
  }
  
  private MutableClassToInstanceMap(Map<Class<? extends B>, B> delegate)
  {
    super(delegate, VALUE_CAN_BE_CAST_TO_KEY);
  }
  
  private static final MapConstraint<Class<?>, Object> VALUE_CAN_BE_CAST_TO_KEY = new MapConstraint()
  {
    public void checkKeyValue(Class<?> key, Object value)
    {
      MutableClassToInstanceMap.cast(key, value);
    }
  };
  private static final long serialVersionUID = 0L;
  
  public <T extends B> T putInstance(Class<T> type, T value)
  {
    return (T)cast(type, put(type, value));
  }
  
  public <T extends B> T getInstance(Class<T> type)
  {
    return (T)cast(type, get(type));
  }
  
  private static <B, T extends B> T cast(Class<T> type, B value)
  {
    return (T)Primitives.wrap(type).cast(value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\collect\MutableClassToInstanceMap.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */