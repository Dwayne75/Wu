package com.sun.xml.txw2;

abstract class Content
{
  private Content next;
  
  final Content getNext()
  {
    return this.next;
  }
  
  final void setNext(Document doc, Content next)
  {
    assert (next != null);
    assert (this.next == null) : ("next of " + this + " is already set to " + this.next);
    this.next = next;
    doc.run();
  }
  
  boolean isReadyToCommit()
  {
    return true;
  }
  
  abstract boolean concludesPendingStartTag();
  
  abstract void accept(ContentVisitor paramContentVisitor);
  
  public void written() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\Content.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */