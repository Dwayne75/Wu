package com.winterwell.jgeoplanet;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Location
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private static final double DIAMETER_OF_EARTH = 1.27562E7D;
  public final double longitude;
  public final double latitude;
  
  public Location(double latitude, double longitude)
    throws IllegalArgumentException
  {
    if ((latitude < -90.0D) || (latitude > 90.0D)) {
      throw new IllegalArgumentException("Invalid latitude: " + latitude + ", " + longitude);
    }
    if ((longitude < -180.0D) || (longitude > 180.0D))
    {
      longitude %= 360.0D;
      if (longitude > 180.0D) {
        longitude = 360.0D - longitude;
      } else if (longitude < -180.0D) {
        longitude += 360.0D;
      }
      assert ((longitude >= -180.0D) || (longitude <= 180.0D)) : longitude;
    }
    this.latitude = latitude;
    this.longitude = longitude;
  }
  
  public double getLatitude()
  {
    return this.latitude;
  }
  
  public double[] getLatLong()
  {
    return new double[] { this.latitude, this.longitude };
  }
  
  public double getLongitude()
  {
    return this.longitude;
  }
  
  public Dx distance(Location other)
  {
    double lat = this.latitude * 3.141592653589793D / 180.0D;
    double lon = this.longitude * 3.141592653589793D / 180.0D;
    double olat = other.latitude * 3.141592653589793D / 180.0D;
    double olon = other.longitude * 3.141592653589793D / 180.0D;
    
    double sin2lat = Math.sin((lat - olat) / 2.0D);
    sin2lat *= sin2lat;
    double sin2long = Math.sin((lon - olon) / 2.0D);
    sin2long *= sin2long;
    double m = 1.27562E7D * Math.asin(
      Math.sqrt(sin2lat + Math.cos(lat) * Math.cos(olat) * sin2long));
    return new Dx(m, LengthUnit.METRE);
  }
  
  public Location move(double metresNorth, double metresEast)
  {
    double fracNorth = metresNorth / 2.003739210386106E10D;
    double lat = this.latitude + fracNorth * 180.0D;
    if (lat > 90.0D) {
      lat = 90.0D;
    } else if (lat < -90.0D) {
      lat = -90.0D;
    }
    double lng = this.longitude + metresEast;
    while (lng > 180.0D) {
      lng -= 360.0D;
    }
    while (lng < -180.0D) {
      lng += 360.0D;
    }
    return new Location(lat, lng);
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    
    long temp = Double.doubleToLongBits(this.latitude);
    result = 31 * result + (int)(temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(this.longitude);
    result = 31 * result + (int)(temp ^ temp >>> 32);
    return result;
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Location other = (Location)obj;
    if (Double.doubleToLongBits(this.latitude) != 
      Double.doubleToLongBits(other.latitude)) {
      return false;
    }
    if (Double.doubleToLongBits(this.longitude) != 
      Double.doubleToLongBits(other.longitude)) {
      return false;
    }
    return true;
  }
  
  public String toString()
  {
    return "(" + this.latitude + " N, " + this.longitude + " E)";
  }
  
  public String toSimpleCoords()
  {
    return this.latitude + "," + this.longitude;
  }
  
  public static final Pattern latLongLocn = Pattern.compile(
    "\\s*(-?[\\d\\.]+),\\s*(-?[\\d\\.]+)\\s*");
  
  public static Location parse(String locnDesc)
  {
    Matcher m = latLongLocn.matcher(locnDesc);
    if (!m.matches()) {
      return null;
    }
    String lat = m.group(1);
    String lng = m.group(2);
    double _lat = Double.valueOf(lat).doubleValue();
    if (Math.abs(_lat) > 90.0D) {
      return null;
    }
    return new Location(_lat, Double.valueOf(lng).doubleValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\winterwell\jgeoplanet\Location.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */