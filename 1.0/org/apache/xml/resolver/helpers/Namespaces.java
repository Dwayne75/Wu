package org.apache.xml.resolver.helpers;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Namespaces
{
  public static String getPrefix(Element paramElement)
  {
    String str1 = paramElement.getTagName();
    String str2 = "";
    if (str1.indexOf(':') > 0) {
      str2 = str1.substring(0, str1.indexOf(':'));
    }
    return str2;
  }
  
  public static String getLocalName(Element paramElement)
  {
    String str = paramElement.getTagName();
    if (str.indexOf(':') > 0) {
      str = str.substring(str.indexOf(':') + 1);
    }
    return str;
  }
  
  public static String getNamespaceURI(Node paramNode, String paramString)
  {
    if ((paramNode == null) || (paramNode.getNodeType() != 1)) {
      return null;
    }
    if (paramString.equals(""))
    {
      if (((Element)paramNode).hasAttribute("xmlns")) {
        return ((Element)paramNode).getAttribute("xmlns");
      }
    }
    else
    {
      String str = "xmlns:" + paramString;
      if (((Element)paramNode).hasAttribute(str)) {
        return ((Element)paramNode).getAttribute(str);
      }
    }
    return getNamespaceURI(paramNode.getParentNode(), paramString);
  }
  
  public static String getNamespaceURI(Element paramElement)
  {
    String str = getPrefix(paramElement);
    return getNamespaceURI(paramElement, str);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\helpers\Namespaces.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */