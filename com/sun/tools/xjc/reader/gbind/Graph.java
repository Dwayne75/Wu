package com.sun.tools.xjc.reader.gbind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class Graph
  implements Iterable<ConnectedComponent>
{
  private final Element source = new SourceNode();
  private final Element sink = new SinkNode();
  private final List<ConnectedComponent> ccs = new ArrayList();
  
  public Graph(Expression body)
  {
    Expression whole = new Sequence(new Sequence(this.source, body), this.sink);
    
    whole.buildDAG(ElementSet.EMPTY_SET);
    
    this.source.assignDfsPostOrder(this.sink);
    this.source.buildStronglyConnectedComponents(this.ccs);
    
    Set<Element> visited = new HashSet();
    for (ConnectedComponent cc : this.ccs)
    {
      visited.clear();
      if (this.source.checkCutSet(cc, visited)) {
        cc.isRequired = true;
      }
    }
  }
  
  public Iterator<ConnectedComponent> iterator()
  {
    return this.ccs.iterator();
  }
  
  public String toString()
  {
    return this.ccs.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\gbind\Graph.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */