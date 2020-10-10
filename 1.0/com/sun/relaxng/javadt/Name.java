package com.sun.relaxng.javadt;

import java.util.HashSet;

public class Name
{
  public static boolean isJavaIdentifier(String token)
  {
    if (token.length() == 0) {
      return false;
    }
    if (reservedKeywords.contains(token)) {
      return false;
    }
    if (!Character.isJavaIdentifierStart(token.charAt(0))) {
      return false;
    }
    for (int i = 1; i < token.length(); i++) {
      if (!Character.isJavaIdentifierPart(token.charAt(i))) {
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
  
  private static HashSet reservedKeywords = new HashSet();
  
  static
  {
    String[] words = {
      "abstract", 
      "boolean", 
      "break", 
      "byte", 
      "case", 
      "catch", 
      "char", 
      "class", 
      "const", 
      "continue", 
      "default", 
      "do", 
      "double", 
      "else", 
      "extends", 
      "final", 
      "finally", 
      "float", 
      "for", 
      "goto", 
      "if", 
      "implements", 
      "import", 
      "instanceof", 
      "int", 
      "interface", 
      "long", 
      "native", 
      "new", 
      "package", 
      "private", 
      "protected", 
      "public", 
      "return", 
      "short", 
      "static", 
      "strictfp", 
      "super", 
      "switch", 
      "synchronized", 
      "this", 
      "throw", 
      "throws", 
      "transient", 
      "try", 
      "void", 
      "volatile", 
      "while", 
      
      "true", 
      "false", 
      "null", 
      
      "assert" };
    for (int i = 0; i < words.length; i++) {
      reservedKeywords.add(words[i]);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\relaxng\javadt\Name.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */