package com.sun.tools.xjc.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public abstract class SubtreeCutter
  extends XMLFilterImpl
{
  private int cutDepth = 0;
  private static final ContentHandler stub = new DefaultHandler();
  private ContentHandler next;
  
  public void startDocument()
    throws SAXException
  {
    this.cutDepth = 0;
    super.startDocument();
  }
  
  public boolean isCutting()
  {
    return this.cutDepth > 0;
  }
  
  public void startCutting()
  {
    super.setContentHandler(stub);
    this.cutDepth = 1;
  }
  
  public void setContentHandler(ContentHandler handler)
  {
    this.next = handler;
    if (getContentHandler() != stub) {
      super.setContentHandler(handler);
    }
  }
  
  public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException
  {
    if (this.cutDepth > 0) {
      this.cutDepth += 1;
    }
    super.startElement(uri, localName, qName, atts);
  }
  
  public void endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    super.endElement(namespaceURI, localName, qName);
    if (this.cutDepth != 0)
    {
      this.cutDepth -= 1;
      if (this.cutDepth == 1)
      {
        super.setContentHandler(this.next);
        this.cutDepth = 0;
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\util\SubtreeCutter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */