package com.sun.xml.bind.api.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class NameUtil
{
  protected static final int UPPER_LETTER = 0;
  protected static final int LOWER_LETTER = 1;
  protected static final int OTHER_LETTER = 2;
  protected static final int DIGIT = 3;
  protected static final int OTHER = 4;
  
  protected boolean isPunct(char c)
  {
    return (c == '-') || (c == '.') || (c == ':') || (c == '_') || (c == '·') || (c == '·') || (c == '۝') || (c == '۞');
  }
  
  protected static boolean isDigit(char c)
  {
    return ((c >= '0') && (c <= '9')) || (Character.isDigit(c));
  }
  
  protected static boolean isUpper(char c)
  {
    return ((c >= 'A') && (c <= 'Z')) || (Character.isUpperCase(c));
  }
  
  protected static boolean isLower(char c)
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
    StringBuilder sb = new StringBuilder(s.length());
    sb.append(Character.toUpperCase(s.charAt(0)));
    sb.append(s.substring(1).toLowerCase());
    return sb.toString();
  }
  
  private int nextBreak(String s, int start)
  {
    int n = s.length();
    
    char c1 = s.charAt(start);
    int t1 = classify(c1);
    for (int i = start + 1; i < n; i++)
    {
      int t0 = t1;
      
      c1 = s.charAt(i);
      t1 = classify(c1);
      switch (actionTable[(t0 * 5 + t1)])
      {
      case 0: 
        if (isPunct(c1)) {
          return i;
        }
        break;
      case 1: 
        if (i < n - 1)
        {
          char c2 = s.charAt(i + 1);
          if (isLower(c2)) {
            return i;
          }
        }
        break;
      case 2: 
        return i;
      }
    }
    return -1;
  }
  
  private static final byte[] actionTable = new byte[25];
  private static final byte ACTION_CHECK_PUNCT = 0;
  private static final byte ACTION_CHECK_C2 = 1;
  private static final byte ACTION_BREAK = 2;
  private static final byte ACTION_NOBREAK = 3;
  private static HashSet<String> reservedKeywords;
  
  private static byte decideAction(int t0, int t1)
  {
    if ((t0 == 4) && (t1 == 4)) {
      return 0;
    }
    if (!xor(t0 == 3, t1 == 3)) {
      return 2;
    }
    if ((t0 == 1) && (t1 != 1)) {
      return 2;
    }
    if (!xor(t0 <= 2, t1 <= 2)) {
      return 2;
    }
    if (!xor(t0 == 2, t1 == 2)) {
      return 2;
    }
    if ((t0 == 0) && (t1 == 0)) {
      return 1;
    }
    return 3;
  }
  
  private static boolean xor(boolean x, boolean y)
  {
    return ((x) && (y)) || ((!x) && (!y));
  }
  
  protected int classify(char c0)
  {
    switch (Character.getType(c0))
    {
    case 1: 
      return 0;
    case 2: 
      return 1;
    case 3: 
    case 4: 
    case 5: 
      return 2;
    case 9: 
      return 3;
    }
    return 4;
  }
  
  public List<String> toWordList(String s)
  {
    ArrayList<String> ss = new ArrayList();
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
    return ss;
  }
  
  protected String toMixedCaseName(List<String> ss, boolean startUpper)
  {
    StringBuilder sb = new StringBuilder();
    if (!ss.isEmpty())
    {
      sb.append(startUpper ? (String)ss.get(0) : ((String)ss.get(0)).toLowerCase());
      for (int i = 1; i < ss.size(); i++) {
        sb.append((String)ss.get(i));
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
    StringBuilder sb = new StringBuilder();
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
  
  public String toConstantName(List<String> ss)
  {
    StringBuilder sb = new StringBuilder();
    if (!ss.isEmpty())
    {
      sb.append(((String)ss.get(0)).toUpperCase());
      for (int i = 1; i < ss.size(); i++)
      {
        sb.append('_');
        sb.append(((String)ss.get(i)).toUpperCase());
      }
    }
    return sb.toString();
  }
  
  public static void escape(StringBuilder sb, String s, int start)
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
        sb.append('_');
        if (c <= '\017') {
          sb.append("000");
        } else if (c <= 'ÿ') {
          sb.append("00");
        } else if (c <= '࿿') {
          sb.append('0');
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
        StringBuilder sb = new StringBuilder(s.substring(0, i));
        escape(sb, s, i);
        return sb.toString();
      }
    }
    return s;
  }
  
  public static boolean isJavaIdentifier(String s)
  {
    if (s.length() == 0) {
      return false;
    }
    if (reservedKeywords.contains(s)) {
      return false;
    }
    if (!Character.isJavaIdentifierStart(s.charAt(0))) {
      return false;
    }
    for (int i = 1; i < s.length(); i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }
  
  public static boolean isJavaPackageName(String s)
  {
    while (s.length() != 0)
    {
      int idx = s.indexOf('.');
      if (idx == -1) {
        idx = s.length();
      }
      if (!isJavaIdentifier(s.substring(0, idx))) {
        return false;
      }
      s = s.substring(idx);
      if (s.length() != 0) {
        s = s.substring(1);
      }
    }
    return true;
  }
  
  static
  {
    for (int t0 = 0; t0 < 5; t0++) {
      for (int t1 = 0; t1 < 5; t1++) {
        actionTable[(t0 * 5 + t1)] = decideAction(t0, t1);
      }
    }
    reservedKeywords = new HashSet();
    
    String[] words = { "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null", "assert", "enum" };
    for (String word : words) {
      reservedKeywords.add(word);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\api\impl\NameUtil.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */