package com.sun.dtdparser;

public class XmlChars
{
  public static boolean isChar(int ucs4char)
  {
    return ((ucs4char >= 32) && (ucs4char <= 55295)) || (ucs4char == 10) || (ucs4char == 9) || (ucs4char == 13) || ((ucs4char >= 57344) && (ucs4char <= 65533)) || ((ucs4char >= 65536) && (ucs4char <= 1114111));
  }
  
  public static boolean isNameChar(char c)
  {
    if (isLetter2(c)) {
      return true;
    }
    if (c == '>') {
      return false;
    }
    if ((c == '.') || (c == '-') || (c == '_') || (c == ':') || (isExtender(c))) {
      return true;
    }
    return false;
  }
  
  public static boolean isNCNameChar(char c)
  {
    return (c != ':') && (isNameChar(c));
  }
  
  public static boolean isSpace(char c)
  {
    return (c == ' ') || (c == '\t') || (c == '\n') || (c == '\r');
  }
  
  public static boolean isLetter(char c)
  {
    if ((c >= 'a') && (c <= 'z')) {
      return true;
    }
    if (c == '/') {
      return false;
    }
    if ((c >= 'A') && (c <= 'Z')) {
      return true;
    }
    switch (Character.getType(c))
    {
    case 1: 
    case 2: 
    case 3: 
    case 5: 
    case 10: 
      return (!isCompatibilityChar(c)) && ((c < '⃝') || (c > '⃠'));
    }
    return ((c >= 'ʻ') && (c <= 'ˁ')) || (c == 'ՙ') || (c == 'ۥ') || (c == 'ۦ');
  }
  
  private static boolean isCompatibilityChar(char c)
  {
    switch (c >> '\b' & 0xFF)
    {
    case 0: 
      return (c == 'ª') || (c == 'µ') || (c == 'º');
    case 1: 
      return ((c >= 'Ĳ') && (c <= 'ĳ')) || ((c >= 'Ŀ') && (c <= 'ŀ')) || (c == 'ŉ') || (c == 'ſ') || ((c >= 'Ǆ') && (c <= 'ǌ')) || ((c >= 'Ǳ') && (c <= 'ǳ'));
    case 2: 
      return ((c >= 'ʰ') && (c <= 'ʸ')) || ((c >= 'ˠ') && (c <= 'ˤ'));
    case 3: 
      return c == 'ͺ';
    case 5: 
      return c == 'և';
    case 14: 
      return (c >= 'ໜ') && (c <= 'ໝ');
    case 17: 
      return (c == 'ᄁ') || (c == 'ᄄ') || (c == 'ᄈ') || (c == 'ᄊ') || (c == 'ᄍ') || ((c >= 'ᄓ') && (c <= 'ᄻ')) || (c == 'ᄽ') || (c == 'ᄿ') || ((c >= 'ᅁ') && (c <= 'ᅋ')) || (c == 'ᅍ') || (c == 'ᅏ') || ((c >= 'ᅑ') && (c <= 'ᅓ')) || ((c >= 'ᅖ') && (c <= 'ᅘ')) || (c == 'ᅢ') || (c == 'ᅤ') || (c == 'ᅦ') || (c == 'ᅨ') || ((c >= 'ᅪ') && (c <= 'ᅬ')) || ((c >= 'ᅯ') && (c <= 'ᅱ')) || (c == 'ᅴ') || ((c >= 'ᅶ') && (c <= 'ᆝ')) || ((c >= 'ᆟ') && (c <= 'ᆢ')) || ((c >= 'ᆩ') && (c <= 'ᆪ')) || ((c >= 'ᆬ') && (c <= 'ᆭ')) || ((c >= 'ᆰ') && (c <= 'ᆶ')) || (c == 'ᆹ') || (c == 'ᆻ') || ((c >= 'ᇃ') && (c <= 'ᇪ')) || ((c >= 'ᇬ') && (c <= 'ᇯ')) || ((c >= 'ᇱ') && (c <= 'ᇸ'));
    case 32: 
      return c == 'ⁿ';
    case 33: 
      return (c == 'ℂ') || (c == 'ℇ') || ((c >= 'ℊ') && (c <= 'ℓ')) || (c == 'ℕ') || ((c >= '℘') && (c <= 'ℝ')) || (c == 'ℤ') || (c == 'ℨ') || ((c >= 'ℬ') && (c <= 'ℭ')) || ((c >= 'ℯ') && (c <= 'ℸ')) || ((c >= 'Ⅰ') && (c <= 'ⅿ'));
    case 48: 
      return (c >= '゛') && (c <= '゜');
    case 49: 
      return (c >= 'ㄱ') && (c <= 'ㆎ');
    case 249: 
    case 250: 
    case 251: 
    case 252: 
    case 253: 
    case 254: 
    case 255: 
      return true;
    }
    return false;
  }
  
  private static boolean isLetter2(char c)
  {
    if ((c >= 'a') && (c <= 'z')) {
      return true;
    }
    if (c == '>') {
      return false;
    }
    if ((c >= 'A') && (c <= 'Z')) {
      return true;
    }
    switch (Character.getType(c))
    {
    case 1: 
    case 2: 
    case 3: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 10: 
      return (!isCompatibilityChar(c)) && ((c < '⃝') || (c > '⃠'));
    }
    return c == '·';
  }
  
  private static boolean isDigit(char c)
  {
    return (Character.isDigit(c)) && ((c < 65296) || (c > 65305));
  }
  
  private static boolean isExtender(char c)
  {
    return (c == '·') || (c == 'ː') || (c == 'ˑ') || (c == '·') || (c == 'ـ') || (c == 'ๆ') || (c == 'ໆ') || (c == '々') || ((c >= '〱') && (c <= '〵')) || ((c >= 'ゝ') && (c <= 'ゞ')) || ((c >= 'ー') && (c <= 'ヾ'));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\dtdparser\XmlChars.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */