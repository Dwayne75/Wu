package org.fourthline.cling.model.meta;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.resource.ServiceEventCallbackResource;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.seamless.util.URIUtil;

public class RemoteDevice
  extends Device<RemoteDeviceIdentity, RemoteDevice, RemoteService>
{
  public RemoteDevice(RemoteDeviceIdentity identity)
    throws ValidationException
  {
    super(identity);
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, RemoteService service)
    throws ValidationException
  {
    super(identity, type, details, null, new RemoteService[] { service });
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, RemoteService service, RemoteDevice embeddedDevice)
    throws ValidationException
  {
    super(identity, type, details, null, new RemoteService[] { service }, new RemoteDevice[] { embeddedDevice });
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, RemoteService[] services)
    throws ValidationException
  {
    super(identity, type, details, null, services);
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, RemoteService[] services, RemoteDevice[] embeddedDevices)
    throws ValidationException
  {
    super(identity, type, details, null, services, embeddedDevices);
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, RemoteService service)
    throws ValidationException
  {
    super(identity, type, details, new Icon[] { icon }, new RemoteService[] { service });
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, RemoteService service, RemoteDevice embeddedDevice)
    throws ValidationException
  {
    super(identity, type, details, new Icon[] { icon }, new RemoteService[] { service }, new RemoteDevice[] { embeddedDevice });
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, RemoteService[] services)
    throws ValidationException
  {
    super(identity, type, details, new Icon[] { icon }, services);
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, RemoteService[] services, RemoteDevice[] embeddedDevices)
    throws ValidationException
  {
    super(identity, type, details, new Icon[] { icon }, services, embeddedDevices);
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService service)
    throws ValidationException
  {
    super(identity, type, details, icons, new RemoteService[] { service });
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService service, RemoteDevice embeddedDevice)
    throws ValidationException
  {
    super(identity, type, details, icons, new RemoteService[] { service }, new RemoteDevice[] { embeddedDevice });
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService[] services)
    throws ValidationException
  {
    super(identity, type, details, icons, services);
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService[] services, RemoteDevice[] embeddedDevices)
    throws ValidationException
  {
    super(identity, type, details, icons, services, embeddedDevices);
  }
  
  public RemoteDevice(RemoteDeviceIdentity identity, UDAVersion version, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService[] services, RemoteDevice[] embeddedDevices)
    throws ValidationException
  {
    super(identity, version, type, details, icons, services, embeddedDevices);
  }
  
  public RemoteService[] getServices()
  {
    return this.services != null ? (RemoteService[])this.services : new RemoteService[0];
  }
  
  public RemoteDevice[] getEmbeddedDevices()
  {
    return this.embeddedDevices != null ? (RemoteDevice[])this.embeddedDevices : new RemoteDevice[0];
  }
  
  public URL normalizeURI(URI relativeOrAbsoluteURI)
  {
    if ((getDetails() != null) && (getDetails().getBaseURL() != null)) {
      return URIUtil.createAbsoluteURL(getDetails().getBaseURL(), relativeOrAbsoluteURI);
    }
    return URIUtil.createAbsoluteURL(((RemoteDeviceIdentity)getIdentity()).getDescriptorURL(), relativeOrAbsoluteURI);
  }
  
  public RemoteDevice newInstance(UDN udn, UDAVersion version, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService[] services, List<RemoteDevice> embeddedDevices)
    throws ValidationException
  {
    return new RemoteDevice(new RemoteDeviceIdentity(udn, (RemoteDeviceIdentity)getIdentity()), version, type, details, icons, services, embeddedDevices.size() > 0 ? (RemoteDevice[])embeddedDevices.toArray(new RemoteDevice[embeddedDevices.size()]) : null);
  }
  
  public RemoteService newInstance(ServiceType serviceType, ServiceId serviceId, URI descriptorURI, URI controlURI, URI eventSubscriptionURI, Action<RemoteService>[] actions, StateVariable<RemoteService>[] stateVariables)
    throws ValidationException
  {
    return new RemoteService(serviceType, serviceId, descriptorURI, controlURI, eventSubscriptionURI, actions, stateVariables);
  }
  
  public RemoteDevice[] toDeviceArray(Collection<RemoteDevice> col)
  {
    return (RemoteDevice[])col.toArray(new RemoteDevice[col.size()]);
  }
  
  public RemoteService[] newServiceArray(int size)
  {
    return new RemoteService[size];
  }
  
  public RemoteService[] toServiceArray(Collection<RemoteService> col)
  {
    return (RemoteService[])col.toArray(new RemoteService[col.size()]);
  }
  
  public Resource[] discoverResources(Namespace namespace)
  {
    List<Resource> discovered = new ArrayList();
    for (RemoteService service : getServices()) {
      if (service != null) {
        discovered.add(new ServiceEventCallbackResource(namespace.getEventCallbackPath(service), service));
      }
    }
    if (hasEmbeddedDevices()) {
      for (Device embeddedDevice : getEmbeddedDevices()) {
        if (embeddedDevice != null) {
          discovered.addAll(Arrays.asList(embeddedDevice.discoverResources(namespace)));
        }
      }
    }
    return (Resource[])discovered.toArray(new Resource[discovered.size()]);
  }
  
  public RemoteDevice getRoot()
  {
    if (isRoot()) {
      return this;
    }
    RemoteDevice current = this;
    while (current.getParentDevice() != null) {
      current = (RemoteDevice)current.getParentDevice();
    }
    return current;
  }
  
  public RemoteDevice findDevice(UDN udn)
  {
    return (RemoteDevice)find(udn, this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\RemoteDevice.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */