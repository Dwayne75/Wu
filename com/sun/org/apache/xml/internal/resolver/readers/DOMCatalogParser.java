package com.sun.org.apache.xml.internal.resolver.readers;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import org.w3c.dom.Node;

public abstract interface DOMCatalogParser
{
  public abstract void parseCatalogEntry(Catalog paramCatalog, Node paramNode);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\readers\DOMCatalogParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */