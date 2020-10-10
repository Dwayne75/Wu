package com.sun.xml.txw2;

abstract interface ContentVisitor
{
  public abstract void onStartDocument();
  
  public abstract void onEndDocument();
  
  public abstract void onEndTag();
  
  public abstract void onPcdata(StringBuilder paramStringBuilder);
  
  public abstract void onCdata(StringBuilder paramStringBuilder);
  
  public abstract void onStartTag(String paramString1, String paramString2, Attribute paramAttribute, NamespaceDecl paramNamespaceDecl);
  
  public abstract void onComment(StringBuilder paramStringBuilder);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\ContentVisitor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */