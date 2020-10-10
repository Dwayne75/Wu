package com.sun.tools.xjc.reader.internalizer;

import com.sun.xml.bind.marshaller.SAX2DOMEx;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

class DOMBuilder
  extends SAX2DOMEx
{
  private final LocatorTable locatorTable;
  private final Set outerMostBindings;
  private Locator locator;
  
  public DOMBuilder(Document dom, LocatorTable ltable, Set outerMostBindings)
  {
    super(dom);
    this.locatorTable = ltable;
    this.outerMostBindings = outerMostBindings;
  }
  
  public void setDocumentLocator(Locator locator)
  {
    this.locator = locator;
    super.setDocumentLocator(locator);
  }
  
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
  {
    super.startElement(namespaceURI, localName, qName, atts);
    
    Element e = getCurrentElement();
    this.locatorTable.storeStartLocation(e, this.locator);
    if (("http://java.sun.com/xml/ns/jaxb".equals(e.getNamespaceURI())) && ("bindings".equals(e.getLocalName())))
    {
      Node p = e.getParentNode();
      if (((p instanceof Document)) || (((p instanceof Element)) && (!e.getNamespaceURI().equals(p.getNamespaceURI())))) {
        this.outerMostBindings.add(e);
      }
    }
  }
  
  public void endElement(String namespaceURI, String localName, String qName)
  {
    this.locatorTable.storeEndLocation(getCurrentElement(), this.locator);
    super.endElement(namespaceURI, localName, qName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\internalizer\DOMBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */