package org.apache.xml.resolver.readers;

import java.util.Vector;
import javax.xml.parsers.SAXParserFactory;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.helpers.PublicId;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class XCatalogReader
  extends SAXCatalogReader
  implements SAXCatalogParser
{
  protected Catalog catalog = null;
  
  public void setCatalog(Catalog paramCatalog)
  {
    this.catalog = paramCatalog;
  }
  
  public Catalog getCatalog()
  {
    return this.catalog;
  }
  
  public XCatalogReader(SAXParserFactory paramSAXParserFactory)
  {
    super(paramSAXParserFactory);
  }
  
  public void setDocumentLocator(Locator paramLocator) {}
  
  public void startDocument()
    throws SAXException
  {}
  
  public void endDocument()
    throws SAXException
  {}
  
  public void startElement(String paramString1, String paramString2, String paramString3, Attributes paramAttributes)
    throws SAXException
  {
    int i = -1;
    Vector localVector = new Vector();
    if (paramString2.equals("Base"))
    {
      i = Catalog.BASE;
      localVector.add(paramAttributes.getValue("HRef"));
      Debug.message(4, "Base", paramAttributes.getValue("HRef"));
    }
    else if (paramString2.equals("Delegate"))
    {
      i = Catalog.DELEGATE_PUBLIC;
      localVector.add(paramAttributes.getValue("PublicId"));
      localVector.add(paramAttributes.getValue("HRef"));
      Debug.message(4, "Delegate", PublicId.normalize(paramAttributes.getValue("PublicId")), paramAttributes.getValue("HRef"));
    }
    else if (paramString2.equals("Extend"))
    {
      i = Catalog.CATALOG;
      localVector.add(paramAttributes.getValue("HRef"));
      Debug.message(4, "Extend", paramAttributes.getValue("HRef"));
    }
    else if (paramString2.equals("Map"))
    {
      i = Catalog.PUBLIC;
      localVector.add(paramAttributes.getValue("PublicId"));
      localVector.add(paramAttributes.getValue("HRef"));
      Debug.message(4, "Map", PublicId.normalize(paramAttributes.getValue("PublicId")), paramAttributes.getValue("HRef"));
    }
    else if (paramString2.equals("Remap"))
    {
      i = Catalog.SYSTEM;
      localVector.add(paramAttributes.getValue("SystemId"));
      localVector.add(paramAttributes.getValue("HRef"));
      Debug.message(4, "Remap", paramAttributes.getValue("SystemId"), paramAttributes.getValue("HRef"));
    }
    else if (!paramString2.equals("XMLCatalog"))
    {
      Debug.message(1, "Invalid catalog entry type", paramString2);
    }
    if (i >= 0) {
      try
      {
        CatalogEntry localCatalogEntry = new CatalogEntry(i, localVector);
        this.catalog.addEntry(localCatalogEntry);
      }
      catch (CatalogException localCatalogException)
      {
        if (localCatalogException.getExceptionType() == 3) {
          Debug.message(1, "Invalid catalog entry type", paramString2);
        } else if (localCatalogException.getExceptionType() == 2) {
          Debug.message(1, "Invalid catalog entry", paramString2);
        }
      }
    }
  }
  
  public void endElement(String paramString1, String paramString2, String paramString3)
    throws SAXException
  {}
  
  public void characters(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {}
  
  public void ignorableWhitespace(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {}
  
  public void processingInstruction(String paramString1, String paramString2)
    throws SAXException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\readers\XCatalogReader.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */