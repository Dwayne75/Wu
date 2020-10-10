package com.sun.xml.bind.marshaller;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.util.Which;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class SAX2DOMEx
  implements ContentHandler
{
  private Node node = null;
  private final Stack<Node> nodeStack = new Stack();
  private final FinalArrayList<String> unprocessedNamespaces = new FinalArrayList();
  private final Document document;
  
  public SAX2DOMEx(Node node)
  {
    this.node = node;
    this.nodeStack.push(this.node);
    if ((node instanceof Document)) {
      this.document = ((Document)node);
    } else {
      this.document = node.getOwnerDocument();
    }
  }
  
  public SAX2DOMEx()
    throws ParserConfigurationException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    
    this.document = factory.newDocumentBuilder().newDocument();
    this.node = this.document;
    this.nodeStack.push(this.document);
  }
  
  public final Element getCurrentElement()
  {
    return (Element)this.nodeStack.peek();
  }
  
  public Node getDOM()
  {
    return this.node;
  }
  
  public void startDocument() {}
  
  public void endDocument() {}
  
  public void startElement(String namespace, String localName, String qName, Attributes attrs)
  {
    Node parent = (Node)this.nodeStack.peek();
    
    Element element = this.document.createElementNS(namespace, qName);
    if (element == null) {
      throw new AssertionError(Messages.format("SAX2DOMEx.DomImplDoesntSupportCreateElementNs", this.document.getClass().getName(), Which.which(this.document.getClass())));
    }
    for (int i = 0; i < this.unprocessedNamespaces.size(); i += 2)
    {
      String prefix = (String)this.unprocessedNamespaces.get(i + 0);
      String uri = (String)this.unprocessedNamespaces.get(i + 1);
      String qname;
      String qname;
      if (("".equals(prefix)) || (prefix == null)) {
        qname = "xmlns";
      } else {
        qname = "xmlns:" + prefix;
      }
      if (element.hasAttributeNS("http://www.w3.org/2000/xmlns/", qname)) {
        element.removeAttributeNS("http://www.w3.org/2000/xmlns/", qname);
      }
      element.setAttributeNS("http://www.w3.org/2000/xmlns/", qname, uri);
    }
    this.unprocessedNamespaces.clear();
    
    int length = attrs.getLength();
    for (int i = 0; i < length; i++)
    {
      String namespaceuri = attrs.getURI(i);
      String value = attrs.getValue(i);
      String qname = attrs.getQName(i);
      element.setAttributeNS(namespaceuri, qname, value);
    }
    parent.appendChild(element);
    
    this.nodeStack.push(element);
  }
  
  public void endElement(String namespace, String localName, String qName)
  {
    this.nodeStack.pop();
  }
  
  public void characters(char[] ch, int start, int length)
  {
    Node parent = (Node)this.nodeStack.peek();
    Text text = this.document.createTextNode(new String(ch, start, length));
    parent.appendChild(text);
  }
  
  public void ignorableWhitespace(char[] ch, int start, int length) {}
  
  public void processingInstruction(String target, String data)
    throws SAXException
  {
    Node parent = (Node)this.nodeStack.peek();
    Node node = this.document.createProcessingInstruction(target, data);
    parent.appendChild(node);
  }
  
  public void setDocumentLocator(Locator locator) {}
  
  public void skippedEntity(String name) {}
  
  public void startPrefixMapping(String prefix, String uri)
  {
    this.unprocessedNamespaces.add(prefix);
    this.unprocessedNamespaces.add(uri);
  }
  
  public void endPrefixMapping(String prefix) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\marshaller\SAX2DOMEx.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */