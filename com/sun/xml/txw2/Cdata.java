package com.sun.xml.txw2;

final class Cdata
  extends Text
{
  Cdata(Document document, NamespaceResolver nsResolver, Object obj)
  {
    super(document, nsResolver, obj);
  }
  
  void accept(ContentVisitor visitor)
  {
    visitor.onCdata(this.buffer);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\Cdata.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */