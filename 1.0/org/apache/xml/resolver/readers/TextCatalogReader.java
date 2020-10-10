package org.apache.xml.resolver.readers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Stack;
import java.util.Vector;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.helpers.Debug;

public class TextCatalogReader
  implements CatalogReader
{
  protected InputStream catfile = null;
  protected int[] stack = new int[3];
  protected Stack tokenStack = new Stack();
  protected int top = -1;
  protected boolean caseSensitive = false;
  
  public void setCaseSensitive(boolean paramBoolean)
  {
    this.caseSensitive = paramBoolean;
  }
  
  public boolean getCaseSensitive()
  {
    return this.caseSensitive;
  }
  
  public void readCatalog(Catalog paramCatalog, String paramString)
    throws MalformedURLException, IOException
  {
    URL localURL = null;
    try
    {
      localURL = new URL(paramString);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      localURL = new URL("file:///" + paramString);
    }
    URLConnection localURLConnection = localURL.openConnection();
    try
    {
      readCatalog(paramCatalog, localURLConnection.getInputStream());
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      Debug.message(1, "Failed to load catalog, file not found", localURL.toString());
    }
  }
  
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
  
  protected void finalize()
  {
    if (this.catfile != null) {
      try
      {
        this.catfile.close();
      }
      catch (IOException localIOException) {}
    }
    this.catfile = null;
  }
  
  protected String nextToken()
    throws IOException
  {
    String str1 = "";
    if (!this.tokenStack.empty()) {
      return (String)this.tokenStack.pop();
    }
    int j;
    for (;;)
    {
      i = this.catfile.read();
      while (i <= 32)
      {
        i = this.catfile.read();
        if (i < 0) {
          return null;
        }
      }
      j = this.catfile.read();
      if (j < 0) {
        return null;
      }
      if ((i != 45) || (j != 45)) {
        break;
      }
      i = 32;
      for (j = nextChar(); (i != 45) || (j != 45); j = nextChar()) {
        i = j;
      }
    }
    this.stack[(++this.top)] = j;
    this.stack[(++this.top)] = i;
    int i = nextChar();
    if ((i == 34) || (i == 39))
    {
      k = i;
      while ((i = nextChar()) != k)
      {
        localObject = new char[1];
        localObject[0] = ((char)i);
        str2 = new String((char[])localObject);
        str1 = str1.concat(str2);
      }
      return str1;
    }
    while (i > 32)
    {
      int k;
      String str2;
      j = nextChar();
      if ((i == 45) && (j == 45))
      {
        this.stack[(++this.top)] = i;
        this.stack[(++this.top)] = j;
        return str1;
      }
      char[] arrayOfChar = new char[1];
      arrayOfChar[0] = ((char)i);
      Object localObject = new String(arrayOfChar);
      str1 = str1.concat((String)localObject);
      i = j;
    }
    return str1;
  }
  
  protected int nextChar()
    throws IOException
  {
    if (this.top < 0) {
      return this.catfile.read();
    }
    return this.stack[(this.top--)];
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\readers\TextCatalogReader.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */