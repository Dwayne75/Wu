package com.sun.tools.xjc.reader.gbind;

public final class Sequence
  extends Expression
{
  private final Expression lhs;
  private final Expression rhs;
  private final boolean isNullable;
  private ElementSet lastSet;
  
  public Sequence(Expression lhs, Expression rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
    this.isNullable = ((lhs.isNullable()) && (rhs.isNullable()));
  }
  
  ElementSet lastSet()
  {
    if (this.lastSet == null) {
      if (this.rhs.isNullable()) {
        this.lastSet = ElementSets.union(this.lhs.lastSet(), this.rhs.lastSet());
      } else {
        this.lastSet = this.rhs.lastSet();
      }
    }
    return this.lastSet;
  }
  
  boolean isNullable()
  {
    return this.isNullable;
  }
  
  void buildDAG(ElementSet incoming)
  {
    this.lhs.buildDAG(incoming);
    if (this.lhs.isNullable()) {
      this.rhs.buildDAG(ElementSets.union(incoming, this.lhs.lastSet()));
    } else {
      this.rhs.buildDAG(this.lhs.lastSet());
    }
  }
  
  public String toString()
  {
    return '(' + this.lhs.toString() + ',' + this.rhs.toString() + ')';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\gbind\Sequence.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */