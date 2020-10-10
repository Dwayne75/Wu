package com.sun.org.apache.xml.internal.resolver.readers;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogEntry;
import com.sun.org.apache.xml.internal.resolver.CatalogException;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Stack;
import java.util.Vector;

public class TextCatalogReader
  implements CatalogReader
{
  protected InputStream catfile = null;
  protected int[] stack = new int[3];
  protected Stack tokenStack = new Stack();
  protected int top = -1;
  protected boolean caseSensitive = false;
  
  public void setCaseSensitive(boolean isCaseSensitive)
  {
    this.caseSensitive = isCaseSensitive;
  }
  
  public boolean getCaseSensitive()
  {
    return this.caseSensitive;
  }
  
  public void readCatalog(Catalog catalog, String fileUrl)
    throws MalformedURLException, IOException
  {
    URL catURL = null;
    try
    {
      catURL = new URL(fileUrl);
    }
    catch (MalformedURLException e)
    {
      catURL = new URL("file:///" + fileUrl);
    }
    URLConnection urlCon = catURL.openConnection();
    try
    {
      readCatalog(catalog, urlCon.getInputStream());
    }
    catch (FileNotFoundException e)
    {
      catalog.getCatalogManager().debug.message(1, "Failed to load catalog, file not found", catURL.toString());
    }
  }
  
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
  
  protected void finalize()
  {
    if (this.catfile != null) {
      try
      {
        this.catfile.close();
      }
      catch (IOException e) {}
    }
    this.catfile = null;
  }
  
  protected String nextToken()
    throws IOException, CatalogException
  {
    String token = "";
    if (!this.tokenStack.empty()) {
      return (String)this.tokenStack.pop();
    }
    int nextch;
    do
    {
      ch = this.catfile.read();
      while (ch <= 32)
      {
        ch = this.catfile.read();
        if (ch < 0) {
          return null;
        }
      }
      nextch = this.catfile.read();
      if (nextch < 0) {
        return null;
      }
      if ((ch != 45) || (nextch != 45)) {
        break;
      }
      ch = 32;
      nextch = nextChar();
      while (((ch != 45) || (nextch != 45)) && (nextch > 0))
      {
        ch = nextch;
        nextch = nextChar();
      }
    } while (nextch >= 0);
    throw new CatalogException(8, "Unterminated comment in catalog file; EOF treated as end-of-comment.");
    
    this.stack[(++this.top)] = nextch;
    this.stack[(++this.top)] = ch;
    
    int ch = nextChar();
    if ((ch == 34) || (ch == 39))
    {
      int quote = ch;
      while ((ch = nextChar()) != quote)
      {
        char[] chararr = new char[1];
        chararr[0] = ((char)ch);
        String s = new String(chararr);
        token = token.concat(s);
      }
      return token;
    }
    while (ch > 32)
    {
      nextch = nextChar();
      if ((ch == 45) && (nextch == 45))
      {
        this.stack[(++this.top)] = ch;
        this.stack[(++this.top)] = nextch;
        return token;
      }
      char[] chararr = new char[1];
      chararr[0] = ((char)ch);
      String s = new String(chararr);
      token = token.concat(s);
      ch = nextch;
    }
    return token;
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\readers\TextCatalogReader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */