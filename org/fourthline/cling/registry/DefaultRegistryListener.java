package org.fourthline.cling.registry;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;

public class DefaultRegistryListener
  implements RegistryListener
{
  public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {}
  
  public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {}
  
  public void remoteDeviceAdded(Registry registry, RemoteDevice device)
  {
    deviceAdded(registry, device);
  }
  
  public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {}
  
  public void remoteDeviceRemoved(Registry registry, RemoteDevice device)
  {
    deviceRemoved(registry, device);
  }
  
  public void localDeviceAdded(Registry registry, LocalDevice device)
  {
    deviceAdded(registry, device);
  }
  
  public void localDeviceRemoved(Registry registry, LocalDevice device)
  {
    deviceRemoved(registry, device);
  }
  
  public void deviceAdded(Registry registry, Device device) {}
  
  public void deviceRemoved(Registry registry, Device device) {}
  
  public void beforeShutdown(Registry registry) {}
  
  public void afterShutdown() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\DefaultRegistryListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */