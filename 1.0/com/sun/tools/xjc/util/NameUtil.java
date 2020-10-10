package com.sun.tools.xjc.util;

import java.util.ArrayList;

public class NameUtil
{
  protected boolean isPunct(char c)
  {
    return (c == '-') || (c == '.') || (c == ':') || (c == '_') || (c == '·') || (c == '·') || (c == '۝') || (c == '۞');
  }
  
  protected boolean isDigit(char c)
  {
    return ((c >= '0') && (c <= '9')) || (Character.isDigit(c));
  }
  
  protected boolean isUpper(char c)
  {
    return ((c >= 'A') && (c <= 'Z')) || (Character.isUpperCase(c));
  }
  
  protected boolean isLower(char c)
  {
    return ((c >= 'a') && (c <= 'z')) || (Character.isLowerCase(c));
  }
  
  protected boolean isLetter(char c)
  {
    return ((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) || (Character.isLetter(c));
  }
  
  public String capitalize(String s)
  {
    if (!isLower(s.charAt(0))) {
      return s;
    }
    StringBuffer sb = new StringBuffer(s.length());
    sb.append(Character.toUpperCase(s.charAt(0)));
    sb.append(s.substring(1).toLowerCase());
    return sb.toString();
  }
  
  protected int nextBreak(String s, int start)
  {
    int n = s.length();
    for (int i = start; i < n; i++)
    {
      char c0 = s.charAt(i);
      if (i < n - 1)
      {
        char c1 = s.charAt(i + 1);
        if (isPunct(c1)) {
          return i + 1;
        }
        if ((isDigit(c0)) && (!isDigit(c1))) {
          return i + 1;
        }
        if ((!isDigit(c0)) && (isDigit(c1))) {
          return i + 1;
        }
        if ((isLower(c0)) && (!isLower(c1))) {
          return i + 1;
        }
        if ((isLetter(c0)) && (!isLetter(c1))) {
          return i + 1;
        }
        if ((!isLetter(c0)) && (isLetter(c1))) {
          return i + 1;
        }
        if (i < n - 2)
        {
          char c2 = s.charAt(i + 2);
          if ((isUpper(c0)) && (isUpper(c1)) && (isLower(c2))) {
            return i + 1;
          }
        }
      }
    }
    return -1;
  }
  
  public String[] toWordList(String s)
  {
    ArrayList ss = new ArrayList();
    int n = s.length();
    for (int i = 0; i < n;)
    {
      while ((i < n) && 
        (isPunct(s.charAt(i)))) {
        i++;
      }
      if (i >= n) {
        break;
      }
      int b = nextBreak(s, i);
      String w = b == -1 ? s.substring(i) : s.substring(i, b);
      ss.add(escape(capitalize(w)));
      if (b == -1) {
        break;
      }
      i = b;
    }
    return (String[])ss.toArray(new String[0]);
  }
  
  protected String toMixedCaseName(String[] ss, boolean startUpper)
  {
    StringBuffer sb = new StringBuffer();
    if (ss.length > 0)
    {
      sb.append(startUpper ? ss[0] : ss[0].toLowerCase());
      for (int i = 1; i < ss.length; i++) {
        sb.append(ss[i]);
      }
    }
    return sb.toString();
  }
  
  protected String toMixedCaseVariableName(String[] ss, boolean startUpper, boolean cdrUpper)
  {
    if (cdrUpper) {
      for (int i = 1; i < ss.length; i++) {
        ss[i] = capitalize(ss[i]);
      }
    }
    StringBuffer sb = new StringBuffer();
    if (ss.length > 0)
    {
      sb.append(startUpper ? ss[0] : ss[0].toLowerCase());
      for (int i = 1; i < ss.length; i++) {
        sb.append(ss[i]);
      }
    }
    return sb.toString();
  }
  
  public String toConstantName(String s)
  {
    return toConstantName(toWordList(s));
  }
  
  public String toConstantName(String[] ss)
  {
    StringBuffer sb = new StringBuffer();
    if (ss.length > 0)
    {
      sb.append(ss[0].toUpperCase());
      for (int i = 1; i < ss.length; i++)
      {
        sb.append('_');
        sb.append(ss[i].toUpperCase());
      }
    }
    return sb.toString();
  }
  
  public static void escape(StringBuffer sb, String s, int start)
  {
    int n = s.length();
    for (int i = start; i < n; i++)
    {
      char c = s.charAt(i);
      if (Character.isJavaIdentifierPart(c))
      {
        sb.append(c);
      }
      else
      {
        sb.append("_");
        if (c <= '\017') {
          sb.append("000");
        } else if (c <= 'ÿ') {
          sb.append("00");
        } else if (c <= '࿿') {
          sb.append("0");
        }
        sb.append(Integer.toString(c, 16));
      }
    }
  }
  
  private static String escape(String s)
  {
    int n = s.length();
    for (int i = 0; i < n; i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(i)))
      {
        StringBuffer sb = new StringBuffer(s.substring(0, i));
        escape(sb, s, i);
        return sb.toString();
      }
    }
    return s;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\util\NameUtil.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */