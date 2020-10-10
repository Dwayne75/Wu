package com.winterwell.jgeoplanet;

public abstract interface IPlace
{
  public static final String TYPE_CITY = "city";
  public static final String TYPE_COUNTRY = "country";
  
  public abstract String getName();
  
  public abstract String getCountryName();
  
  public abstract IPlace getParent();
  
  public abstract Location getCentroid();
  
  public abstract BoundingBox getBoundingBox();
  
  public abstract String getType();
  
  public abstract String getUID();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\winterwell\jgeoplanet\IPlace.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */