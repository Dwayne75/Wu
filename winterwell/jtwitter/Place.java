package winterwell.jtwitter;

import com.winterwell.jgeoplanet.BoundingBox;
import com.winterwell.jgeoplanet.IPlace;
import com.winterwell.jgeoplanet.Location;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import winterwell.json.JSONArray;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public class Place
  implements IPlace, Serializable
{
  private static final long serialVersionUID = 1L;
  private BoundingBox boundingBox;
  private String country;
  private String countryCode;
  private List<Location> geometry;
  private String id;
  private String name;
  private String type;
  private Place parent;
  
  public IPlace getParent()
  {
    return this.parent;
  }
  
  public Place(JSONObject _place)
    throws JSONException
  {
    this.id = InternalUtils.jsonGet("id", _place);
    if (this.id == null) {
      this.id = InternalUtils.jsonGet("woeid", _place);
    }
    this.type = InternalUtils.jsonGet("place_type", _place);
    
    this.name = InternalUtils.jsonGet("full_name", _place);
    if (this.name == null) {
      this.name = InternalUtils.jsonGet("name", _place);
    }
    this.countryCode = InternalUtils.jsonGet("country_code", _place);
    this.country = InternalUtils.jsonGet("country", _place);
    Object _parent = _place.opt("contained_within");
    if ((_parent instanceof JSONArray))
    {
      JSONArray pa = (JSONArray)_parent;
      _parent = pa.length() == 0 ? null : pa.get(0);
    }
    if (_parent != null) {
      this.parent = new Place((JSONObject)_parent);
    }
    Object bbox = _place.opt("bounding_box");
    if ((bbox instanceof JSONObject))
    {
      List<Location> bb = parseCoords((JSONObject)bbox);
      double n = -90.0D;double e = -180.0D;double s = 90.0D;double w = 180.0D;
      for (Location ll : bb)
      {
        n = Math.max(ll.latitude, n);
        s = Math.min(ll.latitude, s);
        e = Math.max(ll.longitude, e);
        w = Math.min(ll.longitude, w);
      }
      this.boundingBox = new BoundingBox(new Location(n, e), new Location(s, w));
    }
    Object geo = _place.opt("geometry");
    if ((geo instanceof JSONObject)) {
      this.geometry = parseCoords((JSONObject)geo);
    }
  }
  
  public BoundingBox getBoundingBox()
  {
    return this.boundingBox;
  }
  
  public String getCountryCode()
  {
    return this.countryCode;
  }
  
  public String getCountryName()
  {
    return this.country;
  }
  
  public List<Location> getGeometry()
  {
    return this.geometry;
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public String getInfoUrl()
  {
    return "http://api.twitter.com/1/geo/id/" + this.id + ".json";
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String getType()
  {
    return this.type;
  }
  
  private List<Location> parseCoords(JSONObject bbox)
    throws JSONException
  {
    JSONArray coords = bbox.getJSONArray("coordinates");
    
    coords = coords.getJSONArray(0);
    List<Location> coordinates = new ArrayList();
    int i = 0;
    for (int n = coords.length(); i < n; i++)
    {
      JSONArray pt = coords.getJSONArray(i);
      Location x = new Location(pt.getDouble(1), pt.getDouble(0));
      coordinates.add(x);
    }
    return coordinates;
  }
  
  public String toString()
  {
    return getName();
  }
  
  public Location getCentroid()
  {
    if (this.boundingBox == null) {
      return null;
    }
    return this.boundingBox.getCenter();
  }
  
  public String getUID()
  {
    return this.id + "@twitter";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\Place.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */