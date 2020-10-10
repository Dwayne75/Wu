package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.Expression;

public final class Multiplicity
{
  public final int min;
  public final Integer max;
  
  public Multiplicity(int min, Integer max)
  {
    this.min = min;this.max = max;
  }
  
  public Multiplicity(int min, int max)
  {
    this.min = min;this.max = new Integer(max);
  }
  
  public boolean isUnique()
  {
    if (this.max == null) {
      return false;
    }
    return (this.min == 1) && (this.max.intValue() == 1);
  }
  
  public boolean isOptional()
  {
    if (this.max == null) {
      return false;
    }
    return (this.min == 0) && (this.max.intValue() == 1);
  }
  
  public boolean isAtMostOnce()
  {
    if (this.max == null) {
      return false;
    }
    return this.max.intValue() <= 1;
  }
  
  public boolean isZero()
  {
    if (this.max == null) {
      return false;
    }
    return this.max.intValue() == 0;
  }
  
  public boolean includes(Multiplicity rhs)
  {
    if (rhs.min < this.min) {
      return false;
    }
    if (this.max == null) {
      return true;
    }
    if (rhs.max == null) {
      return false;
    }
    return rhs.max.intValue() <= this.max.intValue();
  }
  
  public String getMaxString()
  {
    if (this.max == null) {
      return "unbounded";
    }
    return this.max.toString();
  }
  
  public String toString()
  {
    return "(" + this.min + "," + getMaxString() + ")";
  }
  
  public static final Multiplicity zero = new Multiplicity(0, 0);
  public static final Multiplicity one = new Multiplicity(1, 1);
  public static final Multiplicity star = new Multiplicity(0, null);
  
  public static Multiplicity calc(Expression exp, MultiplicityCounter calc)
  {
    return (Multiplicity)exp.visit(calc);
  }
  
  public static Multiplicity choice(Multiplicity lhs, Multiplicity rhs)
  {
    return new Multiplicity(Math.min(lhs.min, rhs.min), (lhs.max == null) || (rhs.max == null) ? null : new Integer(Math.max(lhs.max.intValue(), rhs.max.intValue())));
  }
  
  public static Multiplicity group(Multiplicity lhs, Multiplicity rhs)
  {
    return new Multiplicity(lhs.min + rhs.min, (lhs.max == null) || (rhs.max == null) ? null : new Integer(lhs.max.intValue() + rhs.max.intValue()));
  }
  
  public static Multiplicity oneOrMore(Multiplicity c)
  {
    if (c.max == null) {
      return c;
    }
    if (c.max.intValue() == 0) {
      return c;
    }
    return new Multiplicity(c.min, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\Multiplicity.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */