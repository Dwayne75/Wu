package org.fourthline.cling.model.message.header;

import java.net.MalformedURLException;
import java.net.URL;

public class LocationHeader
  extends UpnpHeader<URL>
{
  public LocationHeader() {}
  
  public LocationHeader(URL value)
  {
    setValue(value);
  }
  
  public LocationHeader(String s)
  {
    setString(s);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      URL url = new URL(s);
      setValue(url);
    }
    catch (MalformedURLException ex)
    {
      throw new InvalidHeaderException("Invalid URI: " + ex.getMessage());
    }
  }
  
  public String getString()
  {
    return ((URL)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\LocationHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */