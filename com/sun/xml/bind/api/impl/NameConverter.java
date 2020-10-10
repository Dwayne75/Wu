package com.sun.xml.bind.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract interface NameConverter
{
  public static final NameConverter standard = new Standard();
  
  public abstract String toClassName(String paramString);
  
  public abstract String toInterfaceName(String paramString);
  
  public abstract String toPropertyName(String paramString);
  
  public abstract String toConstantName(String paramString);
  
  public abstract String toVariableName(String paramString);
  
  public abstract String toPackageName(String paramString);
  
  public static class Standard
    extends NameUtil
    implements NameConverter
  {
    public String toClassName(String s)
    {
      return toMixedCaseName(toWordList(s), true);
    }
    
    public String toVariableName(String s)
    {
      return toMixedCaseName(toWordList(s), false);
    }
    
    public String toInterfaceName(String token)
    {
      return toClassName(token);
    }
    
    public String toPropertyName(String s)
    {
      String prop = toClassName(s);
      if (prop.equals("Class")) {
        prop = "Clazz";
      }
      return prop;
    }
    
    public String toConstantName(String token)
    {
      return super.toConstantName(token);
    }
    
    public String toPackageName(String nsUri)
    {
      int idx = nsUri.indexOf(':');
      String scheme = "";
      if (idx >= 0)
      {
        scheme = nsUri.substring(0, idx);
        if ((scheme.equalsIgnoreCase("http")) || (scheme.equalsIgnoreCase("urn"))) {
          nsUri = nsUri.substring(idx + 1);
        }
      }
      ArrayList<String> tokens = tokenize(nsUri, "/: ");
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
      ArrayList<String> r = reverse(tokenize(domain, scheme.equals("urn") ? ".-" : "."));
      if (((String)r.get(r.size() - 1)).equalsIgnoreCase("www")) {
        r.remove(r.size() - 1);
      }
      tokens.addAll(1, r);
      tokens.remove(0);
      for (int i = 0; i < tokens.size(); i++)
      {
        String token = (String)tokens.get(i);
        token = removeIllegalIdentifierChars(token);
        if (!NameUtil.isJavaIdentifier(token)) {
          token = '_' + token;
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
          newToken.append('_').append(c);
        } else if (!Character.isJavaIdentifierPart(c)) {
          newToken.append('_');
        } else {
          newToken.append(c);
        }
      }
      return newToken.toString();
    }
    
    private static ArrayList<String> tokenize(String str, String sep)
    {
      StringTokenizer tokens = new StringTokenizer(str, sep);
      ArrayList<String> r = new ArrayList();
      while (tokens.hasMoreTokens()) {
        r.add(tokens.nextToken());
      }
      return r;
    }
    
    private static <T> ArrayList<T> reverse(List<T> a)
    {
      ArrayList<T> r = new ArrayList();
      for (int i = a.size() - 1; i >= 0; i--) {
        r.add(a.get(i));
      }
      return r;
    }
    
    private static String combine(List r, char sep)
    {
      StringBuilder buf = new StringBuilder(r.get(0).toString());
      for (int i = 1; i < r.size(); i++)
      {
        buf.append(sep);
        buf.append(r.get(i));
      }
      return buf.toString();
    }
  }
  
  public static final NameConverter jaxrpcCompatible = new Standard()
  {
    protected boolean isPunct(char c)
    {
      return (c == '.') || (c == '-') || (c == ';') || (c == '·') || (c == '·') || (c == '۝') || (c == '۞');
    }
    
    protected boolean isLetter(char c)
    {
      return (super.isLetter(c)) || (c == '_');
    }
    
    protected int classify(char c0)
    {
      if (c0 == '_') {
        return 2;
      }
      return super.classify(c0);
    }
  };
  public static final NameConverter smart = new Standard()
  {
    public String toConstantName(String token)
    {
      String name = super.toConstantName(token);
      if (NameUtil.isJavaIdentifier(name)) {
        return name;
      }
      return '_' + name;
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\api\impl\NameConverter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */