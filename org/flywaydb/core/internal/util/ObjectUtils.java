package org.flywaydb.core.internal.util;

public class ObjectUtils
{
  public static boolean nullSafeEquals(Object o1, Object o2)
  {
    if (o1 == o2) {
      return true;
    }
    if ((o1 == null) || (o2 == null)) {
      return false;
    }
    return o1.equals(o2);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\ObjectUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */