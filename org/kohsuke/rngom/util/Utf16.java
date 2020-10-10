package org.kohsuke.rngom.util;

public abstract class Utf16
{
  public static boolean isSurrogate(char c)
  {
    return (c & 0xF800) == 55296;
  }
  
  public static boolean isSurrogate1(char c)
  {
    return (c & 0xFC00) == 55296;
  }
  
  public static boolean isSurrogate2(char c)
  {
    return (c & 0xFC00) == 56320;
  }
  
  public static int scalarValue(char c1, char c2)
  {
    return ((c1 & 0x3FF) << '\n' | c2 & 0x3FF) + 65536;
  }
  
  public static char surrogate1(int c)
  {
    return (char)(c - 65536 >> 10 | 0xD800);
  }
  
  public static char surrogate2(int c)
  {
    return (char)(c - 65536 & 0x3FF | 0xDC00);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\util\Utf16.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */