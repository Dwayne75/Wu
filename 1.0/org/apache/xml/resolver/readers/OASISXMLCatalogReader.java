package org.apache.xml.resolver.readers;

import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.helpers.PublicId;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class OASISXMLCatalogReader
  extends SAXCatalogReader
  implements SAXCatalogParser
{
  protected Catalog catalog = null;
  public static final String namespaceName = "urn:oasis:names:tc:entity:xmlns:xml:catalog";
  public static final String tr9401NamespaceName = "urn:oasis:names:tc:entity:xmlns:tr9401:catalog";
  protected Stack baseURIStack = new Stack();
  protected Stack overrideStack = new Stack();
  protected Stack namespaceStack = new Stack();
  
  public void setCatalog(Catalog paramCatalog)
  {
    this.catalog = paramCatalog;
  }
  
  public Catalog getCatalog()
  {
    return this.catalog;
  }
  
  protected boolean inExtensionNamespace()
  {
    boolean bool = false;
    Enumeration localEnumeration = this.namespaceStack.elements();
    while ((!bool) && (localEnumeration.hasMoreElements()))
    {
      String str = (String)localEnumeration.nextElement();
      if (str == null) {
        bool = true;
      } else {
        bool = (!str.equals("urn:oasis:names:tc:entity:xmlns:tr9401:catalog")) && (!str.equals("urn:oasis:names:tc:entity:xmlns:xml:catalog"));
      }
    }
    return bool;
  }
  
  public void setDocumentLocator(Locator paramLocator) {}
  
  public void startDocument()
    throws SAXException
  {
    this.baseURIStack.push(this.catalog.getCurrentBase());
    this.overrideStack.push(this.catalog.getDefaultOverride());
  }
  
  public void endDocument()
    throws SAXException
  {}
  
  public void startElement(String paramString1, String paramString2, String paramString3, Attributes paramAttributes)
    throws SAXException
  {
    int i = -1;
    Vector localVector = new Vector();
    this.namespaceStack.push(paramString1);
    boolean bool = inExtensionNamespace();
    if ((paramString1 != null) && ("urn:oasis:names:tc:entity:xmlns:xml:catalog".equals(paramString1)) && (!bool))
    {
      Object localObject1;
      if (paramAttributes.getValue("xml:base") != null)
      {
        localObject1 = paramAttributes.getValue("xml:base");
        i = Catalog.BASE;
        localVector.add(localObject1);
        this.baseURIStack.push(localObject1);
        Debug.message(4, "xml:base", (String)localObject1);
        try
        {
          CatalogEntry localCatalogEntry1 = new CatalogEntry(i, localVector);
          this.catalog.addEntry(localCatalogEntry1);
        }
        catch (CatalogException localCatalogException3)
        {
          if (localCatalogException3.getExceptionType() == 3) {
            Debug.message(1, "Invalid catalog entry type", paramString2);
          } else if (localCatalogException3.getExceptionType() == 2) {
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
      if (((paramString2.equals("catalog")) || (paramString2.equals("group"))) && (paramAttributes.getValue("prefer") != null))
      {
        localObject1 = paramAttributes.getValue("prefer");
        if (((String)localObject1).equals("public"))
        {
          localObject1 = "yes";
        }
        else if (((String)localObject1).equals("system"))
        {
          localObject1 = "no";
        }
        else
        {
          Debug.message(1, "Invalid prefer: must be 'system' or 'public'", paramString2);
          localObject1 = this.catalog.getDefaultOverride();
        }
        i = Catalog.OVERRIDE;
        localVector.add(localObject1);
        this.overrideStack.push(localObject1);
        Debug.message(4, "override", (String)localObject1);
        try
        {
          CatalogEntry localCatalogEntry2 = new CatalogEntry(i, localVector);
          this.catalog.addEntry(localCatalogEntry2);
        }
        catch (CatalogException localCatalogException4)
        {
          if (localCatalogException4.getExceptionType() == 3) {
            Debug.message(1, "Invalid catalog entry type", paramString2);
          } else if (localCatalogException4.getExceptionType() == 2) {
            Debug.message(1, "Invalid catalog entry (override)", paramString2);
          }
        }
        i = -1;
        localVector = new Vector();
      }
      else
      {
        this.overrideStack.push(this.overrideStack.peek());
      }
      if (paramString2.equals("delegatePublic"))
      {
        if (checkAttributes(paramAttributes, "publicIdStartString", "catalog"))
        {
          i = Catalog.DELEGATE_PUBLIC;
          localVector.add(paramAttributes.getValue("publicIdStartString"));
          localVector.add(paramAttributes.getValue("catalog"));
          Debug.message(4, "delegatePublic", PublicId.normalize(paramAttributes.getValue("publicIdStartString")), paramAttributes.getValue("catalog"));
        }
      }
      else if (paramString2.equals("delegateSystem"))
      {
        if (checkAttributes(paramAttributes, "systemIdStartString", "catalog"))
        {
          i = Catalog.DELEGATE_SYSTEM;
          localVector.add(paramAttributes.getValue("systemIdStartString"));
          localVector.add(paramAttributes.getValue("catalog"));
          Debug.message(4, "delegateSystem", paramAttributes.getValue("systemIdStartString"), paramAttributes.getValue("catalog"));
        }
      }
      else if (paramString2.equals("delegateURI"))
      {
        if (checkAttributes(paramAttributes, "uriStartString", "catalog"))
        {
          i = Catalog.DELEGATE_URI;
          localVector.add(paramAttributes.getValue("uriStartString"));
          localVector.add(paramAttributes.getValue("catalog"));
          Debug.message(4, "delegateURI", paramAttributes.getValue("uriStartString"), paramAttributes.getValue("catalog"));
        }
      }
      else if (paramString2.equals("rewriteSystem"))
      {
        if (checkAttributes(paramAttributes, "systemIdStartString", "rewritePrefix"))
        {
          i = Catalog.REWRITE_SYSTEM;
          localVector.add(paramAttributes.getValue("systemIdStartString"));
          localVector.add(paramAttributes.getValue("rewritePrefix"));
          Debug.message(4, "rewriteSystem", paramAttributes.getValue("systemIdStartString"), paramAttributes.getValue("rewritePrefix"));
        }
      }
      else if (paramString2.equals("rewriteURI"))
      {
        if (checkAttributes(paramAttributes, "uriStartString", "rewritePrefix"))
        {
          i = Catalog.REWRITE_URI;
          localVector.add(paramAttributes.getValue("uriStartString"));
          localVector.add(paramAttributes.getValue("rewritePrefix"));
          Debug.message(4, "rewriteURI", paramAttributes.getValue("uriStartString"), paramAttributes.getValue("rewritePrefix"));
        }
      }
      else if (paramString2.equals("nextCatalog"))
      {
        if (checkAttributes(paramAttributes, "catalog"))
        {
          i = Catalog.CATALOG;
          localVector.add(paramAttributes.getValue("catalog"));
          Debug.message(4, "nextCatalog", paramAttributes.getValue("catalog"));
        }
      }
      else if (paramString2.equals("public"))
      {
        if (checkAttributes(paramAttributes, "publicId", "uri"))
        {
          i = Catalog.PUBLIC;
          localVector.add(paramAttributes.getValue("publicId"));
          localVector.add(paramAttributes.getValue("uri"));
          Debug.message(4, "public", PublicId.normalize(paramAttributes.getValue("publicId")), paramAttributes.getValue("uri"));
        }
      }
      else if (paramString2.equals("system"))
      {
        if (checkAttributes(paramAttributes, "systemId", "uri"))
        {
          i = Catalog.SYSTEM;
          localVector.add(paramAttributes.getValue("systemId"));
          localVector.add(paramAttributes.getValue("uri"));
          Debug.message(4, "system", paramAttributes.getValue("systemId"), paramAttributes.getValue("uri"));
        }
      }
      else if (paramString2.equals("uri"))
      {
        if (checkAttributes(paramAttributes, "name", "uri"))
        {
          i = Catalog.URI;
          localVector.add(paramAttributes.getValue("name"));
          localVector.add(paramAttributes.getValue("uri"));
          Debug.message(4, "uri", paramAttributes.getValue("name"), paramAttributes.getValue("uri"));
        }
      }
      else if ((!paramString2.equals("catalog")) && (!paramString2.equals("group"))) {
        Debug.message(1, "Invalid catalog entry type", paramString2);
      }
      if (i >= 0) {
        try
        {
          localObject1 = new CatalogEntry(i, localVector);
          this.catalog.addEntry((CatalogEntry)localObject1);
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
    if ((paramString1 != null) && ("urn:oasis:names:tc:entity:xmlns:tr9401:catalog".equals(paramString1)) && (!bool))
    {
      Object localObject2;
      if (paramAttributes.getValue("xml:base") != null)
      {
        localObject2 = paramAttributes.getValue("xml:base");
        i = Catalog.BASE;
        localVector.add(localObject2);
        this.baseURIStack.push(localObject2);
        Debug.message(4, "xml:base", (String)localObject2);
        try
        {
          CatalogEntry localCatalogEntry3 = new CatalogEntry(i, localVector);
          this.catalog.addEntry(localCatalogEntry3);
        }
        catch (CatalogException localCatalogException5)
        {
          if (localCatalogException5.getExceptionType() == 3) {
            Debug.message(1, "Invalid catalog entry type", paramString2);
          } else if (localCatalogException5.getExceptionType() == 2) {
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
      if (paramString2.equals("doctype"))
      {
        i = Catalog.DOCTYPE;
        localVector.add(paramAttributes.getValue("name"));
        localVector.add(paramAttributes.getValue("uri"));
      }
      else if (paramString2.equals("document"))
      {
        i = Catalog.DOCUMENT;
        localVector.add(paramAttributes.getValue("uri"));
      }
      else if (paramString2.equals("dtddecl"))
      {
        i = Catalog.DTDDECL;
        localVector.add(paramAttributes.getValue("publicId"));
        localVector.add(paramAttributes.getValue("uri"));
      }
      else if (paramString2.equals("entity"))
      {
        i = Catalog.ENTITY;
        localVector.add(paramAttributes.getValue("name"));
        localVector.add(paramAttributes.getValue("uri"));
      }
      else if (paramString2.equals("linktype"))
      {
        i = Catalog.LINKTYPE;
        localVector.add(paramAttributes.getValue("name"));
        localVector.add(paramAttributes.getValue("uri"));
      }
      else if (paramString2.equals("notation"))
      {
        i = Catalog.NOTATION;
        localVector.add(paramAttributes.getValue("name"));
        localVector.add(paramAttributes.getValue("uri"));
      }
      else if (paramString2.equals("sgmldecl"))
      {
        i = Catalog.SGMLDECL;
        localVector.add(paramAttributes.getValue("uri"));
      }
      else
      {
        Debug.message(1, "Invalid catalog entry type", paramString2);
      }
      if (i >= 0) {
        try
        {
          localObject2 = new CatalogEntry(i, localVector);
          this.catalog.addEntry((CatalogEntry)localObject2);
        }
        catch (CatalogException localCatalogException2)
        {
          if (localCatalogException2.getExceptionType() == 3) {
            Debug.message(1, "Invalid catalog entry type", paramString2);
          } else if (localCatalogException2.getExceptionType() == 2) {
            Debug.message(1, "Invalid catalog entry", paramString2);
          }
        }
      }
    }
  }
  
  public boolean checkAttributes(Attributes paramAttributes, String paramString)
  {
    if (paramAttributes.getValue(paramString) == null)
    {
      Debug.message(1, "Error: required attribute " + paramString + " missing.");
      return false;
    }
    return true;
  }
  
  public boolean checkAttributes(Attributes paramAttributes, String paramString1, String paramString2)
  {
    return (checkAttributes(paramAttributes, paramString1)) && (checkAttributes(paramAttributes, paramString2));
  }
  
  public void endElement(String paramString1, String paramString2, String paramString3)
    throws SAXException
  {
    int i = -1;
    Vector localVector = new Vector();
    boolean bool = inExtensionNamespace();
    String str1;
    String str2;
    if ((paramString1 != null) && (!bool) && (("urn:oasis:names:tc:entity:xmlns:xml:catalog".equals(paramString1)) || ("urn:oasis:names:tc:entity:xmlns:tr9401:catalog".equals(paramString1))))
    {
      str1 = (String)this.baseURIStack.pop();
      str2 = (String)this.baseURIStack.peek();
      if (!str2.equals(str1))
      {
        i = Catalog.BASE;
        localVector.add(str2);
        Debug.message(4, "(reset) xml:base", str2);
        try
        {
          CatalogEntry localCatalogEntry1 = new CatalogEntry(i, localVector);
          this.catalog.addEntry(localCatalogEntry1);
        }
        catch (CatalogException localCatalogException1)
        {
          if (localCatalogException1.getExceptionType() == 3) {
            Debug.message(1, "Invalid catalog entry type", paramString2);
          } else if (localCatalogException1.getExceptionType() == 2) {
            Debug.message(1, "Invalid catalog entry (rbase)", paramString2);
          }
        }
      }
    }
    if ((paramString1 != null) && ("urn:oasis:names:tc:entity:xmlns:xml:catalog".equals(paramString1)) && (!bool) && ((paramString2.equals("catalog")) || (paramString2.equals("group"))))
    {
      str1 = (String)this.overrideStack.pop();
      str2 = (String)this.overrideStack.peek();
      if (!str2.equals(str1))
      {
        i = Catalog.OVERRIDE;
        localVector.add(str2);
        this.overrideStack.push(str2);
        Debug.message(4, "(reset) override", str2);
        try
        {
          CatalogEntry localCatalogEntry2 = new CatalogEntry(i, localVector);
          this.catalog.addEntry(localCatalogEntry2);
        }
        catch (CatalogException localCatalogException2)
        {
          if (localCatalogException2.getExceptionType() == 3) {
            Debug.message(1, "Invalid catalog entry type", paramString2);
          } else if (localCatalogException2.getExceptionType() == 2) {
            Debug.message(1, "Invalid catalog entry (roverride)", paramString2);
          }
        }
      }
    }
    this.namespaceStack.pop();
  }
  
  public void characters(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {}
  
  public void ignorableWhitespace(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {}
  
  public void processingInstruction(String paramString1, String paramString2)
    throws SAXException
  {}
  
  public void skippedEntity(String paramString)
    throws SAXException
  {}
  
  public void startPrefixMapping(String paramString1, String paramString2)
    throws SAXException
  {}
  
  public void endPrefixMapping(String paramString)
    throws SAXException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\readers\OASISXMLCatalogReader.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */