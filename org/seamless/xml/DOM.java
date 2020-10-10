package org.seamless.xml;

import java.net.URI;
import javax.xml.xpath.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class DOM
{
  public static final URI XML_SCHEMA_NAMESPACE = URI.create("http://www.w3.org/2001/xml.xsd");
  public static final String CDATA_BEGIN = "<![CDATA[";
  public static final String CDATA_END = "]]>";
  private Document dom;
  
  public DOM(Document dom)
  {
    this.dom = dom;
  }
  
  public Document getW3CDocument()
  {
    return this.dom;
  }
  
  public Element createRoot(String name)
  {
    Element el = getW3CDocument().createElementNS(getRootElementNamespace(), name);
    getW3CDocument().appendChild(el);
    return el;
  }
  
  public abstract String getRootElementNamespace();
  
  public abstract DOMElement getRoot(XPath paramXPath);
  
  public abstract DOM copy();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xml\DOM.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */