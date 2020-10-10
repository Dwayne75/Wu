package com.sun.org.apache.xml.internal.resolver.readers;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogEntry;
import com.sun.org.apache.xml.internal.resolver.CatalogException;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Vector;

public class TR9401CatalogReader
  extends TextCatalogReader
{
  public void readCatalog(Catalog catalog, InputStream is)
    throws MalformedURLException, IOException
  {
    this.catfile = is;
    if (this.catfile == null) {
      return;
    }
    Vector unknownEntry = null;
    try
    {
      for (;;)
      {
        String token = nextToken();
        if (token == null)
        {
          if (unknownEntry != null)
          {
            catalog.unknownEntry(unknownEntry);
            unknownEntry = null;
          }
          this.catfile.close();
          this.catfile = null;
          return;
        }
        String entryToken = null;
        if (this.caseSensitive) {
          entryToken = token;
        } else {
          entryToken = token.toUpperCase();
        }
        if (entryToken.equals("DELEGATE")) {
          entryToken = "DELEGATE_PUBLIC";
        }
        try
        {
          int type = CatalogEntry.getEntryType(entryToken);
          int numArgs = CatalogEntry.getEntryArgCount(type);
          Vector args = new Vector();
          if (unknownEntry != null)
          {
            catalog.unknownEntry(unknownEntry);
            unknownEntry = null;
          }
          for (int count = 0; count < numArgs; count++) {
            args.addElement(nextToken());
          }
          catalog.addEntry(new CatalogEntry(entryToken, args));
        }
        catch (CatalogException cex)
        {
          if (cex.getExceptionType() == 3)
          {
            if (unknownEntry == null) {
              unknownEntry = new Vector();
            }
            unknownEntry.addElement(token);
          }
          else if (cex.getExceptionType() == 2)
          {
            catalog.getCatalogManager().debug.message(1, "Invalid catalog entry", token);
            unknownEntry = null;
          }
          else if (cex.getExceptionType() == 8)
          {
            catalog.getCatalogManager().debug.message(1, cex.getMessage());
          }
        }
      }
      return;
    }
    catch (CatalogException cex2)
    {
      if (cex2.getExceptionType() == 8) {
        catalog.getCatalogManager().debug.message(1, cex2.getMessage());
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\readers\TR9401CatalogReader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */