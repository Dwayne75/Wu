package org.fourthline.cling.model.meta;

import java.net.URI;

public class ManufacturerDetails
{
  private String manufacturer;
  private URI manufacturerURI;
  
  ManufacturerDetails() {}
  
  public ManufacturerDetails(String manufacturer)
  {
    this.manufacturer = manufacturer;
  }
  
  public ManufacturerDetails(URI manufacturerURI)
  {
    this.manufacturerURI = manufacturerURI;
  }
  
  public ManufacturerDetails(String manufacturer, URI manufacturerURI)
  {
    this.manufacturer = manufacturer;
    this.manufacturerURI = manufacturerURI;
  }
  
  public ManufacturerDetails(String manufacturer, String manufacturerURI)
    throws IllegalArgumentException
  {
    this.manufacturer = manufacturer;
    this.manufacturerURI = URI.create(manufacturerURI);
  }
  
  public String getManufacturer()
  {
    return this.manufacturer;
  }
  
  public URI getManufacturerURI()
  {
    return this.manufacturerURI;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\ManufacturerDetails.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */