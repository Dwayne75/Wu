package org.apache.xml.resolver.readers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogException;

public abstract interface CatalogReader
{
  public abstract void readCatalog(Catalog paramCatalog, String paramString)
    throws MalformedURLException, IOException, CatalogException;
  
  public abstract void readCatalog(Catalog paramCatalog, InputStream paramInputStream)
    throws IOException, CatalogException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\readers\CatalogReader.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */