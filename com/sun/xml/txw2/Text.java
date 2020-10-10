package com.sun.xml.txw2;

abstract class Text
  extends Content
{
  protected final StringBuilder buffer = new StringBuilder();
  
  protected Text(Document document, NamespaceResolver nsResolver, Object obj)
  {
    document.writeValue(obj, nsResolver, this.buffer);
  }
  
  boolean concludesPendingStartTag()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\Text.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */