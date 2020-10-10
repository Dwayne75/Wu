package com.sun.dtdparser;

public class XmlNames
{
  public static boolean isName(String value)
  {
    if (value == null) {
      return false;
    }
    char c = value.charAt(0);
    if ((!XmlChars.isLetter(c)) && (c != '_') && (c != ':')) {
      return false;
    }
    for (int i = 1; i < value.length(); i++) {
      if (!XmlChars.isNameChar(value.charAt(i))) {
        return false;
      }
    }
    return true;
  }
  
  public static boolean isUnqualifiedName(String value)
  {
    if ((value == null) || (value.length() == 0)) {
      return false;
    }
    char c = value.charAt(0);
    if ((!XmlChars.isLetter(c)) && (c != '_')) {
      return false;
    }
    for (int i = 1; i < value.length(); i++) {
      if (!XmlChars.isNCNameChar(value.charAt(i))) {
        return false;
      }
    }
    return true;
  }
  
  public static boolean isQualifiedName(String value)
  {
    if (value == null) {
      return false;
    }
    int first = value.indexOf(':');
    if (first <= 0) {
      return isUnqualifiedName(value);
    }
    int last = value.lastIndexOf(':');
    if (last != first) {
      return false;
    }
    return (isUnqualifiedName(value.substring(0, first))) && (isUnqualifiedName(value.substring(first + 1)));
  }
  
  public static boolean isNmtoken(String token)
  {
    int length = token.length();
    for (int i = 0; i < length; i++) {
      if (!XmlChars.isNameChar(token.charAt(i))) {
        return false;
      }
    }
    return true;
  }
  
  public static boolean isNCNmtoken(String token)
  {
    return (isNmtoken(token)) && (token.indexOf(':') < 0);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\dtdparser\XmlNames.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */