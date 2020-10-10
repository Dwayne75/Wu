package org.fourthline.cling.registry;

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;

public abstract interface RegistryListener
{
  public abstract void remoteDeviceDiscoveryStarted(Registry paramRegistry, RemoteDevice paramRemoteDevice);
  
  public abstract void remoteDeviceDiscoveryFailed(Registry paramRegistry, RemoteDevice paramRemoteDevice, Exception paramException);
  
  public abstract void remoteDeviceAdded(Registry paramRegistry, RemoteDevice paramRemoteDevice);
  
  public abstract void remoteDeviceUpdated(Registry paramRegistry, RemoteDevice paramRemoteDevice);
  
  public abstract void remoteDeviceRemoved(Registry paramRegistry, RemoteDevice paramRemoteDevice);
  
  public abstract void localDeviceAdded(Registry paramRegistry, LocalDevice paramLocalDevice);
  
  public abstract void localDeviceRemoved(Registry paramRegistry, LocalDevice paramLocalDevice);
  
  public abstract void beforeShutdown(Registry paramRegistry);
  
  public abstract void afterShutdown();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\RegistryListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */