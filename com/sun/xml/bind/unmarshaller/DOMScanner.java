package com.sun.xml.bind.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import java.util.Enumeration;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

public class DOMScanner
  implements LocatorEx, InfosetScanner
{
  private Node currentNode = null;
  private final AttributesImpl atts = new AttributesImpl();
  private ContentHandler receiver = null;
  private Locator locator = this;
  
  public void setLocator(Locator loc)
  {
    this.locator = loc;
  }
  
  public void scan(Object node)
    throws SAXException
  {
    if ((node instanceof Document)) {
      scan((Document)node);
    } else {
      scan((Element)node);
    }
  }
  
  public void scan(Document doc)
    throws SAXException
  {
    scan(doc.getDocumentElement());
  }
  
  public void scan(Element e)
    throws SAXException
  {
    setCurrentLocation(e);
    
    this.receiver.setDocumentLocator(this.locator);
    this.receiver.startDocument();
    
    NamespaceSupport nss = new NamespaceSupport();
    buildNamespaceSupport(nss, e.getParentNode());
    for (Enumeration en = nss.getPrefixes(); en.hasMoreElements();)
    {
      String prefix = (String)en.nextElement();
      this.receiver.startPrefixMapping(prefix, nss.getURI(prefix));
    }
    visit(e);
    for (Enumeration en = nss.getPrefixes(); en.hasMoreElements();)
    {
      String prefix = (String)en.nextElement();
      this.receiver.endPrefixMapping(prefix);
    }
    setCurrentLocation(e);
    this.receiver.endDocument();
  }
  
  /**
   * @deprecated
   */
  public void parse(Element e, ContentHandler handler)
    throws SAXException
  {
    this.receiver = handler;
    
    setCurrentLocation(e);
    this.receiver.startDocument();
    
    this.receiver.setDocumentLocator(this.locator);
    visit(e);
    
    setCurrentLocation(e);
    this.receiver.endDocument();
  }
  
  /**
   * @deprecated
   */
  public void parseWithContext(Element e, ContentHandler handler)
    throws SAXException
  {
    setContentHandler(handler);
    scan(e);
  }
  
  private void buildNamespaceSupport(NamespaceSupport nss, Node node)
  {
    if ((node == null) || (node.getNodeType() != 1)) {
      return;
    }
    buildNamespaceSupport(nss, node.getParentNode());
    
    nss.pushContext();
    NamedNodeMap atts = node.getAttributes();
    for (int i = 0; i < atts.getLength(); i++)
    {
      Attr a = (Attr)atts.item(i);
      if ("xmlns".equals(a.getPrefix())) {
        nss.declarePrefix(a.getLocalName(), a.getValue());
      } else if ("xmlns".equals(a.getName())) {
        nss.declarePrefix("", a.getValue());
      }
    }
  }
  
  public void visit(Element e)
    throws SAXException
  {
    setCurrentLocation(e);
    NamedNodeMap attributes = e.getAttributes();
    
    this.atts.clear();
    int len = attributes == null ? 0 : attributes.getLength();
    for (int i = len - 1; i >= 0; i--)
    {
      Attr a = (Attr)attributes.item(i);
      String name = a.getName();
      if (name.startsWith("xmlns"))
      {
        if (name.length() == 5)
        {
          this.receiver.startPrefixMapping("", a.getValue());
        }
        else
        {
          String localName = a.getLocalName();
          if (localName == null) {
            localName = name.substring(6);
          }
          this.receiver.startPrefixMapping(localName, a.getValue());
        }
      }
      else
      {
        String uri = a.getNamespaceURI();
        if (uri == null) {
          uri = "";
        }
        String local = a.getLocalName();
        if (local == null) {
          local = a.getName();
        }
        this.atts.addAttribute(uri, local, a.getName(), "CDATA", a.getValue());
      }
    }
    String uri = e.getNamespaceURI();
    if (uri == null) {
      uri = "";
    }
    String local = e.getLocalName();
    String qname = e.getTagName();
    if (local == null) {
      local = qname;
    }
    this.receiver.startElement(uri, local, qname, this.atts);
    
    NodeList children = e.getChildNodes();
    int clen = children.getLength();
    for (int i = 0; i < clen; i++) {
      visit(children.item(i));
    }
    setCurrentLocation(e);
    this.receiver.endElement(uri, local, qname);
    for (int i = len - 1; i >= 0; i--)
    {
      Attr a = (Attr)attributes.item(i);
      String name = a.getName();
      if (name.startsWith("xmlns")) {
        if (name.length() == 5) {
          this.receiver.endPrefixMapping("");
        } else {
          this.receiver.endPrefixMapping(a.getLocalName());
        }
      }
    }
  }
  
  private void visit(Node n)
    throws SAXException
  {
    setCurrentLocation(n);
    switch (n.getNodeType())
    {
    case 3: 
    case 4: 
      String value = n.getNodeValue();
      this.receiver.characters(value.toCharArray(), 0, value.length());
      break;
    case 1: 
      visit((Element)n);
      break;
    case 5: 
      this.receiver.skippedEntity(n.getNodeName());
      break;
    case 7: 
      ProcessingInstruction pi = (ProcessingInstruction)n;
      this.receiver.processingInstruction(pi.getTarget(), pi.getData());
    }
  }
  
  private void setCurrentLocation(Node currNode)
  {
    this.currentNode = currNode;
  }
  
  public Node getCurrentLocation()
  {
    return this.currentNode;
  }
  
  public Object getCurrentElement()
  {
    return this.currentNode;
  }
  
  public LocatorEx getLocator()
  {
    return this;
  }
  
  public void setContentHandler(ContentHandler handler)
  {
    this.receiver = handler;
  }
  
  public ContentHandler getContentHandler()
  {
    return this.receiver;
  }
  
  public String getPublicId()
  {
    return null;
  }
  
  public String getSystemId()
  {
    return null;
  }
  
  public int getLineNumber()
  {
    return -1;
  }
  
  public int getColumnNumber()
  {
    return -1;
  }
  
  public ValidationEventLocator getLocation()
  {
    return new ValidationEventLocatorImpl(getCurrentLocation());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\unmarshaller\DOMScanner.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */