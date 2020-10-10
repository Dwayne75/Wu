package com.sun.xml.bind.v2.schemagen;

public final class Util
{
  public static String escapeURI(String s)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      if (Character.isSpaceChar(c)) {
        sb.append("%20");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
  
  public static String getParentUriPath(String uriPath)
  {
    int idx = uriPath.lastIndexOf('/');
    if (uriPath.endsWith("/"))
    {
      uriPath = uriPath.substring(0, idx);
      idx = uriPath.lastIndexOf('/');
    }
    return uriPath.substring(0, idx) + "/";
  }
  
  public static String normalizeUriPath(String uriPath)
  {
    if (uriPath.endsWith("/")) {
      return uriPath;
    }
    int idx = uriPath.lastIndexOf('/');
    return uriPath.substring(0, idx + 1);
  }
  
  public static boolean equalsIgnoreCase(String s, String t)
  {
    if (s == t) {
      return true;
    }
    if ((s != null) && (t != null)) {
      return s.equalsIgnoreCase(t);
    }
    return false;
  }
  
  public static boolean equal(String s, String t)
  {
    if (s == t) {
      return true;
    }
    if ((s != null) && (t != null)) {
      return s.equals(t);
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\Util.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */