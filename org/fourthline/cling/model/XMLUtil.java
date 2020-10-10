package org.fourthline.cling.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil
{
  public static String documentToString(Document document)
    throws Exception
  {
    return documentToString(document, true);
  }
  
  public static String documentToString(Document document, boolean standalone)
    throws Exception
  {
    String prol = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"" + (standalone ? "yes" : "no") + "\"?>";
    return prol + nodeToString(document.getDocumentElement(), new HashSet(), document.getDocumentElement().getNamespaceURI());
  }
  
  public static String documentToFragmentString(Document document)
    throws Exception
  {
    return nodeToString(document.getDocumentElement(), new HashSet(), document.getDocumentElement().getNamespaceURI());
  }
  
  protected static String nodeToString(Node node, Set<String> parentPrefixes, String namespaceURI)
    throws Exception
  {
    StringBuilder b = new StringBuilder();
    if (node == null) {
      return "";
    }
    int i;
    if ((node instanceof Element))
    {
      Element element = (Element)node;
      b.append("<");
      b.append(element.getNodeName());
      
      Map<String, String> thisLevelPrefixes = new HashMap();
      if ((element.getPrefix() != null) && (!parentPrefixes.contains(element.getPrefix()))) {
        thisLevelPrefixes.put(element.getPrefix(), element.getNamespaceURI());
      }
      NamedNodeMap map;
      if (element.hasAttributes())
      {
        map = element.getAttributes();
        for (int i = 0; i < map.getLength(); i++)
        {
          Node attr = map.item(i);
          if (!attr.getNodeName().startsWith("xmlns"))
          {
            if ((attr.getPrefix() != null) && (!parentPrefixes.contains(attr.getPrefix()))) {
              thisLevelPrefixes.put(attr.getPrefix(), element.getNamespaceURI());
            }
            b.append(" ");
            b.append(attr.getNodeName());
            b.append("=\"");
            b.append(attr.getNodeValue());
            b.append("\"");
          }
        }
      }
      if ((namespaceURI != null) && (!thisLevelPrefixes.containsValue(namespaceURI)) && 
        (!namespaceURI.equals(element.getParentNode().getNamespaceURI()))) {
        b.append(" xmlns=\"").append(namespaceURI).append("\"");
      }
      for (Map.Entry<String, String> entry : thisLevelPrefixes.entrySet())
      {
        b.append(" xmlns:").append((String)entry.getKey()).append("=\"").append((String)entry.getValue()).append("\"");
        parentPrefixes.add(entry.getKey());
      }
      NodeList children = element.getChildNodes();
      boolean hasOnlyAttributes = true;
      for (int i = 0; i < children.getLength(); i++)
      {
        Node child = children.item(i);
        if (child.getNodeType() != 2)
        {
          hasOnlyAttributes = false;
          break;
        }
      }
      if (!hasOnlyAttributes)
      {
        b.append(">");
        for (i = 0; i < children.getLength(); i++) {
          b.append(nodeToString(children.item(i), parentPrefixes, children.item(i).getNamespaceURI()));
        }
        b.append("</");
        b.append(element.getNodeName());
        b.append(">");
      }
      else
      {
        b.append("/>");
      }
      for (String thisLevelPrefix : thisLevelPrefixes.keySet()) {
        parentPrefixes.remove(thisLevelPrefix);
      }
    }
    else if (node.getNodeValue() != null)
    {
      b.append(encodeText(node.getNodeValue(), node instanceof Attr));
    }
    return b.toString();
  }
  
  public static String encodeText(String s)
  {
    return encodeText(s, true);
  }
  
  public static String encodeText(String s, boolean encodeQuotes)
  {
    s = s.replaceAll("&", "&amp;");
    s = s.replaceAll("<", "&lt;");
    s = s.replaceAll(">", "&gt;");
    if (encodeQuotes)
    {
      s = s.replaceAll("'", "&apos;");
      s = s.replaceAll("\"", "&quot;");
    }
    return s;
  }
  
  public static Element appendNewElement(Document document, Element parent, Enum el)
  {
    return appendNewElement(document, parent, el.toString());
  }
  
  public static Element appendNewElement(Document document, Element parent, String element)
  {
    Element child = document.createElement(element);
    parent.appendChild(child);
    return child;
  }
  
  public static Element appendNewElementIfNotNull(Document document, Element parent, Enum el, Object content)
  {
    return appendNewElementIfNotNull(document, parent, el, content, null);
  }
  
  public static Element appendNewElementIfNotNull(Document document, Element parent, Enum el, Object content, String namespace)
  {
    return appendNewElementIfNotNull(document, parent, el.toString(), content, namespace);
  }
  
  public static Element appendNewElementIfNotNull(Document document, Element parent, String element, Object content)
  {
    return appendNewElementIfNotNull(document, parent, element, content, null);
  }
  
  public static Element appendNewElementIfNotNull(Document document, Element parent, String element, Object content, String namespace)
  {
    if (content == null) {
      return parent;
    }
    return appendNewElement(document, parent, element, content, namespace);
  }
  
  public static Element appendNewElement(Document document, Element parent, String element, Object content)
  {
    return appendNewElement(document, parent, element, content, null);
  }
  
  public static Element appendNewElement(Document document, Element parent, String element, Object content, String namespace)
  {
    Element childElement;
    Element childElement;
    if (namespace != null) {
      childElement = document.createElementNS(namespace, element);
    } else {
      childElement = document.createElement(element);
    }
    if (content != null) {
      childElement.appendChild(document.createTextNode(content.toString()));
    }
    parent.appendChild(childElement);
    return childElement;
  }
  
  public static String getTextContent(Node node)
  {
    StringBuffer buffer = new StringBuffer();
    NodeList childList = node.getChildNodes();
    for (int i = 0; i < childList.getLength(); i++)
    {
      Node child = childList.item(i);
      if (child.getNodeType() == 3) {
        buffer.append(child.getNodeValue());
      }
    }
    return buffer.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\XMLUtil.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */