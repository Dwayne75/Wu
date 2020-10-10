package org.apache.xml.resolver.readers;

import java.io.IOException;
import java.io.PrintStream;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXParserHandler
  extends DefaultHandler
{
  private EntityResolver er = null;
  private ContentHandler ch = null;
  
  public void setEntityResolver(EntityResolver paramEntityResolver)
  {
    this.er = paramEntityResolver;
  }
  
  public void setContentHandler(ContentHandler paramContentHandler)
  {
    this.ch = paramContentHandler;
  }
  
  public InputSource resolveEntity(String paramString1, String paramString2)
    throws SAXException
  {
    if (this.er != null) {
      try
      {
        return this.er.resolveEntity(paramString1, paramString2);
      }
      catch (IOException localIOException)
      {
        System.out.println("resolveEntity threw IOException!");
        return null;
      }
    }
    return null;
  }
  
  public void characters(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {
    if (this.ch != null) {
      this.ch.characters(paramArrayOfChar, paramInt1, paramInt2);
    }
  }
  
  public void endDocument()
    throws SAXException
  {
    if (this.ch != null) {
      this.ch.endDocument();
    }
  }
  
  public void endElement(String paramString1, String paramString2, String paramString3)
    throws SAXException
  {
    if (this.ch != null) {
      this.ch.endElement(paramString1, paramString2, paramString3);
    }
  }
  
  public void endPrefixMapping(String paramString)
    throws SAXException
  {
    if (this.ch != null) {
      this.ch.endPrefixMapping(paramString);
    }
  }
  
  public void ignorableWhitespace(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {
    if (this.ch != null) {
      this.ch.ignorableWhitespace(paramArrayOfChar, paramInt1, paramInt2);
    }
  }
  
  public void processingInstruction(String paramString1, String paramString2)
    throws SAXException
  {
    if (this.ch != null) {
      this.ch.processingInstruction(paramString1, paramString2);
    }
  }
  
  public void setDocumentLocator(Locator paramLocator)
  {
    if (this.ch != null) {
      this.ch.setDocumentLocator(paramLocator);
    }
  }
  
  public void skippedEntity(String paramString)
    throws SAXException
  {
    if (this.ch != null) {
      this.ch.skippedEntity(paramString);
    }
  }
  
  public void startDocument()
    throws SAXException
  {
    if (this.ch != null) {
      this.ch.startDocument();
    }
  }
  
  public void startElement(String paramString1, String paramString2, String paramString3, Attributes paramAttributes)
    throws SAXException
  {
    if (this.ch != null) {
      this.ch.startElement(paramString1, paramString2, paramString3, paramAttributes);
    }
  }
  
  public void startPrefixMapping(String paramString1, String paramString2)
    throws SAXException
  {
    if (this.ch != null) {
      this.ch.startPrefixMapping(paramString1, paramString2);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\readers\SAXParserHandler.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */