package com.sun.xml.txw2;

final class NamespaceDecl
{
  final String uri;
  boolean requirePrefix;
  final String dummyPrefix;
  final char uniqueId;
  String prefix;
  boolean declared;
  NamespaceDecl next;
  
  NamespaceDecl(char uniqueId, String uri, String prefix, boolean requirePrefix)
  {
    this.dummyPrefix = (2 + '\000' + uniqueId);
    this.uri = uri;
    this.prefix = prefix;
    this.requirePrefix = requirePrefix;
    this.uniqueId = uniqueId;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\NamespaceDecl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */