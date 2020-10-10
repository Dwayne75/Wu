package org.fourthline.cling.registry;

import java.net.URI;
import java.util.Collection;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.DiscoveryOptions;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.ProtocolFactory;

public abstract interface Registry
{
  public abstract UpnpService getUpnpService();
  
  public abstract UpnpServiceConfiguration getConfiguration();
  
  public abstract ProtocolFactory getProtocolFactory();
  
  public abstract void shutdown();
  
  public abstract void pause();
  
  public abstract void resume();
  
  public abstract boolean isPaused();
  
  public abstract void addListener(RegistryListener paramRegistryListener);
  
  public abstract void removeListener(RegistryListener paramRegistryListener);
  
  public abstract Collection<RegistryListener> getListeners();
  
  public abstract boolean notifyDiscoveryStart(RemoteDevice paramRemoteDevice);
  
  public abstract void notifyDiscoveryFailure(RemoteDevice paramRemoteDevice, Exception paramException);
  
  public abstract void addDevice(LocalDevice paramLocalDevice)
    throws RegistrationException;
  
  public abstract void addDevice(LocalDevice paramLocalDevice, DiscoveryOptions paramDiscoveryOptions)
    throws RegistrationException;
  
  public abstract void setDiscoveryOptions(UDN paramUDN, DiscoveryOptions paramDiscoveryOptions);
  
  public abstract DiscoveryOptions getDiscoveryOptions(UDN paramUDN);
  
  public abstract void addDevice(RemoteDevice paramRemoteDevice)
    throws RegistrationException;
  
  public abstract boolean update(RemoteDeviceIdentity paramRemoteDeviceIdentity);
  
  public abstract boolean removeDevice(LocalDevice paramLocalDevice);
  
  public abstract boolean removeDevice(RemoteDevice paramRemoteDevice);
  
  public abstract boolean removeDevice(UDN paramUDN);
  
  public abstract void removeAllLocalDevices();
  
  public abstract void removeAllRemoteDevices();
  
  public abstract Device getDevice(UDN paramUDN, boolean paramBoolean);
  
  public abstract LocalDevice getLocalDevice(UDN paramUDN, boolean paramBoolean);
  
  public abstract RemoteDevice getRemoteDevice(UDN paramUDN, boolean paramBoolean);
  
  public abstract Collection<LocalDevice> getLocalDevices();
  
  public abstract Collection<RemoteDevice> getRemoteDevices();
  
  public abstract Collection<Device> getDevices();
  
  public abstract Collection<Device> getDevices(DeviceType paramDeviceType);
  
  public abstract Collection<Device> getDevices(ServiceType paramServiceType);
  
  public abstract Service getService(ServiceReference paramServiceReference);
  
  public abstract void addResource(Resource paramResource);
  
  public abstract void addResource(Resource paramResource, int paramInt);
  
  public abstract boolean removeResource(Resource paramResource);
  
  public abstract Resource getResource(URI paramURI)
    throws IllegalArgumentException;
  
  public abstract <T extends Resource> T getResource(Class<T> paramClass, URI paramURI)
    throws IllegalArgumentException;
  
  public abstract Collection<Resource> getResources();
  
  public abstract <T extends Resource> Collection<T> getResources(Class<T> paramClass);
  
  public abstract void addLocalSubscription(LocalGENASubscription paramLocalGENASubscription);
  
  public abstract LocalGENASubscription getLocalSubscription(String paramString);
  
  public abstract boolean updateLocalSubscription(LocalGENASubscription paramLocalGENASubscription);
  
  public abstract boolean removeLocalSubscription(LocalGENASubscription paramLocalGENASubscription);
  
  public abstract void addRemoteSubscription(RemoteGENASubscription paramRemoteGENASubscription);
  
  public abstract RemoteGENASubscription getRemoteSubscription(String paramString);
  
  public abstract void updateRemoteSubscription(RemoteGENASubscription paramRemoteGENASubscription);
  
  public abstract void removeRemoteSubscription(RemoteGENASubscription paramRemoteGENASubscription);
  
  public abstract void registerPendingRemoteSubscription(RemoteGENASubscription paramRemoteGENASubscription);
  
  public abstract void unregisterPendingRemoteSubscription(RemoteGENASubscription paramRemoteGENASubscription);
  
  public abstract RemoteGENASubscription getWaitRemoteSubscription(String paramString);
  
  public abstract void advertiseLocalDevices();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\Registry.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */