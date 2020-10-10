package com.sun.tools.xjc.reader.gbind;

public abstract class Expression
{
  public static final Expression EPSILON = new Expression()
  {
    ElementSet lastSet()
    {
      return ElementSet.EMPTY_SET;
    }
    
    boolean isNullable()
    {
      return true;
    }
    
    void buildDAG(ElementSet incoming) {}
    
    public String toString()
    {
      return "-";
    }
  };
  
  abstract ElementSet lastSet();
  
  abstract boolean isNullable();
  
  abstract void buildDAG(ElementSet paramElementSet);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\gbind\Expression.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */