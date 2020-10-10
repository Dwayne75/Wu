package org.apache.xml.resolver.readers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Vector;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.helpers.Debug;

public class TR9401CatalogReader
  extends TextCatalogReader
{
  public void readCatalog(Catalog paramCatalog, InputStream paramInputStream)
    throws MalformedURLException, IOException
  {
    this.catfile = paramInputStream;
    if (this.catfile == null) {
      return;
    }
    Vector localVector1 = null;
    for (;;)
    {
      String str1 = nextToken();
      if (str1 == null)
      {
        if (localVector1 != null)
        {
          paramCatalog.unknownEntry(localVector1);
          localVector1 = null;
        }
        this.catfile.close();
        this.catfile = null;
        return;
      }
      String str2 = null;
      if (this.caseSensitive) {
        str2 = str1;
      } else {
        str2 = str1.toUpperCase();
      }
      if (str2.equals("DELEGATE")) {
        str2 = "DELEGATE_PUBLIC";
      }
      try
      {
        int i = CatalogEntry.getEntryType(str2);
        int j = CatalogEntry.getEntryArgCount(i);
        Vector localVector2 = new Vector();
        if (localVector1 != null)
        {
          paramCatalog.unknownEntry(localVector1);
          localVector1 = null;
        }
        for (int k = 0; k < j; k++) {
          localVector2.addElement(nextToken());
        }
        paramCatalog.addEntry(new CatalogEntry(str2, localVector2));
      }
      catch (CatalogException localCatalogException)
      {
        if (localCatalogException.getExceptionType() == 3)
        {
          if (localVector1 == null) {
            localVector1 = new Vector();
          }
          localVector1.addElement(str1);
        }
      }
      if (localCatalogException.getExceptionType() == 2)
      {
        Debug.message(1, "Invalid catalog entry", str1);
        localVector1 = null;
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\readers\TR9401CatalogReader.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */