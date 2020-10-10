package org.seamless.xml;

import java.util.HashMap;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;

public abstract class NamespaceContextMap
  extends HashMap<String, String>
  implements NamespaceContext
{
  public String getNamespaceURI(String prefix)
  {
    if (prefix == null) {
      throw new IllegalArgumentException("No prefix provided!");
    }
    if (prefix.equals("")) {
      return getDefaultNamespaceURI();
    }
    if (get(prefix) != null) {
      return (String)get(prefix);
    }
    return "";
  }
  
  public String getPrefix(String namespaceURI)
  {
    return null;
  }
  
  public Iterator getPrefixes(String s)
  {
    return null;
  }
  
  protected abstract String getDefaultNamespaceURI();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xml\NamespaceContextMap.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */