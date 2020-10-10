package org.fourthline.cling.binding.staging;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.meta.UDAVersion;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDN;

public class MutableDevice
{
  public UDN udn;
  public MutableUDAVersion udaVersion = new MutableUDAVersion();
  public URL baseURL;
  public String deviceType;
  public String friendlyName;
  public String manufacturer;
  public URI manufacturerURI;
  public String modelName;
  public String modelDescription;
  public String modelNumber;
  public URI modelURI;
  public String serialNumber;
  public String upc;
  public URI presentationURI;
  public List<DLNADoc> dlnaDocs = new ArrayList();
  public DLNACaps dlnaCaps;
  public List<MutableIcon> icons = new ArrayList();
  public List<MutableService> services = new ArrayList();
  public List<MutableDevice> embeddedDevices = new ArrayList();
  public MutableDevice parentDevice;
  
  public Device build(Device prototype)
    throws ValidationException
  {
    return build(prototype, createDeviceVersion(), this.baseURL);
  }
  
  public Device build(Device prototype, UDAVersion deviceVersion, URL baseURL)
    throws ValidationException
  {
    List<Device> embeddedDevicesList = new ArrayList();
    for (MutableDevice embeddedDevice : this.embeddedDevices) {
      embeddedDevicesList.add(embeddedDevice.build(prototype, deviceVersion, baseURL));
    }
    return prototype.newInstance(this.udn, deviceVersion, 
    
      createDeviceType(), 
      createDeviceDetails(baseURL), 
      createIcons(), 
      createServices(prototype), embeddedDevicesList);
  }
  
  public UDAVersion createDeviceVersion()
  {
    return new UDAVersion(this.udaVersion.major, this.udaVersion.minor);
  }
  
  public DeviceType createDeviceType()
  {
    return DeviceType.valueOf(this.deviceType);
  }
  
  public DeviceDetails createDeviceDetails(URL baseURL)
  {
    return new DeviceDetails(baseURL, this.friendlyName, new ManufacturerDetails(this.manufacturer, this.manufacturerURI), new ModelDetails(this.modelName, this.modelDescription, this.modelNumber, this.modelURI), this.serialNumber, this.upc, this.presentationURI, (DLNADoc[])this.dlnaDocs.toArray(new DLNADoc[this.dlnaDocs.size()]), this.dlnaCaps);
  }
  
  public Icon[] createIcons()
  {
    Icon[] iconArray = new Icon[this.icons.size()];
    int i = 0;
    for (MutableIcon icon : this.icons) {
      iconArray[(i++)] = icon.build();
    }
    return iconArray;
  }
  
  public Service[] createServices(Device prototype)
    throws ValidationException
  {
    Service[] services = prototype.newServiceArray(this.services.size());
    int i = 0;
    for (MutableService service : this.services) {
      services[(i++)] = service.build(prototype);
    }
    return services;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\staging\MutableDevice.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */