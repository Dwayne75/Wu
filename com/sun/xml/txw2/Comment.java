package com.sun.xml.txw2;

final class Comment
  extends Content
{
  private final StringBuilder buffer = new StringBuilder();
  
  public Comment(Document document, NamespaceResolver nsResolver, Object obj)
  {
    document.writeValue(obj, nsResolver, this.buffer);
  }
  
  boolean concludesPendingStartTag()
  {
    return false;
  }
  
  void accept(ContentVisitor visitor)
  {
    visitor.onComment(this.buffer);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\Comment.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */