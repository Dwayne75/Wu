package org.fourthline.cling.protocol;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest.Method;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.RegistrationException;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;

public class RetrieveRemoteDescriptors
  implements Runnable
{
  private static final Logger log = Logger.getLogger(RetrieveRemoteDescriptors.class.getName());
  private final UpnpService upnpService;
  private RemoteDevice rd;
  private static final Set<URL> activeRetrievals = new CopyOnWriteArraySet();
  protected List<UDN> errorsAlreadyLogged = new ArrayList();
  
  public RetrieveRemoteDescriptors(UpnpService upnpService, RemoteDevice rd)
  {
    this.upnpService = upnpService;
    this.rd = rd;
  }
  
  public UpnpService getUpnpService()
  {
    return this.upnpService;
  }
  
  public void run()
  {
    URL deviceURL = ((RemoteDeviceIdentity)this.rd.getIdentity()).getDescriptorURL();
    if (activeRetrievals.contains(deviceURL))
    {
      log.finer("Exiting early, active retrieval for URL already in progress: " + deviceURL);
      return;
    }
    if (getUpnpService().getRegistry().getRemoteDevice(((RemoteDeviceIdentity)this.rd.getIdentity()).getUdn(), true) != null)
    {
      log.finer("Exiting early, already discovered: " + deviceURL);
      return;
    }
    try
    {
      activeRetrievals.add(deviceURL);
      describe();
    }
    catch (RouterException ex)
    {
      log.log(Level.WARNING, "Descriptor retrieval failed: " + deviceURL, ex);
    }
    finally
    {
      activeRetrievals.remove(deviceURL);
    }
  }
  
  protected void describe()
    throws RouterException
  {
    if (getUpnpService().getRouter() == null)
    {
      log.warning("Router not yet initialized");
      return;
    }
    try
    {
      StreamRequestMessage deviceDescRetrievalMsg = new StreamRequestMessage(UpnpRequest.Method.GET, ((RemoteDeviceIdentity)this.rd.getIdentity()).getDescriptorURL());
      
      UpnpHeaders headers = getUpnpService().getConfiguration().getDescriptorRetrievalHeaders((RemoteDeviceIdentity)this.rd.getIdentity());
      if (headers != null) {
        deviceDescRetrievalMsg.getHeaders().putAll(headers);
      }
      log.fine("Sending device descriptor retrieval message: " + deviceDescRetrievalMsg);
      deviceDescMsg = getUpnpService().getRouter().send(deviceDescRetrievalMsg);
    }
    catch (IllegalArgumentException ex)
    {
      StreamResponseMessage deviceDescMsg;
      log.warning("Device descriptor retrieval failed: " + 
      
        ((RemoteDeviceIdentity)this.rd.getIdentity()).getDescriptorURL() + ", possibly invalid URL: " + ex); return;
    }
    StreamResponseMessage deviceDescMsg;
    StreamRequestMessage deviceDescRetrievalMsg;
    if (deviceDescMsg == null)
    {
      log.warning("Device descriptor retrieval failed, no response: " + 
        ((RemoteDeviceIdentity)this.rd.getIdentity()).getDescriptorURL());
      
      return;
    }
    if (((UpnpResponse)deviceDescMsg.getOperation()).isFailed())
    {
      log.warning("Device descriptor retrieval failed: " + 
      
        ((RemoteDeviceIdentity)this.rd.getIdentity()).getDescriptorURL() + ", " + 
        
        ((UpnpResponse)deviceDescMsg.getOperation()).getResponseDetails());
      
      return;
    }
    if (!deviceDescMsg.isContentTypeTextUDA()) {
      log.fine("Received device descriptor without or with invalid Content-Type: " + 
      
        ((RemoteDeviceIdentity)this.rd.getIdentity()).getDescriptorURL());
    }
    String descriptorContent = deviceDescMsg.getBodyString();
    if ((descriptorContent == null) || (descriptorContent.length() == 0))
    {
      log.warning("Received empty device descriptor:" + ((RemoteDeviceIdentity)this.rd.getIdentity()).getDescriptorURL());
      return;
    }
    log.fine("Received root device descriptor: " + deviceDescMsg);
    describe(descriptorContent);
  }
  
  protected void describe(String descriptorXML)
    throws RouterException
  {
    boolean notifiedStart = false;
    RemoteDevice describedDevice = null;
    try
    {
      DeviceDescriptorBinder deviceDescriptorBinder = getUpnpService().getConfiguration().getDeviceDescriptorBinderUDA10();
      
      describedDevice = (RemoteDevice)deviceDescriptorBinder.describe(this.rd, descriptorXML);
      
      log.fine("Remote device described (without services) notifying listeners: " + describedDevice);
      notifiedStart = getUpnpService().getRegistry().notifyDiscoveryStart(describedDevice);
      
      log.fine("Hydrating described device's services: " + describedDevice);
      hydratedDevice = describeServices(describedDevice);
      if (hydratedDevice == null)
      {
        if (!this.errorsAlreadyLogged.contains(((RemoteDeviceIdentity)this.rd.getIdentity()).getUdn()))
        {
          this.errorsAlreadyLogged.add(((RemoteDeviceIdentity)this.rd.getIdentity()).getUdn());
          log.warning("Device service description failed: " + this.rd);
        }
        if (notifiedStart) {
          getUpnpService().getRegistry().notifyDiscoveryFailure(describedDevice, new DescriptorBindingException("Device service description failed: " + this.rd));
        }
        return;
      }
      log.fine("Adding fully hydrated remote device to registry: " + hydratedDevice);
      
      getUpnpService().getRegistry().addDevice(hydratedDevice);
    }
    catch (ValidationException ex)
    {
      RemoteDevice hydratedDevice;
      if (!this.errorsAlreadyLogged.contains(((RemoteDeviceIdentity)this.rd.getIdentity()).getUdn()))
      {
        this.errorsAlreadyLogged.add(((RemoteDeviceIdentity)this.rd.getIdentity()).getUdn());
        log.warning("Could not validate device model: " + this.rd);
        for (ValidationError validationError : ex.getErrors()) {
          log.warning(validationError.toString());
        }
        if ((describedDevice != null) && (notifiedStart)) {
          getUpnpService().getRegistry().notifyDiscoveryFailure(describedDevice, ex);
        }
      }
    }
    catch (DescriptorBindingException ex)
    {
      log.warning("Could not hydrate device or its services from descriptor: " + this.rd);
      log.warning("Cause was: " + Exceptions.unwrap(ex));
      if ((describedDevice != null) && (notifiedStart)) {
        getUpnpService().getRegistry().notifyDiscoveryFailure(describedDevice, ex);
      }
    }
    catch (RegistrationException ex)
    {
      log.warning("Adding hydrated device to registry failed: " + this.rd);
      log.warning("Cause was: " + ex.toString());
      if ((describedDevice != null) && (notifiedStart)) {
        getUpnpService().getRegistry().notifyDiscoveryFailure(describedDevice, ex);
      }
    }
  }
  
  protected RemoteDevice describeServices(RemoteDevice currentDevice)
    throws RouterException, DescriptorBindingException, ValidationException
  {
    List<RemoteService> describedServices = new ArrayList();
    Object localObject;
    if (currentDevice.hasServices())
    {
      List<RemoteService> filteredServices = filterExclusiveServices(currentDevice.getServices());
      for (localObject = filteredServices.iterator(); ((Iterator)localObject).hasNext();)
      {
        service = (RemoteService)((Iterator)localObject).next();
        svc = describeService(service);
        if (svc != null) {
          describedServices.add(svc);
        } else {
          log.warning("Skipping invalid service '" + service + "' of: " + currentDevice);
        }
      }
    }
    RemoteService service;
    RemoteService svc;
    List<RemoteDevice> describedEmbeddedDevices = new ArrayList();
    if (currentDevice.hasEmbeddedDevices())
    {
      localObject = currentDevice.getEmbeddedDevices();service = localObject.length;
      for (svc = 0; svc < service; svc++)
      {
        RemoteDevice embeddedDevice = localObject[svc];
        if (embeddedDevice != null)
        {
          RemoteDevice describedEmbeddedDevice = describeServices(embeddedDevice);
          if (describedEmbeddedDevice != null) {
            describedEmbeddedDevices.add(describedEmbeddedDevice);
          }
        }
      }
    }
    Icon[] iconDupes = new Icon[currentDevice.getIcons().length];
    for (int i = 0; i < currentDevice.getIcons().length; i++)
    {
      Icon icon = currentDevice.getIcons()[i];
      iconDupes[i] = icon.deepCopy();
    }
    return currentDevice.newInstance(
      ((RemoteDeviceIdentity)currentDevice.getIdentity()).getUdn(), currentDevice
      .getVersion(), currentDevice
      .getType(), currentDevice
      .getDetails(), iconDupes, currentDevice
      
      .toServiceArray(describedServices), describedEmbeddedDevices);
  }
  
  protected RemoteService describeService(RemoteService service)
    throws RouterException, DescriptorBindingException, ValidationException
  {
    try
    {
      descriptorURL = ((RemoteDevice)service.getDevice()).normalizeURI(service.getDescriptorURI());
    }
    catch (IllegalArgumentException e)
    {
      URL descriptorURL;
      log.warning("Could not normalize service descriptor URL: " + service.getDescriptorURI());
      return null;
    }
    URL descriptorURL;
    StreamRequestMessage serviceDescRetrievalMsg = new StreamRequestMessage(UpnpRequest.Method.GET, descriptorURL);
    
    UpnpHeaders headers = getUpnpService().getConfiguration().getDescriptorRetrievalHeaders((RemoteDeviceIdentity)((RemoteDevice)service.getDevice()).getIdentity());
    if (headers != null) {
      serviceDescRetrievalMsg.getHeaders().putAll(headers);
    }
    log.fine("Sending service descriptor retrieval message: " + serviceDescRetrievalMsg);
    StreamResponseMessage serviceDescMsg = getUpnpService().getRouter().send(serviceDescRetrievalMsg);
    if (serviceDescMsg == null)
    {
      log.warning("Could not retrieve service descriptor, no response: " + service);
      return null;
    }
    if (((UpnpResponse)serviceDescMsg.getOperation()).isFailed())
    {
      log.warning("Service descriptor retrieval failed: " + descriptorURL + ", " + 
      
        ((UpnpResponse)serviceDescMsg.getOperation()).getResponseDetails());
      return null;
    }
    if (!serviceDescMsg.isContentTypeTextUDA()) {
      log.fine("Received service descriptor without or with invalid Content-Type: " + descriptorURL);
    }
    String descriptorContent = serviceDescMsg.getBodyString();
    if ((descriptorContent == null) || (descriptorContent.length() == 0))
    {
      log.warning("Received empty service descriptor:" + descriptorURL);
      return null;
    }
    log.fine("Received service descriptor, hydrating service model: " + serviceDescMsg);
    
    ServiceDescriptorBinder serviceDescriptorBinder = getUpnpService().getConfiguration().getServiceDescriptorBinderUDA10();
    
    return (RemoteService)serviceDescriptorBinder.describe(service, descriptorContent);
  }
  
  protected List<RemoteService> filterExclusiveServices(RemoteService[] services)
  {
    ServiceType[] exclusiveTypes = getUpnpService().getConfiguration().getExclusiveServiceTypes();
    if ((exclusiveTypes == null) || (exclusiveTypes.length == 0)) {
      return Arrays.asList(services);
    }
    List<RemoteService> exclusiveServices = new ArrayList();
    for (RemoteService discoveredService : services) {
      for (ServiceType exclusiveType : exclusiveTypes) {
        if (discoveredService.getServiceType().implementsVersion(exclusiveType))
        {
          log.fine("Including exclusive service: " + discoveredService);
          exclusiveServices.add(discoveredService);
        }
        else
        {
          log.fine("Excluding unwanted service: " + exclusiveType);
        }
      }
    }
    return exclusiveServices;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\RetrieveRemoteDescriptors.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */