package com.winterwell.jgeoplanet;

public enum LengthUnit
{
  METRE(1.0D),  KILOMETRE(1000.0D),  MILE(1609.344D);
  
  public final double metres;
  public final Dx dx;
  
  private LengthUnit(double metres)
  {
    this.metres = metres;
    this.dx = new Dx(1.0D, this);
  }
  
  public Dx getDx()
  {
    return this.dx;
  }
  
  public double getMetres()
  {
    return this.metres;
  }
  
  @Deprecated
  public double convert(double amount, LengthUnit otherUnit)
  {
    Dx Dx2 = new Dx(amount, otherUnit);
    return Dx2.convertTo(this).getValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\winterwell\jgeoplanet\LengthUnit.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */