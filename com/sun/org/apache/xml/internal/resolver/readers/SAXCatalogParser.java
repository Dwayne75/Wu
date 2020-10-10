package com.sun.org.apache.xml.internal.resolver.readers;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import org.xml.sax.ContentHandler;
import org.xml.sax.DocumentHandler;

public abstract interface SAXCatalogParser
  extends ContentHandler, DocumentHandler
{
  public abstract void setCatalog(Catalog paramCatalog);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\readers\SAXCatalogParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */