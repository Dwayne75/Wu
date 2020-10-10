package winterwell.jtwitter;

import com.winterwell.jgeoplanet.IGeoCode;
import com.winterwell.jgeoplanet.IPlace;
import com.winterwell.jgeoplanet.MFloat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import winterwell.json.JSONArray;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public class Twitter_Geo
  implements IGeoCode
{
  private double accuracy;
  private final Twitter jtwit;
  
  Twitter_Geo(Twitter jtwit)
  {
    assert (jtwit != null);
    this.jtwit = jtwit;
  }
  
  public List geoSearch(double latitude, double longitude)
  {
    throw new RuntimeException();
  }
  
  public List<Place> geoSearch(String query)
  {
    String url = this.jtwit.TWITTER_URL + "/geo/search.json";
    Map vars = InternalUtils.asMap(new Object[] { "query", query });
    if (this.accuracy != 0.0D) {
      vars.put("accuracy", String.valueOf(this.accuracy));
    }
    boolean auth = InternalUtils.authoriseIn11(this.jtwit);
    String json = this.jtwit.getHttpClient().getPage(url, vars, auth);
    try
    {
      JSONObject jo = new JSONObject(json);
      JSONObject jo2 = jo.getJSONObject("result");
      JSONArray arr = jo2.getJSONArray("places");
      List places = new ArrayList(arr.length());
      for (int i = 0; i < arr.length(); i++)
      {
        JSONObject _place = arr.getJSONObject(i);
        
        Place place = new Place(_place);
        places.add(place);
      }
      return places;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  public List geoSearchByIP(String ipAddress)
  {
    throw new RuntimeException();
  }
  
  public List<Place> getTrendRegions()
  {
    String json = this.jtwit.getHttpClient().getPage(
      this.jtwit.TWITTER_URL + "/trends/available.json", null, false);
    try
    {
      JSONArray json2 = new JSONArray(json);
      List<Place> trends = new ArrayList();
      for (int i = 0; i < json2.length(); i++)
      {
        JSONObject ti = json2.getJSONObject(i);
        Place place = new Place(ti);
        trends.add(place);
      }
      return trends;
    }
    catch (JSONException e)
    {
      throw new TwitterException.Parsing(json, e);
    }
  }
  
  public void setAccuracy(double metres)
  {
    this.accuracy = metres;
  }
  
  public IPlace getPlace(String locationDescription, MFloat confidence)
  {
    List<Place> places = geoSearch(locationDescription);
    if (places.size() == 0) {
      return null;
    }
    if (places.size() == 1)
    {
      if (confidence != null) {
        confidence.value = 0.8F;
      }
      return (IPlace)places.get(0);
    }
    return InternalUtils.prefer(places, "city", confidence, 0.8F);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\Twitter_Geo.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */