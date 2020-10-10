package com.sun.tools.xjc.util;

import org.xml.sax.Locator;

public final class Util
{
  public static String getSystemProperty(String name)
  {
    try
    {
      return System.getProperty(name);
    }
    catch (SecurityException e) {}
    return null;
  }
  
  public static boolean equals(Locator lhs, Locator rhs)
  {
    return (lhs.getLineNumber() == rhs.getLineNumber()) && (lhs.getColumnNumber() == rhs.getColumnNumber()) && (equals(lhs.getSystemId(), rhs.getSystemId())) && (equals(lhs.getPublicId(), rhs.getPublicId()));
  }
  
  private static boolean equals(String lhs, String rhs)
  {
    if ((lhs == null) && (rhs == null)) {
      return true;
    }
    if ((lhs == null) || (rhs == null)) {
      return false;
    }
    return lhs.equals(rhs);
  }
  
  public static String getSystemProperty(Class clazz, String name)
  {
    return getSystemProperty(clazz.getName() + '.' + name);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\util\Util.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */