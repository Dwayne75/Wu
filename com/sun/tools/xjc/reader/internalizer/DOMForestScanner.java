package com.sun.tools.xjc.reader.internalizer;

import com.sun.xml.bind.unmarshaller.DOMScanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class DOMForestScanner
{
  private final DOMForest forest;
  
  public DOMForestScanner(DOMForest _forest)
  {
    this.forest = _forest;
  }
  
  public void scan(Element e, ContentHandler contentHandler)
    throws SAXException
  {
    DOMScanner scanner = new DOMScanner();
    
    LocationResolver resolver = new LocationResolver(scanner);
    resolver.setContentHandler(contentHandler);
    
    scanner.setContentHandler(resolver);
    scanner.scan(e);
  }
  
  public void scan(Document d, ContentHandler contentHandler)
    throws SAXException
  {
    scan(d.getDocumentElement(), contentHandler);
  }
  
  private class LocationResolver
    extends XMLFilterImpl
    implements Locator
  {
    private final DOMScanner parent;
    
    LocationResolver(DOMScanner _parent)
    {
      this.parent = _parent;
    }
    
    private boolean inStart = false;
    
    public void setDocumentLocator(Locator locator)
    {
      super.setDocumentLocator(this);
    }
    
    public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException
    {
      this.inStart = false;
      super.endElement(namespaceURI, localName, qName);
    }
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws SAXException
    {
      this.inStart = true;
      super.startElement(namespaceURI, localName, qName, atts);
    }
    
    private Locator findLocator()
    {
      Node n = this.parent.getCurrentLocation();
      if ((n instanceof Element))
      {
        Element e = (Element)n;
        if (this.inStart) {
          return DOMForestScanner.this.forest.locatorTable.getStartLocation(e);
        }
        return DOMForestScanner.this.forest.locatorTable.getEndLocation(e);
      }
      return null;
    }
    
    public int getColumnNumber()
    {
      Locator l = findLocator();
      if (l != null) {
        return l.getColumnNumber();
      }
      return -1;
    }
    
    public int getLineNumber()
    {
      Locator l = findLocator();
      if (l != null) {
        return l.getLineNumber();
      }
      return -1;
    }
    
    public String getPublicId()
    {
      Locator l = findLocator();
      if (l != null) {
        return l.getPublicId();
      }
      return null;
    }
    
    public String getSystemId()
    {
      Locator l = findLocator();
      if (l != null) {
        return l.getSystemId();
      }
      return null;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\internalizer\DOMForestScanner.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */