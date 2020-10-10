package com.sun.tools.xjc.model;

public final class Multiplicity
{
  public final int min;
  public final Integer max;
  
  public static Multiplicity create(int min, Integer max)
  {
    if ((min == 0) && (max == null)) {
      return STAR;
    }
    if ((min == 1) && (max == null)) {
      return PLUS;
    }
    if (max != null)
    {
      if ((min == 0) && (max.intValue() == 0)) {
        return ZERO;
      }
      if ((min == 0) && (max.intValue() == 1)) {
        return OPTIONAL;
      }
      if ((min == 1) && (max.intValue() == 1)) {
        return ONE;
      }
    }
    return new Multiplicity(min, max);
  }
  
  private Multiplicity(int min, Integer max)
  {
    this.min = min;this.max = max;
  }
  
  public boolean equals(Object o)
  {
    if (!(o instanceof Multiplicity)) {
      return false;
    }
    Multiplicity that = (Multiplicity)o;
    if (this.min != that.min) {
      return false;
    }
    if (this.max != null ? !this.max.equals(that.max) : that.max != null) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.min;
    result = 29 * result + (this.max != null ? this.max.hashCode() : 0);
    return result;
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
    return "(" + this.min + ',' + getMaxString() + ')';
  }
  
  public static final Multiplicity ZERO = new Multiplicity(0, Integer.valueOf(0));
  public static final Multiplicity ONE = new Multiplicity(1, Integer.valueOf(1));
  public static final Multiplicity OPTIONAL = new Multiplicity(0, Integer.valueOf(1));
  public static final Multiplicity STAR = new Multiplicity(0, null);
  public static final Multiplicity PLUS = new Multiplicity(1, null);
  
  public static Multiplicity choice(Multiplicity lhs, Multiplicity rhs)
  {
    return create(Math.min(lhs.min, rhs.min), (lhs.max == null) || (rhs.max == null) ? null : Integer.valueOf(Math.max(lhs.max.intValue(), rhs.max.intValue())));
  }
  
  public static Multiplicity group(Multiplicity lhs, Multiplicity rhs)
  {
    return create(lhs.min + rhs.min, (lhs.max == null) || (rhs.max == null) ? null : Integer.valueOf(lhs.max.intValue() + rhs.max.intValue()));
  }
  
  public static Multiplicity multiply(Multiplicity lhs, Multiplicity rhs)
  {
    int min = lhs.min * rhs.min;
    Integer max;
    Integer max;
    if ((isZero(lhs.max)) || (isZero(rhs.max)))
    {
      max = Integer.valueOf(0);
    }
    else
    {
      Integer max;
      if ((lhs.max == null) || (rhs.max == null)) {
        max = null;
      } else {
        max = Integer.valueOf(lhs.max.intValue() * rhs.max.intValue());
      }
    }
    return create(min, max);
  }
  
  private static boolean isZero(Integer i)
  {
    return (i != null) && (i.intValue() == 0);
  }
  
  public static Multiplicity oneOrMore(Multiplicity c)
  {
    if (c.max == null) {
      return c;
    }
    if (c.max.intValue() == 0) {
      return c;
    }
    return create(c.min, null);
  }
  
  public Multiplicity makeOptional()
  {
    if (this.min == 0) {
      return this;
    }
    return create(0, this.max);
  }
  
  public Multiplicity makeRepeated()
  {
    if ((this.max == null) || (this.max.intValue() == 0)) {
      return this;
    }
    return create(this.min, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\Multiplicity.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */