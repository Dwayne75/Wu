package org.fourthline.cling.model.meta;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;

public abstract class Device<DI extends DeviceIdentity, D extends Device, S extends Service>
  implements Validatable
{
  private static final Logger log = Logger.getLogger(Device.class.getName());
  private final DI identity;
  private final UDAVersion version;
  private final DeviceType type;
  private final DeviceDetails details;
  private final Icon[] icons;
  protected final S[] services;
  protected final D[] embeddedDevices;
  private D parentDevice;
  
  public Device(DI identity)
    throws ValidationException
  {
    this(identity, null, null, null, null, null);
  }
  
  public Device(DI identity, DeviceType type, DeviceDetails details, Icon[] icons, S[] services)
    throws ValidationException
  {
    this(identity, null, type, details, icons, services, null);
  }
  
  public Device(DI identity, DeviceType type, DeviceDetails details, Icon[] icons, S[] services, D[] embeddedDevices)
    throws ValidationException
  {
    this(identity, null, type, details, icons, services, embeddedDevices);
  }
  
  public Device(DI identity, UDAVersion version, DeviceType type, DeviceDetails details, Icon[] icons, S[] services, D[] embeddedDevices)
    throws ValidationException
  {
    this.identity = identity;
    this.version = (version == null ? new UDAVersion() : version);
    this.type = type;
    this.details = details;
    
    List<Icon> validIcons = new ArrayList();
    Object localObject1;
    Icon icon;
    if (icons != null)
    {
      Icon[] arrayOfIcon = icons;int i = arrayOfIcon.length;
      for (localObject1 = 0; localObject1 < i; localObject1++)
      {
        icon = arrayOfIcon[localObject1];
        if (icon != null)
        {
          icon.setDevice(this);
          List<ValidationError> iconErrors = icon.validate();
          if (iconErrors.isEmpty()) {
            validIcons.add(icon);
          } else {
            log.warning("Discarding invalid '" + icon + "': " + iconErrors);
          }
        }
      }
    }
    this.icons = ((Icon[])validIcons.toArray(new Icon[validIcons.size()]));
    
    boolean allNullServices = true;
    S service;
    if (services != null)
    {
      S[] arrayOfS = services;localObject1 = arrayOfS.length;
      for (icon = 0; icon < localObject1; icon++)
      {
        service = arrayOfS[icon];
        if (service != null)
        {
          allNullServices = false;
          service.setDevice(this);
        }
      }
    }
    this.services = ((services == null) || (allNullServices) ? null : services);
    
    boolean allNullEmbedded = true;
    if (embeddedDevices != null)
    {
      localObject1 = embeddedDevices;icon = localObject1.length;
      for (service = 0; service < icon; service++)
      {
        D embeddedDevice = localObject1[service];
        if (embeddedDevice != null)
        {
          allNullEmbedded = false;
          embeddedDevice.setParentDevice(this);
        }
      }
    }
    this.embeddedDevices = ((embeddedDevices == null) || (allNullEmbedded) ? null : embeddedDevices);
    
    Object errors = validate();
    if (((List)errors).size() > 0)
    {
      if (log.isLoggable(Level.FINEST)) {
        for (ValidationError error : (List)errors) {
          log.finest(error.toString());
        }
      }
      throw new ValidationException("Validation of device graph failed, call getErrors() on exception", (List)errors);
    }
  }
  
  public DI getIdentity()
  {
    return this.identity;
  }
  
  public UDAVersion getVersion()
  {
    return this.version;
  }
  
  public DeviceType getType()
  {
    return this.type;
  }
  
  public DeviceDetails getDetails()
  {
    return this.details;
  }
  
  public DeviceDetails getDetails(RemoteClientInfo info)
  {
    return getDetails();
  }
  
  public Icon[] getIcons()
  {
    return this.icons;
  }
  
  public boolean hasIcons()
  {
    return (getIcons() != null) && (getIcons().length > 0);
  }
  
  public boolean hasServices()
  {
    return (getServices() != null) && (getServices().length > 0);
  }
  
  public boolean hasEmbeddedDevices()
  {
    return (getEmbeddedDevices() != null) && (getEmbeddedDevices().length > 0);
  }
  
  public D getParentDevice()
  {
    return this.parentDevice;
  }
  
  void setParentDevice(D parentDevice)
  {
    if (this.parentDevice != null) {
      throw new IllegalStateException("Final value has been set already, model is immutable");
    }
    this.parentDevice = parentDevice;
  }
  
  public boolean isRoot()
  {
    return getParentDevice() == null;
  }
  
  public abstract S[] getServices();
  
  public abstract D[] getEmbeddedDevices();
  
  public abstract D getRoot();
  
  public abstract D findDevice(UDN paramUDN);
  
  public D[] findEmbeddedDevices()
  {
    return toDeviceArray(findEmbeddedDevices(this));
  }
  
  public D[] findDevices(DeviceType deviceType)
  {
    return toDeviceArray(find(deviceType, this));
  }
  
  public D[] findDevices(ServiceType serviceType)
  {
    return toDeviceArray(find(serviceType, this));
  }
  
  public Icon[] findIcons()
  {
    List<Icon> icons = new ArrayList();
    if (hasIcons()) {
      icons.addAll(Arrays.asList(getIcons()));
    }
    D[] embeddedDevices = findEmbeddedDevices();
    for (D embeddedDevice : embeddedDevices) {
      if (embeddedDevice.hasIcons()) {
        icons.addAll(Arrays.asList(embeddedDevice.getIcons()));
      }
    }
    return (Icon[])icons.toArray(new Icon[icons.size()]);
  }
  
  public S[] findServices()
  {
    return toServiceArray(findServices(null, null, this));
  }
  
  public S[] findServices(ServiceType serviceType)
  {
    return toServiceArray(findServices(serviceType, null, this));
  }
  
  protected D find(UDN udn, D current)
  {
    if ((current.getIdentity() != null) && (current.getIdentity().getUdn() != null) && 
      (current.getIdentity().getUdn().equals(udn))) {
      return current;
    }
    if (current.hasEmbeddedDevices()) {
      for (D embeddedDevice : (Device[])current.getEmbeddedDevices())
      {
        D match;
        if ((match = find(udn, embeddedDevice)) != null) {
          return match;
        }
      }
    }
    return null;
  }
  
  protected Collection<D> findEmbeddedDevices(D current)
  {
    Collection<D> devices = new HashSet();
    if ((!current.isRoot()) && (current.getIdentity().getUdn() != null)) {
      devices.add(current);
    }
    if (current.hasEmbeddedDevices()) {
      for (D embeddedDevice : (Device[])current.getEmbeddedDevices()) {
        devices.addAll(findEmbeddedDevices(embeddedDevice));
      }
    }
    return devices;
  }
  
  protected Collection<D> find(DeviceType deviceType, D current)
  {
    Collection<D> devices = new HashSet();
    if ((current.getType() != null) && (current.getType().implementsVersion(deviceType))) {
      devices.add(current);
    }
    if (current.hasEmbeddedDevices()) {
      for (D embeddedDevice : (Device[])current.getEmbeddedDevices()) {
        devices.addAll(find(deviceType, embeddedDevice));
      }
    }
    return devices;
  }
  
  protected Collection<D> find(ServiceType serviceType, D current)
  {
    Collection<S> services = findServices(serviceType, null, current);
    Collection<D> devices = new HashSet();
    for (Service service : services) {
      devices.add(service.getDevice());
    }
    return devices;
  }
  
  protected Collection<S> findServices(ServiceType serviceType, ServiceId serviceId, D current)
  {
    Collection services = new HashSet();
    Service service;
    if (current.hasServices()) {
      for (service : current.getServices()) {
        if (isMatch(service, serviceType, serviceId)) {
          services.add(service);
        }
      }
    }
    Object embeddedDevices = findEmbeddedDevices(current);
    if (embeddedDevices != null) {
      for (Object embeddedDevice : (Collection)embeddedDevices) {
        if (((Device)embeddedDevice).hasServices()) {
          for (Service service : ((Device)embeddedDevice).getServices()) {
            if (isMatch(service, serviceType, serviceId)) {
              services.add(service);
            }
          }
        }
      }
    }
    return services;
  }
  
  public S findService(ServiceId serviceId)
  {
    Collection<S> services = findServices(null, serviceId, this);
    return services.size() == 1 ? (Service)services.iterator().next() : null;
  }
  
  public S findService(ServiceType serviceType)
  {
    Collection<S> services = findServices(serviceType, null, this);
    return services.size() > 0 ? (Service)services.iterator().next() : null;
  }
  
  public ServiceType[] findServiceTypes()
  {
    Collection<S> services = findServices(null, null, this);
    Collection<ServiceType> col = new HashSet();
    for (S service : services) {
      col.add(service.getServiceType());
    }
    return (ServiceType[])col.toArray(new ServiceType[col.size()]);
  }
  
  private boolean isMatch(Service s, ServiceType serviceType, ServiceId serviceId)
  {
    boolean matchesType = (serviceType == null) || (s.getServiceType().implementsVersion(serviceType));
    boolean matchesId = (serviceId == null) || (s.getServiceId().equals(serviceId));
    return (matchesType) && (matchesId);
  }
  
  public boolean isFullyHydrated()
  {
    S[] services = findServices();
    for (S service : services) {
      if (service.hasStateVariables()) {
        return true;
      }
    }
    return false;
  }
  
  public String getDisplayString()
  {
    String cleanModelName = null;
    String cleanModelNumber = null;
    if ((getDetails() != null) && (getDetails().getModelDetails() != null))
    {
      ModelDetails modelDetails = getDetails().getModelDetails();
      if (modelDetails.getModelName() != null) {
        cleanModelName = (modelDetails.getModelNumber() != null) && (modelDetails.getModelName().endsWith(modelDetails.getModelNumber())) ? modelDetails.getModelName().substring(0, modelDetails.getModelName().length() - modelDetails.getModelNumber().length()) : modelDetails.getModelName();
      }
      if (cleanModelName != null) {
        cleanModelNumber = (modelDetails.getModelNumber() != null) && (!cleanModelName.startsWith(modelDetails.getModelNumber())) ? modelDetails.getModelNumber() : "";
      } else {
        cleanModelNumber = modelDetails.getModelNumber();
      }
    }
    StringBuilder sb = new StringBuilder();
    if ((getDetails() != null) && (getDetails().getManufacturerDetails() != null))
    {
      if ((cleanModelName != null) && (getDetails().getManufacturerDetails().getManufacturer() != null)) {
        cleanModelName = cleanModelName.startsWith(getDetails().getManufacturerDetails().getManufacturer()) ? cleanModelName.substring(getDetails().getManufacturerDetails().getManufacturer().length()).trim() : cleanModelName.trim();
      }
      if (getDetails().getManufacturerDetails().getManufacturer() != null) {
        sb.append(getDetails().getManufacturerDetails().getManufacturer());
      }
    }
    sb.append((cleanModelName != null) && (cleanModelName.length() > 0) ? " " + cleanModelName : "");
    sb.append((cleanModelNumber != null) && (cleanModelNumber.length() > 0) ? " " + cleanModelNumber.trim() : "");
    return sb.toString();
  }
  
  public List<ValidationError> validate()
  {
    List<ValidationError> errors = new ArrayList();
    if (getType() != null)
    {
      errors.addAll(getVersion().validate());
      if (getIdentity() != null) {
        errors.addAll(getIdentity().validate());
      }
      if (getDetails() != null) {
        errors.addAll(getDetails().validate());
      }
      if (hasServices()) {
        for (Service service : getServices()) {
          if (service != null) {
            errors.addAll(service.validate());
          }
        }
      }
      if (hasEmbeddedDevices()) {
        for (Device embeddedDevice : getEmbeddedDevices()) {
          if (embeddedDevice != null) {
            errors.addAll(embeddedDevice.validate());
          }
        }
      }
    }
    return errors;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Device device = (Device)o;
    if (!this.identity.equals(device.identity)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    return this.identity.hashCode();
  }
  
  public abstract D newInstance(UDN paramUDN, UDAVersion paramUDAVersion, DeviceType paramDeviceType, DeviceDetails paramDeviceDetails, Icon[] paramArrayOfIcon, S[] paramArrayOfS, List<D> paramList)
    throws ValidationException;
  
  public abstract S newInstance(ServiceType paramServiceType, ServiceId paramServiceId, URI paramURI1, URI paramURI2, URI paramURI3, Action<S>[] paramArrayOfAction, StateVariable<S>[] paramArrayOfStateVariable)
    throws ValidationException;
  
  public abstract D[] toDeviceArray(Collection<D> paramCollection);
  
  public abstract S[] newServiceArray(int paramInt);
  
  public abstract S[] toServiceArray(Collection<S> paramCollection);
  
  public abstract Resource[] discoverResources(Namespace paramNamespace);
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") Identity: " + getIdentity().toString() + ", Root: " + isRoot();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\Device.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */