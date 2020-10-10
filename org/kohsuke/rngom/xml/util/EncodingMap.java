package org.kohsuke.rngom.xml.util;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public abstract class EncodingMap
{
  private static final String[] aliases = { "UTF-8", "UTF8", "UTF-16", "Unicode", "UTF-16BE", "UnicodeBigUnmarked", "UTF-16LE", "UnicodeLittleUnmarked", "US-ASCII", "ASCII", "TIS-620", "TIS620" };
  
  public static String getJavaName(String enc)
  {
    int i;
    try
    {
      "x".getBytes(enc);
    }
    catch (UnsupportedEncodingException e)
    {
      i = 0;
    }
    for (; i < aliases.length; i += 2) {
      if (enc.equalsIgnoreCase(aliases[i])) {
        try
        {
          "x".getBytes(aliases[(i + 1)]);
          return aliases[(i + 1)];
        }
        catch (UnsupportedEncodingException e2) {}
      }
    }
    return enc;
  }
  
  public static void main(String[] args)
  {
    System.err.println(getJavaName(args[0]));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\xml\util\EncodingMap.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */