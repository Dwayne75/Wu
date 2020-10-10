package com.sun.tools.xjc.reader.internalizer;

import com.sun.xml.bind.unmarshaller.DOMScanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

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
    
    DOMForestScanner.LocationResolver resolver = new DOMForestScanner.LocationResolver(this, scanner);
    resolver.setContentHandler(contentHandler);
    
    scanner.parseWithContext(e, resolver);
  }
  
  public void scan(Document d, ContentHandler contentHandler)
    throws SAXException
  {
    scan(d.getDocumentElement(), contentHandler);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\internalizer\DOMForestScanner.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */