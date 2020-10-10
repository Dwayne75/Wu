package com.sun.tools.xjc.reader.internalizer;

import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.tools.xjc.util.DOMUtils;
import com.sun.tools.xjc.util.EditDistance;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class Internalizer
{
  private final DOMForest forest;
  private ErrorHandler errorHandler;
  private static final String EXTENSION_PREFIXES = "extensionBindingPrefixes";
  
  static void transform(DOMForest forest)
    throws SAXException
  {
    new Internalizer(forest).transform();
  }
  
  private Internalizer(DOMForest forest)
  {
    this.errorHandler = forest.getErrorHandler();
    this.forest = forest;
  }
  
  private void transform()
    throws SAXException
  {
    Map targetNodes = new HashMap();
    for (Iterator itr = this.forest.outerMostBindings.iterator(); itr.hasNext();)
    {
      Element jaxbBindings = (Element)itr.next();
      
      buildTargetNodeMap(jaxbBindings, jaxbBindings, targetNodes);
    }
    for (Iterator itr = this.forest.outerMostBindings.iterator(); itr.hasNext();)
    {
      Element jaxbBindings = (Element)itr.next();
      
      move(jaxbBindings, targetNodes);
    }
  }
  
  private void validate(Element bindings)
    throws SAXException
  {
    NamedNodeMap atts = bindings.getAttributes();
    for (int i = 0; i < atts.getLength(); i++)
    {
      Attr a = (Attr)atts.item(i);
      if (a.getNamespaceURI() == null) {
        if (!a.getLocalName().equals("node")) {
          if (a.getLocalName().equals("schemaLocation")) {}
        }
      }
    }
  }
  
  private void buildTargetNodeMap(Element bindings, Node inheritedTarget, Map result)
    throws SAXException
  {
    Node target = inheritedTarget;
    
    validate(bindings);
    if (bindings.getAttributeNode("schemaLocation") != null)
    {
      String schemaLocation = bindings.getAttribute("schemaLocation");
      try
      {
        schemaLocation = new URL(new URL(this.forest.getSystemId(bindings.getOwnerDocument())), schemaLocation).toExternalForm();
      }
      catch (MalformedURLException e) {}
      target = this.forest.get(schemaLocation);
      if (target == null)
      {
        reportError(bindings, Messages.format("Internalizer.IncorrectSchemaReference", schemaLocation, EditDistance.findNearest(schemaLocation, this.forest.listSystemIDs())));
        
        return;
      }
    }
    if (bindings.getAttributeNode("node") != null)
    {
      String nodeXPath = bindings.getAttribute("node");
      try
      {
        nlst = XPathAPI.selectNodeList(target, nodeXPath, bindings);
      }
      catch (TransformerException e)
      {
        NodeList nlst;
        reportError(bindings, Messages.format("Internalizer.XPathEvaluationError", e.getMessage()), e); return;
      }
      NodeList nlst;
      if (nlst.getLength() == 0)
      {
        reportError(bindings, Messages.format("Internalizer.XPathEvaluatesToNoTarget", nodeXPath));
        
        return;
      }
      if (nlst.getLength() != 1)
      {
        reportError(bindings, Messages.format("Internalizer.XPathEvaulatesToTooManyTargets", nodeXPath, new Integer(nlst.getLength())));
        
        return;
      }
      Node rnode = nlst.item(0);
      if (!(rnode instanceof Element))
      {
        reportError(bindings, Messages.format("Internalizer.XPathEvaluatesToNonElement", nodeXPath));
        
        return;
      }
      if (!this.forest.logic.checkIfValidTargetNode(this.forest, bindings, (Element)rnode))
      {
        reportError(bindings, Messages.format("Internalizer.XPathEvaluatesToNonSchemaElement", nodeXPath, rnode.getNodeName()));
        
        return;
      }
      target = (Element)rnode;
    }
    result.put(bindings, target);
    
    Element[] children = DOMUtils.getChildElements(bindings, "http://java.sun.com/xml/ns/jaxb", "bindings");
    for (int i = 0; i < children.length; i++) {
      buildTargetNodeMap(children[i], target, result);
    }
  }
  
  private void move(Element bindings, Map targetNodes)
    throws SAXException
  {
    Node target = (Node)targetNodes.get(bindings);
    if (target == null) {
      return;
    }
    Element[] children = DOMUtils.getChildElements(bindings);
    for (int i = 0; i < children.length; i++)
    {
      Element item = children[i];
      if ("bindings".equals(item.getLocalName()))
      {
        move(item, targetNodes);
      }
      else
      {
        if (!(target instanceof Element))
        {
          reportError(item, Messages.format("Internalizer.ContextNodeIsNotElement"));
          
          return;
        }
        if (!this.forest.logic.checkIfValidTargetNode(this.forest, item, (Element)target))
        {
          reportError(item, Messages.format("Internalizer.OrphanedCustomization", item.getNodeName()));
          
          return;
        }
        moveUnder(item, (Element)target);
      }
    }
  }
  
  private void moveUnder(Element decl, Element target)
  {
    Element realTarget = this.forest.logic.refineTarget(target);
    
    declExtensionNamespace(decl, target);
    
    Element p = decl;
    Set inscopes = new HashSet();
    for (;;)
    {
      NamedNodeMap atts = p.getAttributes();
      for (int i = 0; i < atts.getLength(); i++)
      {
        Attr a = (Attr)atts.item(i);
        if ("http://www.w3.org/2000/xmlns/".equals(a.getNamespaceURI()))
        {
          String prefix;
          String prefix;
          if (a.getName().indexOf(':') == -1) {
            prefix = "";
          } else {
            prefix = a.getLocalName();
          }
          if ((inscopes.add(prefix)) && (p != decl)) {
            decl.setAttributeNodeNS((Attr)a.cloneNode(true));
          }
        }
      }
      if ((p.getParentNode() instanceof Document)) {
        break;
      }
      p = (Element)p.getParentNode();
    }
    if (!inscopes.contains("")) {
      decl.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "");
    }
    if (realTarget.getOwnerDocument() != decl.getOwnerDocument())
    {
      Element original = decl;
      decl = (Element)realTarget.getOwnerDocument().importNode(decl, true);
      
      copyLocators(original, decl);
    }
    realTarget.appendChild(decl);
  }
  
  private void declExtensionNamespace(Element decl, Element target)
  {
    if (!"http://java.sun.com/xml/ns/jaxb".equals(decl.getNamespaceURI())) {
      declareExtensionNamespace(target, decl.getNamespaceURI());
    }
    NodeList lst = decl.getChildNodes();
    for (int i = 0; i < lst.getLength(); i++)
    {
      Node n = lst.item(i);
      if ((n instanceof Element)) {
        declExtensionNamespace((Element)n, target);
      }
    }
  }
  
  private void declareExtensionNamespace(Element target, String nsUri)
  {
    Element root = target.getOwnerDocument().getDocumentElement();
    Attr att = root.getAttributeNodeNS("http://java.sun.com/xml/ns/jaxb", "extensionBindingPrefixes");
    if (att == null)
    {
      String jaxbPrefix = allocatePrefix(root, "http://java.sun.com/xml/ns/jaxb");
      
      att = target.getOwnerDocument().createAttributeNS("http://java.sun.com/xml/ns/jaxb", jaxbPrefix + ":" + "extensionBindingPrefixes");
      
      root.setAttributeNodeNS(att);
    }
    String prefix = allocatePrefix(root, nsUri);
    if (att.getValue().indexOf(prefix) == -1) {
      att.setValue(att.getValue() + " " + prefix);
    }
  }
  
  private String allocatePrefix(Element e, String nsUri)
  {
    NamedNodeMap atts = e.getAttributes();
    for (int i = 0; i < atts.getLength(); i++)
    {
      Attr a = (Attr)atts.item(i);
      if (("http://www.w3.org/2000/xmlns/".equals(a.getNamespaceURI())) && 
        (a.getName().indexOf(':') != -1)) {
        if (a.getValue().equals(nsUri)) {
          return a.getLocalName();
        }
      }
    }
    String prefix;
    do
    {
      prefix = "p" + (int)(Math.random() * 1000000.0D) + "_";
    } while (e.getAttributeNodeNS("http://www.w3.org/2000/xmlns/", prefix) != null);
    e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, nsUri);
    return prefix;
  }
  
  private void copyLocators(Element src, Element dst)
  {
    this.forest.locatorTable.storeStartLocation(dst, this.forest.locatorTable.getStartLocation(src));
    
    this.forest.locatorTable.storeEndLocation(dst, this.forest.locatorTable.getEndLocation(src));
    
    Element[] srcChilds = DOMUtils.getChildElements(src);
    Element[] dstChilds = DOMUtils.getChildElements(dst);
    for (int i = 0; i < srcChilds.length; i++) {
      copyLocators(srcChilds[i], dstChilds[i]);
    }
  }
  
  private void reportError(Element errorSource, String formattedMsg)
    throws SAXException
  {
    reportError(errorSource, formattedMsg, null);
  }
  
  private void reportError(Element errorSource, String formattedMsg, Exception nestedException)
    throws SAXException
  {
    SAXParseException e = new SAXParseException(formattedMsg, this.forest.locatorTable.getStartLocation(errorSource), nestedException);
    
    this.errorHandler.error(e);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\internalizer\Internalizer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */