package org.fourthline.cling.registry.event;

import org.fourthline.cling.model.meta.Device;

public class DeviceDiscovery<D extends Device>
{
  protected D device;
  
  public DeviceDiscovery(D device)
  {
    this.device = device;
  }
  
  public D getDevice()
  {
    return this.device;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\event\DeviceDiscovery.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */