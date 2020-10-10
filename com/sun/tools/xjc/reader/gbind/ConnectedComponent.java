package com.sun.tools.xjc.reader.gbind;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ConnectedComponent
  implements Iterable<Element>
{
  private final List<Element> elements = new ArrayList();
  boolean isRequired;
  
  public final boolean isCollection()
  {
    assert (!this.elements.isEmpty());
    if (this.elements.size() > 1) {
      return true;
    }
    Element n = (Element)this.elements.get(0);
    return n.hasSelfLoop();
  }
  
  public final boolean isRequired()
  {
    return this.isRequired;
  }
  
  void add(Element e)
  {
    assert (!this.elements.contains(e));
    this.elements.add(e);
  }
  
  public Iterator<Element> iterator()
  {
    return this.elements.iterator();
  }
  
  public String toString()
  {
    String s = this.elements.toString();
    if (isRequired()) {
      s = s + '!';
    }
    if (isCollection()) {
      s = s + '*';
    }
    return s;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\gbind\ConnectedComponent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */