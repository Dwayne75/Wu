package com.sun.tools.xjc.util;

import com.sun.xml.xsom.XmlString;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;

public final class NamespaceContextAdapter
  implements NamespaceContext
{
  private XmlString xstr;
  
  public NamespaceContextAdapter(XmlString xstr)
  {
    this.xstr = xstr;
  }
  
  public String getNamespaceURI(String prefix)
  {
    return this.xstr.resolvePrefix(prefix);
  }
  
  public String getPrefix(String namespaceURI)
  {
    return null;
  }
  
  public Iterator getPrefixes(String namespaceURI)
  {
    return Collections.EMPTY_LIST.iterator();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\util\NamespaceContextAdapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */