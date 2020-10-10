package com.sun.xml.bind.marshaller;

public abstract class NamespacePrefixMapper
{
  private static final String[] EMPTY_STRING = new String[0];
  
  public abstract String getPreferredPrefix(String paramString1, String paramString2, boolean paramBoolean);
  
  public String[] getPreDeclaredNamespaceUris()
  {
    return EMPTY_STRING;
  }
  
  public String[] getPreDeclaredNamespaceUris2()
  {
    return EMPTY_STRING;
  }
  
  public String[] getContextualNamespaceDecls()
  {
    return EMPTY_STRING;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\marshaller\NamespacePrefixMapper.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */