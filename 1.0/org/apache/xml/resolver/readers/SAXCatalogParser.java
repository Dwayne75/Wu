package org.apache.xml.resolver.readers;

import org.apache.xml.resolver.Catalog;
import org.xml.sax.ContentHandler;
import org.xml.sax.DocumentHandler;

public abstract interface SAXCatalogParser
  extends ContentHandler, DocumentHandler
{
  public abstract void setCatalog(Catalog paramCatalog);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\readers\SAXCatalogParser.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */