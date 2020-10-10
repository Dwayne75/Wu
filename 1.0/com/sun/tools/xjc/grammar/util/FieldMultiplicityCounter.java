package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;

public class FieldMultiplicityCounter
  extends MultiplicityCounter
{
  private final String name;
  
  private FieldMultiplicityCounter(String _name)
  {
    this.name = _name;
  }
  
  public static Multiplicity count(Expression exp, FieldItem fi)
  {
    return (Multiplicity)exp.visit(new FieldMultiplicityCounter(fi.name));
  }
  
  public static Multiplicity count(Expression exp, FieldUse fu)
  {
    return (Multiplicity)exp.visit(new FieldMultiplicityCounter(fu.name));
  }
  
  protected Multiplicity isChild(Expression exp)
  {
    if ((exp instanceof FieldItem))
    {
      FieldItem fi = (FieldItem)exp;
      if (fi.name.equals(this.name)) {
        return fi.multiplicity;
      }
      return Multiplicity.zero;
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\FieldMultiplicityCounter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */