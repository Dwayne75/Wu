package org.apache.xml.resolver.readers;

import org.apache.xml.resolver.Catalog;
import org.w3c.dom.Node;

public abstract interface DOMCatalogParser
{
  public abstract void parseCatalogEntry(Catalog paramCatalog, Node paramNode);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\readers\DOMCatalogParser.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */