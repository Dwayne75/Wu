package com.sun.tools.xjc.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class ForkContentHandler
  implements ContentHandler
{
  private final ContentHandler lhs;
  private final ContentHandler rhs;
  
  public ForkContentHandler(ContentHandler first, ContentHandler second)
  {
    this.lhs = first;
    this.rhs = second;
  }
  
  public static ContentHandler create(ContentHandler[] handlers)
  {
    if (handlers.length == 0) {
      throw new IllegalArgumentException();
    }
    ContentHandler result = handlers[0];
    for (int i = 1; i < handlers.length; i++) {
      result = new ForkContentHandler(result, handlers[i]);
    }
    return result;
  }
  
  public void setDocumentLocator(Locator locator)
  {
    this.lhs.setDocumentLocator(locator);
    this.rhs.setDocumentLocator(locator);
  }
  
  public void startDocument()
    throws SAXException
  {
    this.lhs.startDocument();
    this.rhs.startDocument();
  }
  
  public void endDocument()
    throws SAXException
  {
    this.lhs.endDocument();
    this.rhs.endDocument();
  }
  
  public void startPrefixMapping(String prefix, String uri)
    throws SAXException
  {
    this.lhs.startPrefixMapping(prefix, uri);
    this.rhs.startPrefixMapping(prefix, uri);
  }
  
  public void endPrefixMapping(String prefix)
    throws SAXException
  {
    this.lhs.endPrefixMapping(prefix);
    this.rhs.endPrefixMapping(prefix);
  }
  
  public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    this.lhs.startElement(uri, localName, qName, attributes);
    this.rhs.startElement(uri, localName, qName, attributes);
  }
  
  public void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    this.lhs.endElement(uri, localName, qName);
    this.rhs.endElement(uri, localName, qName);
  }
  
  public void characters(char[] ch, int start, int length)
    throws SAXException
  {
    this.lhs.characters(ch, start, length);
    this.rhs.characters(ch, start, length);
  }
  
  public void ignorableWhitespace(char[] ch, int start, int length)
    throws SAXException
  {
    this.lhs.ignorableWhitespace(ch, start, length);
    this.rhs.ignorableWhitespace(ch, start, length);
  }
  
  public void processingInstruction(String target, String data)
    throws SAXException
  {
    this.lhs.processingInstruction(target, data);
    this.rhs.processingInstruction(target, data);
  }
  
  public void skippedEntity(String name)
    throws SAXException
  {
    this.lhs.skippedEntity(name);
    this.rhs.skippedEntity(name);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\util\ForkContentHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */