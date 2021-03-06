package com.sun.tools.xjc.reader.xmlschema.parser;

import com.sun.tools.xjc.reader.internalizer.AbstractReferenceFinderImpl;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.InternalizationLogic;
import com.sun.tools.xjc.util.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;

public class XMLSchemaInternalizationLogic
  implements InternalizationLogic
{
  private static final class ReferenceFinder
    extends AbstractReferenceFinderImpl
  {
    ReferenceFinder(DOMForest parent)
    {
      super();
    }
    
    protected String findExternalResource(String nsURI, String localName, Attributes atts)
    {
      if (("http://www.w3.org/2001/XMLSchema".equals(nsURI)) && (("import".equals(localName)) || ("include".equals(localName)))) {
        return atts.getValue("schemaLocation");
      }
      return null;
    }
  }
  
  public XMLFilterImpl createExternalReferenceFinder(DOMForest parent)
  {
    return new ReferenceFinder(parent);
  }
  
  public boolean checkIfValidTargetNode(DOMForest parent, Element bindings, Element target)
  {
    return "http://www.w3.org/2001/XMLSchema".equals(target.getNamespaceURI());
  }
  
  public Element refineTarget(Element target)
  {
    Element annotation = DOMUtils.getFirstChildElement(target, "http://www.w3.org/2001/XMLSchema", "annotation");
    if (annotation == null) {
      annotation = insertXMLSchemaElement(target, "annotation");
    }
    Element appinfo = DOMUtils.getFirstChildElement(annotation, "http://www.w3.org/2001/XMLSchema", "appinfo");
    if (appinfo == null) {
      appinfo = insertXMLSchemaElement(annotation, "appinfo");
    }
    return appinfo;
  }
  
  private Element insertXMLSchemaElement(Element parent, String localName)
  {
    String qname = parent.getTagName();
    int idx = qname.indexOf(':');
    if (idx == -1) {
      qname = localName;
    } else {
      qname = qname.substring(0, idx + 1) + localName;
    }
    Element child = parent.getOwnerDocument().createElementNS("http://www.w3.org/2001/XMLSchema", qname);
    
    NodeList children = parent.getChildNodes();
    if (children.getLength() == 0) {
      parent.appendChild(child);
    } else {
      parent.insertBefore(child, children.item(0));
    }
    return child;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\parser\XMLSchemaInternalizationLogic.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */