package org.seamless.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class DOMElement<CHILD extends DOMElement, PARENT extends DOMElement>
{
  public final DOMElement<CHILD, PARENT>.Builder<PARENT> PARENT_BUILDER;
  public final DOMElement<CHILD, PARENT>.ArrayBuilder<CHILD> CHILD_BUILDER;
  private final XPath xpath;
  private Element element;
  
  public DOMElement(XPath xpath, Element element)
  {
    this.xpath = xpath;
    this.element = element;
    this.PARENT_BUILDER = createParentBuilder(this);
    this.CHILD_BUILDER = createChildBuilder(this);
  }
  
  public Element getW3CElement()
  {
    return this.element;
  }
  
  public String getElementName()
  {
    return getW3CElement().getNodeName();
  }
  
  public String getContent()
  {
    return getW3CElement().getTextContent();
  }
  
  public DOMElement<CHILD, PARENT> setContent(String content)
  {
    getW3CElement().setTextContent(content);
    return this;
  }
  
  public String getAttribute(String attribute)
  {
    String v = getW3CElement().getAttribute(attribute);
    return v.length() > 0 ? v : null;
  }
  
  public DOMElement setAttribute(String attribute, String value)
  {
    getW3CElement().setAttribute(attribute, value);
    return this;
  }
  
  public PARENT getParent()
  {
    return this.PARENT_BUILDER.build((Element)getW3CElement().getParentNode());
  }
  
  public CHILD[] getChildren()
  {
    NodeList nodes = getW3CElement().getChildNodes();
    List<CHILD> children = new ArrayList();
    for (int i = 0; i < nodes.getLength(); i++)
    {
      Node node = nodes.item(i);
      if (node.getNodeType() == 1) {
        children.add(this.CHILD_BUILDER.build((Element)node));
      }
    }
    return (DOMElement[])children.toArray(this.CHILD_BUILDER.newChildrenArray(children.size()));
  }
  
  public CHILD[] getChildren(String name)
  {
    Collection<CHILD> list = getXPathChildElements(this.CHILD_BUILDER, prefix(name));
    return (DOMElement[])list.toArray(this.CHILD_BUILDER.newChildrenArray(list.size()));
  }
  
  public CHILD getRequiredChild(String name)
    throws ParserException
  {
    CHILD[] children = getChildren(name);
    if (children.length != 1) {
      throw new ParserException("Required single child element of '" + getElementName() + "' not found: " + name);
    }
    return children[0];
  }
  
  public CHILD[] findChildren(String name)
  {
    Collection<CHILD> list = getXPathChildElements(this.CHILD_BUILDER, "descendant::" + prefix(name));
    return (DOMElement[])list.toArray(this.CHILD_BUILDER.newChildrenArray(list.size()));
  }
  
  public CHILD findChildWithIdentifier(String id)
  {
    Collection<CHILD> list = getXPathChildElements(this.CHILD_BUILDER, "descendant::" + prefix("*") + "[@id=\"" + id + "\"]");
    if (list.size() == 1) {
      return (DOMElement)list.iterator().next();
    }
    return null;
  }
  
  public CHILD getFirstChild(String name)
  {
    return getXPathChildElement(this.CHILD_BUILDER, prefix(name) + "[1]");
  }
  
  public CHILD createChild(String name)
  {
    return createChild(name, null);
  }
  
  public CHILD createChild(String name, String namespaceURI)
  {
    CHILD child = this.CHILD_BUILDER.build(namespaceURI == null ? getW3CElement().getOwnerDocument().createElement(name) : getW3CElement().getOwnerDocument().createElementNS(namespaceURI, name));
    
    getW3CElement().appendChild(child.getW3CElement());
    return child;
  }
  
  public CHILD appendChild(CHILD el, boolean copy)
  {
    el = adoptOrImport(getW3CElement().getOwnerDocument(), el, copy);
    getW3CElement().appendChild(el.getW3CElement());
    return el;
  }
  
  public CHILD replaceChild(CHILD original, CHILD replacement, boolean copy)
  {
    replacement = adoptOrImport(getW3CElement().getOwnerDocument(), replacement, copy);
    getW3CElement().replaceChild(replacement.getW3CElement(), original.getW3CElement());
    return replacement;
  }
  
  public void replaceEqualChild(DOMElement source, String identifier)
  {
    DOMElement original = findChildWithIdentifier(identifier);
    DOMElement replacement = source.findChildWithIdentifier(identifier);
    original.getParent().replaceChild(original, replacement, true);
  }
  
  public void removeChild(CHILD el)
  {
    getW3CElement().removeChild(el.getW3CElement());
  }
  
  public void removeChildren()
  {
    NodeList children = getW3CElement().getChildNodes();
    for (int i = 0; i < children.getLength(); i++)
    {
      Node child = children.item(i);
      getW3CElement().removeChild(child);
    }
  }
  
  protected CHILD adoptOrImport(Document document, CHILD child, boolean copy)
  {
    if (document != null) {
      if (copy) {
        child = this.CHILD_BUILDER.build((Element)document.importNode(child.getW3CElement(), true));
      } else {
        child = this.CHILD_BUILDER.build((Element)document.adoptNode(child.getW3CElement()));
      }
    }
    return child;
  }
  
  protected abstract DOMElement<CHILD, PARENT>.Builder<PARENT> createParentBuilder(DOMElement paramDOMElement);
  
  protected abstract DOMElement<CHILD, PARENT>.ArrayBuilder<CHILD> createChildBuilder(DOMElement paramDOMElement);
  
  public String toSimpleXMLString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<").append(getElementName());
    NamedNodeMap map = getW3CElement().getAttributes();
    for (int i = 0; i < map.getLength(); i++)
    {
      Node attr = map.item(i);
      sb.append(" ").append(attr.getNodeName()).append("=\"").append(attr.getTextContent()).append("\"");
    }
    if (getContent().length() > 0) {
      sb.append(">").append(getContent()).append("</").append(getElementName()).append(">");
    } else {
      sb.append("/>");
    }
    return sb.toString();
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") " + (getW3CElement() == null ? "UNBOUND" : getElementName());
  }
  
  public XPath getXpath()
  {
    return this.xpath;
  }
  
  protected String prefix(String localName)
  {
    return localName;
  }
  
  public Collection<PARENT> getXPathParentElements(DOMElement<CHILD, PARENT>.Builder<CHILD> builder, String expr)
  {
    return getXPathElements(builder, expr);
  }
  
  public Collection<CHILD> getXPathChildElements(DOMElement<CHILD, PARENT>.Builder<CHILD> builder, String expr)
  {
    return getXPathElements(builder, expr);
  }
  
  public PARENT getXPathParentElement(DOMElement<CHILD, PARENT>.Builder<PARENT> builder, String expr)
  {
    Node node = (Node)getXPathResult(getW3CElement(), expr, XPathConstants.NODE);
    return (node != null) && (node.getNodeType() == 1) ? builder.build((Element)node) : null;
  }
  
  public CHILD getXPathChildElement(DOMElement<CHILD, PARENT>.Builder<CHILD> builder, String expr)
  {
    Node node = (Node)getXPathResult(getW3CElement(), expr, XPathConstants.NODE);
    return (node != null) && (node.getNodeType() == 1) ? builder.build((Element)node) : null;
  }
  
  public Collection getXPathElements(Builder builder, String expr)
  {
    Collection col = new ArrayList();
    NodeList result = (NodeList)getXPathResult(getW3CElement(), expr, XPathConstants.NODESET);
    for (int i = 0; i < result.getLength(); i++)
    {
      DOMElement e = builder.build((Element)result.item(i));
      col.add(e);
    }
    return col;
  }
  
  public String getXPathString(XPath xpath, String expr)
  {
    return getXPathResult(getW3CElement(), expr, null).toString();
  }
  
  public Object getXPathResult(String expr, QName result)
  {
    return getXPathResult(getW3CElement(), expr, result);
  }
  
  public Object getXPathResult(Node context, String expr, QName result)
  {
    try
    {
      if (result == null) {
        return this.xpath.evaluate(expr, context);
      }
      return this.xpath.evaluate(expr, context, result);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public abstract class Builder<T extends DOMElement>
  {
    public DOMElement element;
    
    protected Builder(DOMElement element)
    {
      this.element = element;
    }
    
    public abstract T build(Element paramElement);
    
    public T firstChildOrNull(String elementName)
    {
      DOMElement el = this.element.getFirstChild(elementName);
      return el != null ? build(el.getW3CElement()) : null;
    }
  }
  
  public abstract class ArrayBuilder<T extends DOMElement>
    extends DOMElement<CHILD, PARENT>.Builder<T>
  {
    protected ArrayBuilder(DOMElement element)
    {
      super(element);
    }
    
    public abstract T[] newChildrenArray(int paramInt);
    
    public T[] getChildElements()
    {
      return buildArray(this.element.getChildren());
    }
    
    public T[] getChildElements(String elementName)
    {
      return buildArray(this.element.getChildren(elementName));
    }
    
    protected T[] buildArray(DOMElement[] list)
    {
      T[] children = newChildrenArray(list.length);
      for (int i = 0; i < children.length; i++) {
        children[i] = build(list[i].getW3CElement());
      }
      return children;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xml\DOMElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */