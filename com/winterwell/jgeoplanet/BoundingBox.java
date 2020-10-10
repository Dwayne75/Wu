package com.winterwell.jgeoplanet;

import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public class BoundingBox
{
  final Location northEast;
  final Location southWest;
  
  public BoundingBox(Location northEast, Location southWest)
  {
    if (northEast.latitude < southWest.latitude) {
      throw new IllegalArgumentException("North east corner is south of south west corner");
    }
    this.northEast = northEast;
    this.southWest = southWest;
  }
  
  public Location getCenter()
  {
    Location ne = this.northEast;
    Location sw = this.southWest;
    
    double tempLat = (ne.latitude + sw.latitude) / 2.0D;
    if (Math.abs(ne.latitude - sw.latitude) > 90.0D) {
      if (tempLat <= 0.0D) {
        tempLat += 90.0D;
      } else {
        tempLat -= 90.0D;
      }
    }
    double tempLong = (ne.longitude + sw.longitude) / 2.0D;
    if (Math.abs(ne.longitude - sw.longitude) > 180.0D) {
      if (tempLong <= 0.0D) {
        tempLong += 180.0D;
      } else {
        tempLong -= 180.0D;
      }
    }
    Location tempCentroid = new Location(tempLat, tempLong);
    return tempCentroid;
  }
  
  BoundingBox(JSONObject bbox)
    throws JSONException
  {
    this(getLocation(bbox.getJSONObject("northEast")), getLocation(bbox.getJSONObject("southWest")));
  }
  
  public BoundingBox(Location centre, Dx radius)
  {
    double r = radius.getMetres();
    this.northEast = centre.move(r, r);
    this.southWest = centre.move(-r, -r);
  }
  
  public Location getNorthEast()
  {
    return this.northEast;
  }
  
  public Location getSouthWest()
  {
    return this.southWest;
  }
  
  public Location getNorthWest()
  {
    return new Location(this.northEast.latitude, this.southWest.longitude);
  }
  
  public Location getSouthEast()
  {
    return new Location(this.southWest.latitude, this.northEast.longitude);
  }
  
  public boolean contains(Location location)
  {
    if (location.latitude > this.northEast.latitude) {
      return false;
    }
    if (location.latitude < this.southWest.latitude) {
      return false;
    }
    if ((this.northEast.longitude < 0.0D) && (this.southWest.longitude >= 0.0D) && (this.southWest.longitude > this.northEast.longitude))
    {
      if ((location.longitude < 0.0D) && (location.longitude > this.northEast.longitude)) {
        return false;
      }
      if ((location.longitude >= 0.0D) && (location.longitude < this.southWest.longitude)) {
        return false;
      }
    }
    else
    {
      if (location.longitude > this.northEast.longitude) {
        return false;
      }
      if (location.longitude < this.southWest.longitude) {
        return false;
      }
    }
    return true;
  }
  
  public boolean contains(BoundingBox other)
  {
    return (contains(other.southWest)) && (contains(other.northEast));
  }
  
  public boolean intersects(BoundingBox other)
  {
    return (contains(other.northEast)) || 
      (contains(other.southWest)) || 
      (contains(other.getNorthWest())) || 
      (contains(other.getSouthEast()));
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (
      this.northEast == null ? 0 : this.northEast.hashCode());
    result = 31 * result + (
      this.southWest == null ? 0 : this.southWest.hashCode());
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
    BoundingBox other = (BoundingBox)obj;
    if (this.northEast == null)
    {
      if (other.northEast != null) {
        return false;
      }
    }
    else if (!this.northEast.equals(other.northEast)) {
      return false;
    }
    if (this.southWest == null)
    {
      if (other.southWest != null) {
        return false;
      }
    }
    else if (!this.southWest.equals(other.southWest)) {
      return false;
    }
    return true;
  }
  
  public String toString()
  {
    return 
      "BoundingBox [northEast=" + this.northEast + ", southWest=" + this.southWest + "]";
  }
  
  static Location getLocation(JSONObject jo)
    throws JSONException
  {
    return new Location(
      jo.getDouble("latitude"), jo.getDouble("longitude"));
  }
  
  public boolean isPoint()
  {
    return this.northEast.equals(this.southWest);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\winterwell\jgeoplanet\BoundingBox.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */