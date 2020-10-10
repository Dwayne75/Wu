package com.sun.tools.xjc.reader.gbind;

public final class Choice
  extends Expression
{
  private final Expression lhs;
  private final Expression rhs;
  private final boolean isNullable;
  
  public Choice(Expression lhs, Expression rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
    this.isNullable = ((lhs.isNullable()) || (rhs.isNullable()));
  }
  
  boolean isNullable()
  {
    return this.isNullable;
  }
  
  ElementSet lastSet()
  {
    return ElementSets.union(this.lhs.lastSet(), this.rhs.lastSet());
  }
  
  void buildDAG(ElementSet incoming)
  {
    this.lhs.buildDAG(incoming);
    this.rhs.buildDAG(incoming);
  }
  
  public String toString()
  {
    return '(' + this.lhs.toString() + '|' + this.rhs.toString() + ')';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\gbind\Choice.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */