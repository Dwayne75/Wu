package com.sun.tools.xjc.reader;

import com.sun.codemodel.JJavaName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.xml.sax.InputSource;

public class Util
{
  public static InputSource getInputSource(String fileOrURL)
  {
    try
    {
      return new InputSource(escapeSpace(new URL(fileOrURL).toExternalForm()));
    }
    catch (MalformedURLException e)
    {
      String url = new File(fileOrURL).getCanonicalFile().toURL().toExternalForm();
      return new InputSource(escapeSpace(url));
    }
    catch (Exception e) {}
    return new InputSource(fileOrURL);
  }
  
  private static String escapeSpace(String url)
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < url.length(); i++) {
      if (url.charAt(i) == ' ') {
        buf.append("%20");
      } else {
        buf.append(url.charAt(i));
      }
    }
    return buf.toString();
  }
  
  public static String getPackageNameFromNamespaceURI(String nsUri, NameConverter nameConv)
  {
    int idx = nsUri.indexOf(':');
    if (idx >= 0)
    {
      String scheme = nsUri.substring(0, idx);
      if ((scheme.equalsIgnoreCase("http")) || (scheme.equalsIgnoreCase("urn"))) {
        nsUri = nsUri.substring(idx + 1);
      }
    }
    ArrayList tokens = tokenize(nsUri, "/: ");
    if (tokens.size() == 0) {
      return null;
    }
    if (tokens.size() > 1)
    {
      String lastToken = (String)tokens.get(tokens.size() - 1);
      idx = lastToken.lastIndexOf('.');
      if (idx > 0)
      {
        lastToken = lastToken.substring(0, idx);
        tokens.set(tokens.size() - 1, lastToken);
      }
    }
    String domain = (String)tokens.get(0);
    idx = domain.indexOf(':');
    if (idx >= 0) {
      domain = domain.substring(0, idx);
    }
    ArrayList r = reverse(tokenize(domain, "."));
    if (((String)r.get(r.size() - 1)).equalsIgnoreCase("www")) {
      r.remove(r.size() - 1);
    }
    tokens.addAll(1, r);
    tokens.remove(0);
    for (int i = 0; i < tokens.size(); i++)
    {
      String token = (String)tokens.get(i);
      token = removeIllegalIdentifierChars(token);
      if (!JJavaName.isJavaIdentifier(token)) {
        token = new String("_" + token);
      }
      tokens.set(i, token.toLowerCase());
    }
    return combine(tokens, '.');
  }
  
  private static String removeIllegalIdentifierChars(String token)
  {
    StringBuffer newToken = new StringBuffer();
    for (int i = 0; i < token.length(); i++)
    {
      char c = token.charAt(i);
      if ((i == 0) && (!Character.isJavaIdentifierStart(c))) {
        newToken.append("_" + c);
      } else if (!Character.isJavaIdentifierPart(c)) {
        newToken.append('_');
      } else {
        newToken.append(c);
      }
    }
    return newToken.toString();
  }
  
  private static ArrayList tokenize(String str, String sep)
  {
    StringTokenizer tokens = new StringTokenizer(str, sep);
    ArrayList r = new ArrayList();
    while (tokens.hasMoreTokens()) {
      r.add(tokens.nextToken());
    }
    return r;
  }
  
  private static ArrayList reverse(List a)
  {
    ArrayList r = new ArrayList();
    for (int i = a.size() - 1; i >= 0; i--) {
      r.add(a.get(i));
    }
    return r;
  }
  
  private static String combine(List r, char sep)
  {
    StringBuffer buf = new StringBuffer((String)r.get(0));
    for (int i = 1; i < r.size(); i++)
    {
      buf.append(sep);
      buf.append(r.get(i));
    }
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\Util.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */