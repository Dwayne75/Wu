package org.apache.xml.resolver.readers;

import java.util.Stack;
import java.util.Vector;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.Resolver;
import org.apache.xml.resolver.helpers.Debug;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ExtendedXMLCatalogReader
  extends OASISXMLCatalogReader
{
  public static final String extendedNamespaceName = "http://nwalsh.com/xcatalog/1.0";
  
  public void startElement(String paramString1, String paramString2, String paramString3, Attributes paramAttributes)
    throws SAXException
  {
    boolean bool = inExtensionNamespace();
    super.startElement(paramString1, paramString2, paramString3, paramAttributes);
    int i = -1;
    Vector localVector = new Vector();
    if ((paramString1 != null) && ("http://nwalsh.com/xcatalog/1.0".equals(paramString1)) && (!bool))
    {
      Object localObject;
      if (paramAttributes.getValue("xml:base") != null)
      {
        localObject = paramAttributes.getValue("xml:base");
        i = Catalog.BASE;
        localVector.add(localObject);
        this.baseURIStack.push(localObject);
        Debug.message(4, "xml:base", (String)localObject);
        try
        {
          CatalogEntry localCatalogEntry = new CatalogEntry(i, localVector);
          this.catalog.addEntry(localCatalogEntry);
        }
        catch (CatalogException localCatalogException2)
        {
          if (localCatalogException2.getExceptionType() == 3) {
            Debug.message(1, "Invalid catalog entry type", paramString2);
          } else if (localCatalogException2.getExceptionType() == 2) {
            Debug.message(1, "Invalid catalog entry (base)", paramString2);
          }
        }
        i = -1;
        localVector = new Vector();
      }
      else
      {
        this.baseURIStack.push(this.baseURIStack.peek());
      }
      if (paramString2.equals("uriSuffix"))
      {
        if (checkAttributes(paramAttributes, "suffix", "uri"))
        {
          i = Resolver.URISUFFIX;
          localVector.add(paramAttributes.getValue("suffix"));
          localVector.add(paramAttributes.getValue("uri"));
          Debug.message(4, "uriSuffix", paramAttributes.getValue("suffix"), paramAttributes.getValue("uri"));
        }
      }
      else if (paramString2.equals("systemSuffix"))
      {
        if (checkAttributes(paramAttributes, "suffix", "uri"))
        {
          i = Resolver.SYSTEMSUFFIX;
          localVector.add(paramAttributes.getValue("suffix"));
          localVector.add(paramAttributes.getValue("uri"));
          Debug.message(4, "systemSuffix", paramAttributes.getValue("suffix"), paramAttributes.getValue("uri"));
        }
      }
      else {
        Debug.message(1, "Invalid catalog entry type", paramString2);
      }
      if (i >= 0) {
        try
        {
          localObject = new CatalogEntry(i, localVector);
          this.catalog.addEntry((CatalogEntry)localObject);
        }
        catch (CatalogException localCatalogException1)
        {
          if (localCatalogException1.getExceptionType() == 3) {
            Debug.message(1, "Invalid catalog entry type", paramString2);
          } else if (localCatalogException1.getExceptionType() == 2) {
            Debug.message(1, "Invalid catalog entry", paramString2);
          }
        }
      }
    }
  }
  
  public void endElement(String paramString1, String paramString2, String paramString3)
    throws SAXException
  {
    super.endElement(paramString1, paramString2, paramString3);
    boolean bool = inExtensionNamespace();
    int i = -1;
    Vector localVector = new Vector();
    if ((paramString1 != null) && ("http://nwalsh.com/xcatalog/1.0".equals(paramString1)) && (!bool))
    {
      String str1 = (String)this.baseURIStack.pop();
      String str2 = (String)this.baseURIStack.peek();
      if (!str2.equals(str1))
      {
        i = Catalog.BASE;
        localVector.add(str2);
        Debug.message(4, "(reset) xml:base", str2);
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
            Debug.message(1, "Invalid catalog entry (rbase)", paramString2);
          }
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\readers\ExtendedXMLCatalogReader.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */