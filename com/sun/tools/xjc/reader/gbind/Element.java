package com.sun.tools.xjc.reader.gbind;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class Element
  extends Expression
  implements ElementSet
{
  final Set<Element> foreEdges = new LinkedHashSet();
  final Set<Element> backEdges = new LinkedHashSet();
  Element prevPostOrder;
  private ConnectedComponent cc;
  
  ElementSet lastSet()
  {
    return this;
  }
  
  boolean isNullable()
  {
    return false;
  }
  
  boolean isSource()
  {
    return false;
  }
  
  boolean isSink()
  {
    return false;
  }
  
  void buildDAG(ElementSet incoming)
  {
    incoming.addNext(this);
  }
  
  public void addNext(Element element)
  {
    this.foreEdges.add(element);
    element.backEdges.add(this);
  }
  
  public boolean contains(ElementSet rhs)
  {
    return (this == rhs) || (rhs == ElementSet.EMPTY_SET);
  }
  
  /**
   * @deprecated
   */
  public Iterator<Element> iterator()
  {
    return Collections.singleton(this).iterator();
  }
  
  Element assignDfsPostOrder(Element prev)
  {
    if (this.prevPostOrder != null) {
      return prev;
    }
    this.prevPostOrder = this;
    for (Element next : this.foreEdges) {
      prev = next.assignDfsPostOrder(prev);
    }
    this.prevPostOrder = prev;
    return this;
  }
  
  public void buildStronglyConnectedComponents(List<ConnectedComponent> ccs)
  {
    for (Element cur = this; cur != cur.prevPostOrder; cur = cur.prevPostOrder) {
      if (!cur.belongsToSCC())
      {
        ConnectedComponent cc = new ConnectedComponent();
        ccs.add(cc);
        
        cur.formConnectedComponent(cc);
      }
    }
  }
  
  private boolean belongsToSCC()
  {
    return (this.cc != null) || (isSource()) || (isSink());
  }
  
  private void formConnectedComponent(ConnectedComponent group)
  {
    if (belongsToSCC()) {
      return;
    }
    this.cc = group;
    group.add(this);
    for (Element prev : this.backEdges) {
      prev.formConnectedComponent(group);
    }
  }
  
  public boolean hasSelfLoop()
  {
    assert (this.foreEdges.contains(this) == this.backEdges.contains(this));
    
    return this.foreEdges.contains(this);
  }
  
  final boolean checkCutSet(ConnectedComponent cc, Set<Element> visited)
  {
    assert (belongsToSCC());
    if (isSink()) {
      return false;
    }
    if (!visited.add(this)) {
      return true;
    }
    if (this.cc == cc) {
      return true;
    }
    for (Element next : this.foreEdges) {
      if (!next.checkCutSet(cc, visited)) {
        return false;
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\gbind\Element.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */