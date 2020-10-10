package com.sun.tools.xjc.reader.gbind;

public final class OneOrMore
  extends Expression
{
  private final Expression child;
  
  public OneOrMore(Expression child)
  {
    this.child = child;
  }
  
  ElementSet lastSet()
  {
    return this.child.lastSet();
  }
  
  boolean isNullable()
  {
    return this.child.isNullable();
  }
  
  void buildDAG(ElementSet incoming)
  {
    this.child.buildDAG(ElementSets.union(incoming, this.child.lastSet()));
  }
  
  public String toString()
  {
    return this.child.toString() + '+';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\gbind\OneOrMore.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */