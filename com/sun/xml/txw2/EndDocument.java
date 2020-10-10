package com.sun.xml.txw2;

final class EndDocument
  extends Content
{
  boolean concludesPendingStartTag()
  {
    return true;
  }
  
  void accept(ContentVisitor visitor)
  {
    visitor.onEndDocument();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\EndDocument.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */