package com.winterwell.jgeoplanet;

public final class Dx
  implements Comparable<Dx>
{
  private static final long serialVersionUID = 1L;
  private final LengthUnit unit;
  private final double n;
  
  public static Dx ZERO()
  {
    return new Dx(0.0D, LengthUnit.METRE);
  }
  
  public Dx(double metres)
  {
    this(metres, LengthUnit.METRE);
  }
  
  public Dx(double n, LengthUnit unit)
  {
    this.n = n;
    this.unit = unit;
    assert (unit != null);
  }
  
  public double getMetres()
  {
    return this.unit.metres * this.n;
  }
  
  public double getValue()
  {
    return this.n;
  }
  
  public LengthUnit geKLength()
  {
    return this.unit;
  }
  
  public String toString()
  {
    return (float)this.n + " " + this.unit.toString().toLowerCase() + (this.n != 1.0D ? "s" : "");
  }
  
  public boolean isShorterThan(Dx Dx2)
  {
    assert (Dx2 != null);
    return Math.abs(getMetres()) < Math.abs(Dx2.getMetres());
  }
  
  public boolean equals(Object obj)
  {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != Dx.class) {
      return false;
    }
    Dx Dx = (Dx)obj;
    return getMetres() == Dx.getMetres();
  }
  
  public int hashCode()
  {
    return new Double(getMetres()).hashCode();
  }
  
  public Dx multiply(double x)
  {
    return new Dx(x * this.n, this.unit);
  }
  
  public int compareTo(Dx Dx2)
  {
    double ms = getMetres();
    double ms2 = Dx2.getMetres();
    if (ms == ms2) {
      return 0;
    }
    return ms < ms2 ? -1 : 1;
  }
  
  public Dx convertTo(LengthUnit unit2)
  {
    if (this.unit == unit2) {
      return this;
    }
    double n2 = divide(unit2.dx);
    return new Dx(n2, unit2);
  }
  
  public double divide(Dx other)
  {
    if (this.n == 0.0D) {
      return 0.0D;
    }
    return this.n * this.unit.metres / (other.n * other.unit.metres);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\winterwell\jgeoplanet\Dx.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */