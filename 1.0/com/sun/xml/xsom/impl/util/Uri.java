package com.sun.xml.xsom.impl.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

public class Uri
{
  private static String utf8 = "UTF-8";
  private static final String HEX_DIGITS = "0123456789abcdef";
  
  public static boolean isValid(String s)
  {
    return (isValidPercent(s)) && (isValidFragment(s)) && (isValidScheme(s));
  }
  
  public static String escapeDisallowedChars(String s)
  {
    StringBuffer buf = null;
    int len = s.length();
    int done = 0;
    for (;;)
    {
      int i = done;
      for (;;)
      {
        if (i == len)
        {
          if (done != 0) {
            break;
          }
          return s;
        }
        if (isExcluded(s.charAt(i))) {
          break;
        }
        i++;
      }
      if (buf == null) {
        buf = new StringBuffer();
      }
      if (i > done)
      {
        buf.append(s.substring(done, i));
        done = i;
      }
      if (i == len) {
        break;
      }
      for (i++; (i < len) && (isExcluded(s.charAt(i))); i++) {}
      String tem = s.substring(done, i);
      try
      {
        bytes = tem.getBytes(utf8);
      }
      catch (UnsupportedEncodingException e)
      {
        byte[] bytes;
        utf8 = "UTF8";
        try
        {
          bytes = tem.getBytes(utf8);
        }
        catch (UnsupportedEncodingException e2)
        {
          byte[] bytes;
          return s;
        }
      }
      byte[] bytes;
      for (int j = 0; j < bytes.length; j++)
      {
        buf.append('%');
        buf.append("0123456789abcdef".charAt((bytes[j] & 0xFF) >> 4));
        buf.append("0123456789abcdef".charAt(bytes[j] & 0xF));
      }
      done = i;
    }
    return buf.toString();
  }
  
  private static String excluded = "<>\"{}|\\^`";
  
  private static boolean isExcluded(char c)
  {
    return (c <= ' ') || (c >= '') || (excluded.indexOf(c) >= 0);
  }
  
  private static boolean isAlpha(char c)
  {
    return (('a' <= c) && (c <= 'z')) || (('A' <= c) && (c <= 'Z'));
  }
  
  private static boolean isHexDigit(char c)
  {
    return (('a' <= c) && (c <= 'f')) || (('A' <= c) && (c <= 'F')) || (isDigit(c));
  }
  
  private static boolean isDigit(char c)
  {
    return ('0' <= c) && (c <= '9');
  }
  
  private static boolean isSchemeChar(char c)
  {
    return (isAlpha(c)) || (isDigit(c)) || (c == '+') || (c == '-') || (c == '.');
  }
  
  private static boolean isValidPercent(String s)
  {
    int len = s.length();
    for (int i = 0; i < len; i++) {
      if (s.charAt(i) == '%')
      {
        if (i + 2 >= len) {
          return false;
        }
        if ((!isHexDigit(s.charAt(i + 1))) || (!isHexDigit(s.charAt(i + 2)))) {
          return false;
        }
      }
    }
    return true;
  }
  
  private static boolean isValidFragment(String s)
  {
    int i = s.indexOf('#');
    return (i < 0) || (s.indexOf('#', i + 1) < 0);
  }
  
  private static boolean isValidScheme(String s)
  {
    if (!isAbsolute(s)) {
      return true;
    }
    int i = s.indexOf(':');
    if ((i == 0) || (i + 1 == s.length()) || (!isAlpha(s.charAt(0)))) {
      return false;
    }
    do
    {
      i--;
      if (i <= 0) {
        break;
      }
    } while (isSchemeChar(s.charAt(i)));
    return false;
    return true;
  }
  
  public static String resolve(String baseUri, String uriReference)
  {
    if ((!isAbsolute(uriReference)) && (baseUri != null) && (isAbsolute(baseUri))) {
      try
      {
        return new URL(new URL(baseUri), uriReference).toString();
      }
      catch (MalformedURLException e) {}
    }
    return uriReference;
  }
  
  public static boolean hasFragmentId(String uri)
  {
    return uri.indexOf('#') >= 0;
  }
  
  public static boolean isAbsolute(String uri)
  {
    int i = uri.indexOf(':');
    if (i < 0) {
      return false;
    }
    for (;;)
    {
      i--;
      if (i < 0) {
        break;
      }
      switch (uri.charAt(i))
      {
      case '#': 
      case '/': 
      case '?': 
        return false;
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\util\Uri.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */