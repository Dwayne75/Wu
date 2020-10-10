package org.flywaydb.core.internal.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils
{
  public static String trimOrPad(String str, int length)
  {
    return trimOrPad(str, length, ' ');
  }
  
  public static String trimOrPad(String str, int length, char padChar)
  {
    String result;
    String result;
    if (str == null) {
      result = "";
    } else {
      result = str;
    }
    if (result.length() > length) {
      return result.substring(0, length);
    }
    while (result.length() < length) {
      result = result + padChar;
    }
    return result;
  }
  
  public static boolean isNumeric(String str)
  {
    return (str != null) && (str.matches("\\d*"));
  }
  
  public static String collapseWhitespace(String str)
  {
    return str.replaceAll("\\s+", " ");
  }
  
  public static String left(String str, int count)
  {
    if (str == null) {
      return null;
    }
    if (str.length() < count) {
      return str;
    }
    return str.substring(0, count);
  }
  
  public static String replaceAll(String str, String originalToken, String replacementToken)
  {
    return str.replaceAll(Pattern.quote(originalToken), Matcher.quoteReplacement(replacementToken));
  }
  
  public static boolean hasLength(String str)
  {
    return (str != null) && (str.length() > 0);
  }
  
  public static String arrayToCommaDelimitedString(Object[] strings)
  {
    if (strings == null) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < strings.length; i++)
    {
      if (i > 0) {
        builder.append(",");
      }
      builder.append(String.valueOf(strings[i]));
    }
    return builder.toString();
  }
  
  public static boolean hasText(String s)
  {
    return (s != null) && (s.trim().length() > 0);
  }
  
  public static String[] tokenizeToStringArray(String str, String delimiters)
  {
    if (str == null) {
      return null;
    }
    String[] tokens = str.split("[" + delimiters + "]");
    for (int i = 0; i < tokens.length; i++) {
      tokens[i] = tokens[i].trim();
    }
    return tokens;
  }
  
  public static int countOccurrencesOf(String str, String token)
  {
    if ((str == null) || (token == null) || (str.length() == 0) || (token.length() == 0)) {
      return 0;
    }
    int count = 0;
    int pos = 0;
    int idx;
    while ((idx = str.indexOf(token, pos)) != -1)
    {
      count++;
      pos = idx + token.length();
    }
    return count;
  }
  
  public static String replace(String inString, String oldPattern, String newPattern)
  {
    if ((!hasLength(inString)) || (!hasLength(oldPattern)) || (newPattern == null)) {
      return inString;
    }
    StringBuilder sb = new StringBuilder();
    int pos = 0;
    int index = inString.indexOf(oldPattern);
    
    int patLen = oldPattern.length();
    while (index >= 0)
    {
      sb.append(inString.substring(pos, index));
      sb.append(newPattern);
      pos = index + patLen;
      index = inString.indexOf(oldPattern, pos);
    }
    sb.append(inString.substring(pos));
    
    return sb.toString();
  }
  
  public static String collectionToCommaDelimitedString(Collection<?> collection)
  {
    return collectionToDelimitedString(collection, ", ");
  }
  
  public static String collectionToDelimitedString(Collection<?> collection, String delimiter)
  {
    if (collection == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    Iterator it = collection.iterator();
    while (it.hasNext())
    {
      sb.append(it.next());
      if (it.hasNext()) {
        sb.append(delimiter);
      }
    }
    return sb.toString();
  }
  
  public static String trimLeadingWhitespace(String str)
  {
    if (!hasLength(str)) {
      return str;
    }
    StringBuilder buf = new StringBuilder(str);
    while ((buf.length() > 0) && (Character.isWhitespace(buf.charAt(0)))) {
      buf.deleteCharAt(0);
    }
    return buf.toString();
  }
  
  public static String trimTrailingWhitespace(String str)
  {
    if (!hasLength(str)) {
      return str;
    }
    StringBuilder buf = new StringBuilder(str);
    while ((buf.length() > 0) && (Character.isWhitespace(buf.charAt(buf.length() - 1)))) {
      buf.deleteCharAt(buf.length() - 1);
    }
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\StringUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */