package com.sun.tools.xjc.reader.internalizer;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

final class NamespaceContextImpl
  implements NamespaceContext
{
  private final Element e;
  
  public NamespaceContextImpl(Element e)
  {
    this.e = e;
  }
  
  public String getNamespaceURI(String prefix)
  {
    Node parent = this.e;
    String namespace = null;
    String prefixColon = prefix + ':';
    if (prefix.equals("xml"))
    {
      namespace = "http://www.w3.org/XML/1998/namespace";
    }
    else
    {
      int type;
      while ((null != parent) && (null == namespace) && (((type = parent.getNodeType()) == 1) || (type == 5)))
      {
        if (type == 1)
        {
          if (parent.getNodeName().startsWith(prefixColon)) {
            return parent.getNamespaceURI();
          }
          NamedNodeMap nnm = parent.getAttributes();
          for (int i = 0; i < nnm.getLength(); i++)
          {
            Node attr = nnm.item(i);
            String aname = attr.getNodeName();
            boolean isPrefix = aname.startsWith("xmlns:");
            if ((isPrefix) || (aname.equals("xmlns")))
            {
              int index = aname.indexOf(':');
              String p = isPrefix ? aname.substring(index + 1) : "";
              if (p.equals(prefix))
              {
                namespace = attr.getNodeValue();
                
                break;
              }
            }
          }
        }
        parent = parent.getParentNode();
      }
    }
    if (prefix.equals("")) {
      return "";
    }
    return namespace;
  }
  
  public String getPrefix(String namespaceURI)
  {
    throw new UnsupportedOperationException();
  }
  
  public Iterator getPrefixes(String namespaceURI)
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\internalizer\NamespaceContextImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */