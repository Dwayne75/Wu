package com.sun.tools.xjc.reader.internalizer;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.istack.SAXParseException2;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.util.DOMUtils;
import com.sun.xml.bind.v2.util.EditDistance;
import com.sun.xml.xsom.SCD;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

class Internalizer
{
  private static final XPathFactory xpf = ;
  private final XPath xpath = xpf.newXPath();
  private final DOMForest forest;
  private ErrorReceiver errorHandler;
  private boolean enableSCD;
  private static final String EXTENSION_PREFIXES = "extensionBindingPrefixes";
  
  static SCDBasedBindingSet transform(DOMForest forest, boolean enableSCD)
  {
    return new Internalizer(forest, enableSCD).transform();
  }
  
  private Internalizer(DOMForest forest, boolean enableSCD)
  {
    this.errorHandler = forest.getErrorHandler();
    this.forest = forest;
    this.enableSCD = enableSCD;
  }
  
  private SCDBasedBindingSet transform()
  {
    Map<Element, Node> targetNodes = new HashMap();
    
    SCDBasedBindingSet scd = new SCDBasedBindingSet(this.forest);
    for (Element jaxbBindings : this.forest.outerMostBindings) {
      buildTargetNodeMap(jaxbBindings, jaxbBindings, null, targetNodes, scd);
    }
    for (Element jaxbBindings : this.forest.outerMostBindings) {
      move(jaxbBindings, targetNodes);
    }
    return scd;
  }
  
  private void validate(Element bindings)
  {
    NamedNodeMap atts = bindings.getAttributes();
    for (int i = 0; i < atts.getLength(); i++)
    {
      Attr a = (Attr)atts.item(i);
      if (a.getNamespaceURI() == null) {
        if (!a.getLocalName().equals("node")) {
          if (!a.getLocalName().equals("schemaLocation")) {
            if (a.getLocalName().equals("scd")) {}
          }
        }
      }
    }
  }
  
  private void buildTargetNodeMap(Element bindings, @NotNull Node inheritedTarget, @Nullable SCDBasedBindingSet.Target inheritedSCD, Map<Element, Node> result, SCDBasedBindingSet scdResult)
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
        reportError(bindings, Messages.format("Internalizer.IncorrectSchemaReference", new Object[] { schemaLocation, EditDistance.findNearest(schemaLocation, this.forest.listSystemIDs()) }));
        
        return;
      }
      target = ((Document)target).getDocumentElement();
    }
    if (bindings.getAttributeNode("node") != null)
    {
      String nodeXPath = bindings.getAttribute("node");
      NodeList nlst;
      try
      {
        this.xpath.setNamespaceContext(new NamespaceContextImpl(bindings));
        nlst = (NodeList)this.xpath.evaluate(nodeXPath, target, XPathConstants.NODESET);
      }
      catch (XPathExpressionException e)
      {
        reportError(bindings, Messages.format("Internalizer.XPathEvaluationError", new Object[] { e.getMessage() }), e);
        
        return;
      }
      if (nlst.getLength() == 0)
      {
        reportError(bindings, Messages.format("Internalizer.XPathEvaluatesToNoTarget", new Object[] { nodeXPath }));
        
        return;
      }
      if (nlst.getLength() != 1)
      {
        reportError(bindings, Messages.format("Internalizer.XPathEvaulatesToTooManyTargets", new Object[] { nodeXPath, Integer.valueOf(nlst.getLength()) }));
        
        return;
      }
      Node rnode = nlst.item(0);
      if (!(rnode instanceof Element))
      {
        reportError(bindings, Messages.format("Internalizer.XPathEvaluatesToNonElement", new Object[] { nodeXPath }));
        
        return;
      }
      if (!this.forest.logic.checkIfValidTargetNode(this.forest, bindings, (Element)rnode))
      {
        reportError(bindings, Messages.format("Internalizer.XPathEvaluatesToNonSchemaElement", new Object[] { nodeXPath, rnode.getNodeName() }));
        
        return;
      }
      target = rnode;
    }
    if (bindings.getAttributeNode("scd") != null)
    {
      String scdPath = bindings.getAttribute("scd");
      if (!this.enableSCD)
      {
        reportError(bindings, Messages.format("SCD_NOT_ENABLED", new Object[0]));
        
        this.enableSCD = true;
      }
      try
      {
        inheritedSCD = scdResult.createNewTarget(inheritedSCD, bindings, SCD.create(scdPath, new NamespaceContextImpl(bindings)));
      }
      catch (ParseException e)
      {
        reportError(bindings, Messages.format("ERR_SCD_EVAL", new Object[] { e.getMessage() }), e);
        return;
      }
    }
    if (inheritedSCD != null) {
      inheritedSCD.addBinidng(bindings);
    } else {
      result.put(bindings, target);
    }
    Element[] children = DOMUtils.getChildElements(bindings, "http://java.sun.com/xml/ns/jaxb", "bindings");
    for (Element value : children) {
      buildTargetNodeMap(value, target, inheritedSCD, result, scdResult);
    }
  }
  
  private void move(Element bindings, Map<Element, Node> targetNodes)
  {
    Node target = (Node)targetNodes.get(bindings);
    if (target == null) {
      return;
    }
    for (Element item : DOMUtils.getChildElements(bindings))
    {
      String localName = item.getLocalName();
      if ("bindings".equals(localName))
      {
        move(item, targetNodes);
      }
      else if ("globalBindings".equals(localName))
      {
        moveUnder(item, this.forest.getOneDocument().getDocumentElement());
      }
      else
      {
        if (!(target instanceof Element))
        {
          reportError(item, Messages.format("Internalizer.ContextNodeIsNotElement", new Object[0]));
          
          return;
        }
        if (!this.forest.logic.checkIfValidTargetNode(this.forest, item, (Element)target))
        {
          reportError(item, Messages.format("Internalizer.OrphanedCustomization", new Object[] { item.getNodeName() }));
          
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
    Set<String> inscopes = new HashSet();
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
      
      att = target.getOwnerDocument().createAttributeNS("http://java.sun.com/xml/ns/jaxb", jaxbPrefix + ':' + "extensionBindingPrefixes");
      
      root.setAttributeNodeNS(att);
    }
    String prefix = allocatePrefix(root, nsUri);
    if (att.getValue().indexOf(prefix) == -1) {
      att.setValue(att.getValue() + ' ' + prefix);
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
      prefix = "p" + (int)(Math.random() * 1000000.0D) + '_';
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
  {
    reportError(errorSource, formattedMsg, null);
  }
  
  private void reportError(Element errorSource, String formattedMsg, Exception nestedException)
  {
    SAXParseException e = new SAXParseException2(formattedMsg, this.forest.locatorTable.getStartLocation(errorSource), nestedException);
    
    this.errorHandler.error(e);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\internalizer\Internalizer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */