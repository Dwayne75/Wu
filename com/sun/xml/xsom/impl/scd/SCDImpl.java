package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSComponent;
import java.util.Iterator;

public final class SCDImpl
  extends SCD
{
  private final Step[] steps;
  private final String text;
  
  public SCDImpl(String text, Step[] steps)
  {
    this.text = text;
    this.steps = steps;
  }
  
  public Iterator<XSComponent> select(Iterator<? extends XSComponent> contextNode)
  {
    Iterator<XSComponent> nodeSet = contextNode;
    
    int len = this.steps.length;
    for (int i = 0; i < len; i++)
    {
      if ((i != 0) && (i != len - 1) && (!this.steps[(i - 1)].axis.isModelGroup()) && (this.steps[i].axis.isModelGroup())) {
        nodeSet = new Iterators.Unique(new Iterators.Map(nodeSet)
        {
          protected Iterator<XSComponent> apply(XSComponent u)
          {
            return new Iterators.Union(Iterators.singleton(u), Axis.INTERMEDIATE_SKIP.iterator(u));
          }
        });
      }
      nodeSet = this.steps[i].evaluate(nodeSet);
    }
    return nodeSet;
  }
  
  public String toString()
  {
    return this.text;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\scd\SCDImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */