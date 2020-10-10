package com.sun.org.apache.xml.internal.resolver.helpers;

public abstract class PublicId
{
  public static String normalize(String publicId)
  {
    String normal = publicId.replace('\t', ' ');
    normal = normal.replace('\r', ' ');
    normal = normal.replace('\n', ' ');
    normal = normal.trim();
    int pos;
    while ((pos = normal.indexOf("  ")) >= 0) {
      normal = normal.substring(0, pos) + normal.substring(pos + 1);
    }
    return normal;
  }
  
  public static String encodeURN(String publicId)
  {
    String urn = normalize(publicId);
    
    urn = stringReplace(urn, "%", "%25");
    urn = stringReplace(urn, ";", "%3B");
    urn = stringReplace(urn, "'", "%27");
    urn = stringReplace(urn, "?", "%3F");
    urn = stringReplace(urn, "#", "%23");
    urn = stringReplace(urn, "+", "%2B");
    urn = stringReplace(urn, " ", "+");
    urn = stringReplace(urn, "::", ";");
    urn = stringReplace(urn, ":", "%3A");
    urn = stringReplace(urn, "//", ":");
    urn = stringReplace(urn, "/", "%2F");
    
    return "urn:publicid:" + urn;
  }
  
  public static String decodeURN(String urn)
  {
    String publicId = "";
    if (urn.startsWith("urn:publicid:")) {
      publicId = urn.substring(13);
    } else {
      return urn;
    }
    publicId = stringReplace(publicId, "%2F", "/");
    publicId = stringReplace(publicId, ":", "//");
    publicId = stringReplace(publicId, "%3A", ":");
    publicId = stringReplace(publicId, ";", "::");
    publicId = stringReplace(publicId, "+", " ");
    publicId = stringReplace(publicId, "%2B", "+");
    publicId = stringReplace(publicId, "%23", "#");
    publicId = stringReplace(publicId, "%3F", "?");
    publicId = stringReplace(publicId, "%27", "'");
    publicId = stringReplace(publicId, "%3B", ";");
    publicId = stringReplace(publicId, "%25", "%");
    
    return publicId;
  }
  
  private static String stringReplace(String str, String oldStr, String newStr)
  {
    String result = "";
    int pos = str.indexOf(oldStr);
    while (pos >= 0)
    {
      result = result + str.substring(0, pos);
      result = result + newStr;
      str = str.substring(pos + 1);
      
      pos = str.indexOf(oldStr);
    }
    return result + str;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\helpers\PublicId.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */