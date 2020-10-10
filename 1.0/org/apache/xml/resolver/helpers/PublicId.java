package org.apache.xml.resolver.helpers;

public abstract class PublicId
{
  public static String normalize(String paramString)
  {
    String str = paramString.replace('\t', ' ');
    str = str.replace('\r', ' ');
    str = str.replace('\n', ' ');
    int i;
    for (str = str.trim(); (i = str.indexOf("  ")) >= 0; str = str.substring(0, i) + str.substring(i + 1)) {}
    return str;
  }
  
  public static String encodeURN(String paramString)
  {
    String str = normalize(paramString);
    str = stringReplace(str, "%", "%25");
    str = stringReplace(str, ";", "%3B");
    str = stringReplace(str, "'", "%27");
    str = stringReplace(str, "?", "%3F");
    str = stringReplace(str, "#", "%23");
    str = stringReplace(str, "+", "%2B");
    str = stringReplace(str, " ", "+");
    str = stringReplace(str, "::", ";");
    str = stringReplace(str, ":", "%3A");
    str = stringReplace(str, "//", ":");
    str = stringReplace(str, "/", "%2F");
    return "urn:publicid:" + str;
  }
  
  public static String decodeURN(String paramString)
  {
    String str = "";
    if (paramString.startsWith("urn:publicid:")) {
      str = paramString.substring(13);
    } else {
      return paramString;
    }
    str = stringReplace(str, "%2F", "/");
    str = stringReplace(str, ":", "//");
    str = stringReplace(str, "%3A", ":");
    str = stringReplace(str, ";", "::");
    str = stringReplace(str, "+", " ");
    str = stringReplace(str, "%2B", "+");
    str = stringReplace(str, "%23", "#");
    str = stringReplace(str, "%3F", "?");
    str = stringReplace(str, "%27", "'");
    str = stringReplace(str, "%3B", ";");
    str = stringReplace(str, "%25", "%");
    return str;
  }
  
  private static String stringReplace(String paramString1, String paramString2, String paramString3)
  {
    String str = "";
    for (int i = paramString1.indexOf(paramString2); i >= 0; i = paramString1.indexOf(paramString2))
    {
      str = str + paramString1.substring(0, i);
      str = str + paramString3;
      paramString1 = paramString1.substring(i + 1);
    }
    return str + paramString1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\helpers\PublicId.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */