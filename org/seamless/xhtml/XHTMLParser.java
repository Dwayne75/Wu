package org.seamless.xhtml;

import java.util.HashSet;
import java.util.Set;
import javax.xml.xpath.XPath;
import org.seamless.xml.DOMParser;
import org.seamless.xml.DOMParser.NodeVisitor;
import org.seamless.xml.NamespaceContextMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XHTMLParser
  extends DOMParser<XHTML>
{
  public XHTMLParser()
  {
    super(XHTML.createSchemaSources());
  }
  
  protected XHTML createDOM(Document document)
  {
    return document != null ? new XHTML(document) : null;
  }
  
  public void checkDuplicateIdentifiers(XHTML document)
    throws IllegalStateException
  {
    final Set<String> identifiers = new HashSet();
    accept(document.getW3CDocument().getDocumentElement(), new DOMParser.NodeVisitor((short)1)
    {
      public void visit(Node node)
      {
        Element element = (Element)node;
        
        String id = element.getAttribute(XHTML.ATTR.id.name());
        if (!"".equals(id))
        {
          if (identifiers.contains(id)) {
            throw new IllegalStateException("Duplicate identifier, override/change value: " + id);
          }
          identifiers.add(id);
        }
      }
    });
  }
  
  public NamespaceContextMap createDefaultNamespaceContext(String... optionalPrefixes)
  {
    NamespaceContextMap ctx = new NamespaceContextMap()
    {
      protected String getDefaultNamespaceURI()
      {
        return "http://www.w3.org/1999/xhtml";
      }
    };
    for (String optionalPrefix : optionalPrefixes) {
      ctx.put(optionalPrefix, "http://www.w3.org/1999/xhtml");
    }
    return ctx;
  }
  
  public XPath createXPath()
  {
    return super.createXPath(createDefaultNamespaceContext(new String[] { "h" }));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xhtml\XHTMLParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */