package com.sun.xml.bind.v2.util;

import java.util.Map;
import java.util.Map.Entry;

public class TypeCast
{
  public static <K, V> Map<K, V> checkedCast(Map<?, ?> m, Class<K> keyType, Class<V> valueType)
  {
    if (m == null) {
      return null;
    }
    for (Map.Entry e : m.entrySet())
    {
      if (!keyType.isInstance(e.getKey())) {
        throw new ClassCastException(e.getKey().getClass().toString());
      }
      if (!valueType.isInstance(e.getValue())) {
        throw new ClassCastException(e.getValue().getClass().toString());
      }
    }
    return m;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\util\TypeCast.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */