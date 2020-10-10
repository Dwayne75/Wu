package org.fourthline.cling.registry;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;

abstract class RegistryItems<D extends Device, S extends GENASubscription>
{
  protected final RegistryImpl registry;
  protected final Set<RegistryItem<UDN, D>> deviceItems = new HashSet();
  protected final Set<RegistryItem<String, S>> subscriptionItems = new HashSet();
  
  RegistryItems(RegistryImpl registry)
  {
    this.registry = registry;
  }
  
  Set<RegistryItem<UDN, D>> getDeviceItems()
  {
    return this.deviceItems;
  }
  
  Set<RegistryItem<String, S>> getSubscriptionItems()
  {
    return this.subscriptionItems;
  }
  
  abstract void add(D paramD);
  
  abstract boolean remove(D paramD);
  
  abstract void removeAll();
  
  abstract void maintain();
  
  abstract void shutdown();
  
  D get(UDN udn, boolean rootOnly)
  {
    for (RegistryItem<UDN, D> item : this.deviceItems)
    {
      D device = (Device)item.getItem();
      if (device.getIdentity().getUdn().equals(udn)) {
        return device;
      }
      if (!rootOnly)
      {
        D foundDevice = ((Device)item.getItem()).findDevice(udn);
        if (foundDevice != null) {
          return foundDevice;
        }
      }
    }
    return null;
  }
  
  Collection<D> get(DeviceType deviceType)
  {
    Collection<D> devices = new HashSet();
    for (RegistryItem<UDN, D> item : this.deviceItems)
    {
      D[] d = (Device[])((Device)item.getItem()).findDevices(deviceType);
      if (d != null) {
        devices.addAll(Arrays.asList(d));
      }
    }
    return devices;
  }
  
  Collection<D> get(ServiceType serviceType)
  {
    Collection<D> devices = new HashSet();
    for (RegistryItem<UDN, D> item : this.deviceItems)
    {
      D[] d = (Device[])((Device)item.getItem()).findDevices(serviceType);
      if (d != null) {
        devices.addAll(Arrays.asList(d));
      }
    }
    return devices;
  }
  
  Collection<D> get()
  {
    Collection<D> devices = new HashSet();
    for (RegistryItem<UDN, D> item : this.deviceItems) {
      devices.add(item.getItem());
    }
    return devices;
  }
  
  boolean contains(D device)
  {
    return contains(device.getIdentity().getUdn());
  }
  
  boolean contains(UDN udn)
  {
    return this.deviceItems.contains(new RegistryItem(udn));
  }
  
  void addSubscription(S subscription)
  {
    RegistryItem<String, S> subscriptionItem = new RegistryItem(subscription.getSubscriptionId(), subscription, subscription.getActualDurationSeconds());
    
    this.subscriptionItems.add(subscriptionItem);
  }
  
  boolean updateSubscription(S subscription)
  {
    if (removeSubscription(subscription))
    {
      addSubscription(subscription);
      return true;
    }
    return false;
  }
  
  boolean removeSubscription(S subscription)
  {
    return this.subscriptionItems.remove(new RegistryItem(subscription.getSubscriptionId()));
  }
  
  S getSubscription(String subscriptionId)
  {
    for (RegistryItem<String, S> registryItem : this.subscriptionItems) {
      if (((String)registryItem.getKey()).equals(subscriptionId)) {
        return (GENASubscription)registryItem.getItem();
      }
    }
    return null;
  }
  
  Resource[] getResources(Device device)
    throws RegistrationException
  {
    try
    {
      return this.registry.getConfiguration().getNamespace().getResources(device);
    }
    catch (ValidationException ex)
    {
      throw new RegistrationException("Resource discover error: " + ex.toString(), ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\RegistryItems.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */