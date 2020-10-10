package com.sun.tools.xjc.util;

public final class Util
{
  public static final String getSystemProperty(String name)
  {
    try
    {
      return System.getProperty(name);
    }
    catch (SecurityException e) {}
    return null;
  }
  
  public static final String getSystemProperty(Class clazz, String name)
  {
    return getSystemProperty(clazz.getName() + '.' + name);
  }
  
  public static int calculateInitialHashMapCapacity(int count, float loadFactor)
  {
    int initialCapacity = (int)Math.ceil(count / loadFactor) + 1;
    if (initialCapacity < 16) {
      return 16;
    }
    return initialCapacity;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\util\Util.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */