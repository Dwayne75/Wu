package org.fourthline.cling.protocol.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.DiscoveryOptions;
import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.discovery.IncomingSearchRequest;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponse;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponseDeviceType;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponseRootDevice;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponseServiceType;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponseUDN;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.message.header.MXHeader;
import org.fourthline.cling.model.message.header.RootDeviceHeader;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.ServiceTypeHeader;
import org.fourthline.cling.model.message.header.UDNHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.ReceivingAsync;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;

public class ReceivingSearch
  extends ReceivingAsync<IncomingSearchRequest>
{
  private static final Logger log = Logger.getLogger(ReceivingSearch.class.getName());
  private static final boolean LOG_ENABLED = log.isLoggable(Level.FINE);
  protected final Random randomGenerator = new Random();
  
  public ReceivingSearch(UpnpService upnpService, IncomingDatagramMessage<UpnpRequest> inputMessage)
  {
    super(upnpService, new IncomingSearchRequest(inputMessage));
  }
  
  protected void execute()
    throws RouterException
  {
    if (getUpnpService().getRouter() == null)
    {
      log.fine("Router hasn't completed initialization, ignoring received search message");
      return;
    }
    if (!((IncomingSearchRequest)getInputMessage()).isMANSSDPDiscover())
    {
      log.fine("Invalid search request, no or invalid MAN ssdp:discover header: " + getInputMessage());
      return;
    }
    UpnpHeader searchTarget = ((IncomingSearchRequest)getInputMessage()).getSearchTarget();
    if (searchTarget == null)
    {
      log.fine("Invalid search request, did not contain ST header: " + getInputMessage());
      return;
    }
    List<NetworkAddress> activeStreamServers = getUpnpService().getRouter().getActiveStreamServers(((IncomingSearchRequest)getInputMessage()).getLocalAddress());
    if (activeStreamServers.size() == 0)
    {
      log.fine("Aborting search response, no active stream servers found (network disabled?)");
      return;
    }
    for (NetworkAddress activeStreamServer : activeStreamServers) {
      sendResponses(searchTarget, activeStreamServer);
    }
  }
  
  protected boolean waitBeforeExecution()
    throws InterruptedException
  {
    Integer mx = ((IncomingSearchRequest)getInputMessage()).getMX();
    if (mx == null)
    {
      log.fine("Invalid search request, did not contain MX header: " + getInputMessage());
      return false;
    }
    if ((mx.intValue() > 120) || (mx.intValue() <= 0)) {
      mx = MXHeader.DEFAULT_VALUE;
    }
    if (getUpnpService().getRegistry().getLocalDevices().size() > 0)
    {
      int sleepTime = this.randomGenerator.nextInt(mx.intValue() * 1000);
      log.fine("Sleeping " + sleepTime + " milliseconds to avoid flooding with search responses");
      Thread.sleep(sleepTime);
    }
    return true;
  }
  
  protected void sendResponses(UpnpHeader searchTarget, NetworkAddress activeStreamServer)
    throws RouterException
  {
    if ((searchTarget instanceof STAllHeader)) {
      sendSearchResponseAll(activeStreamServer);
    } else if ((searchTarget instanceof RootDeviceHeader)) {
      sendSearchResponseRootDevices(activeStreamServer);
    } else if ((searchTarget instanceof UDNHeader)) {
      sendSearchResponseUDN((UDN)searchTarget.getValue(), activeStreamServer);
    } else if ((searchTarget instanceof DeviceTypeHeader)) {
      sendSearchResponseDeviceType((DeviceType)searchTarget.getValue(), activeStreamServer);
    } else if ((searchTarget instanceof ServiceTypeHeader)) {
      sendSearchResponseServiceType((ServiceType)searchTarget.getValue(), activeStreamServer);
    } else {
      log.warning("Non-implemented search request target: " + searchTarget.getClass());
    }
  }
  
  protected void sendSearchResponseAll(NetworkAddress activeStreamServer)
    throws RouterException
  {
    if (LOG_ENABLED) {
      log.fine("Responding to 'all' search with advertisement messages for all local devices");
    }
    for (LocalDevice localDevice : getUpnpService().getRegistry().getLocalDevices()) {
      if (!isAdvertisementDisabled(localDevice))
      {
        if (LOG_ENABLED) {
          log.finer("Sending root device messages: " + localDevice);
        }
        List<OutgoingSearchResponse> rootDeviceMsgs = createDeviceMessages(localDevice, activeStreamServer);
        for (Object localObject1 = rootDeviceMsgs.iterator(); ((Iterator)localObject1).hasNext();)
        {
          upnpMessage = (OutgoingSearchResponse)((Iterator)localObject1).next();
          getUpnpService().getRouter().send(upnpMessage);
        }
        if (localDevice.hasEmbeddedDevices())
        {
          localObject1 = (LocalDevice[])localDevice.findEmbeddedDevices();upnpMessage = localObject1.length;
          for (OutgoingSearchResponse localOutgoingSearchResponse1 = 0; localOutgoingSearchResponse1 < upnpMessage; localOutgoingSearchResponse1++)
          {
            LocalDevice embeddedDevice = localObject1[localOutgoingSearchResponse1];
            if (LOG_ENABLED) {
              log.finer("Sending embedded device messages: " + embeddedDevice);
            }
            List<OutgoingSearchResponse> embeddedDeviceMsgs = createDeviceMessages(embeddedDevice, activeStreamServer);
            for (OutgoingSearchResponse upnpMessage : embeddedDeviceMsgs) {
              getUpnpService().getRouter().send(upnpMessage);
            }
          }
        }
        Object serviceTypeMsgs = createServiceTypeMessages(localDevice, activeStreamServer);
        if (((List)serviceTypeMsgs).size() > 0)
        {
          if (LOG_ENABLED) {
            log.finer("Sending service type messages");
          }
          for (OutgoingSearchResponse upnpMessage : (List)serviceTypeMsgs) {
            getUpnpService().getRouter().send(upnpMessage);
          }
        }
      }
    }
    OutgoingSearchResponse upnpMessage;
  }
  
  protected List<OutgoingSearchResponse> createDeviceMessages(LocalDevice device, NetworkAddress activeStreamServer)
  {
    List<OutgoingSearchResponse> msgs = new ArrayList();
    if (device.isRoot()) {
      msgs.add(new OutgoingSearchResponseRootDevice(
      
        (IncomingDatagramMessage)getInputMessage(), 
        getDescriptorLocation(activeStreamServer, device), device));
    }
    msgs.add(new OutgoingSearchResponseUDN(
    
      (IncomingDatagramMessage)getInputMessage(), 
      getDescriptorLocation(activeStreamServer, device), device));
    
    msgs.add(new OutgoingSearchResponseDeviceType(
    
      (IncomingDatagramMessage)getInputMessage(), 
      getDescriptorLocation(activeStreamServer, device), device));
    for (OutgoingSearchResponse msg : msgs) {
      prepareOutgoingSearchResponse(msg);
    }
    return msgs;
  }
  
  protected List<OutgoingSearchResponse> createServiceTypeMessages(LocalDevice device, NetworkAddress activeStreamServer)
  {
    List<OutgoingSearchResponse> msgs = new ArrayList();
    for (ServiceType serviceType : device.findServiceTypes())
    {
      OutgoingSearchResponse message = new OutgoingSearchResponseServiceType((IncomingDatagramMessage)getInputMessage(), getDescriptorLocation(activeStreamServer, device), device, serviceType);
      
      prepareOutgoingSearchResponse(message);
      msgs.add(message);
    }
    return msgs;
  }
  
  protected void sendSearchResponseRootDevices(NetworkAddress activeStreamServer)
    throws RouterException
  {
    log.fine("Responding to root device search with advertisement messages for all local root devices");
    for (LocalDevice device : getUpnpService().getRegistry().getLocalDevices()) {
      if (!isAdvertisementDisabled(device))
      {
        OutgoingSearchResponse message = new OutgoingSearchResponseRootDevice((IncomingDatagramMessage)getInputMessage(), getDescriptorLocation(activeStreamServer, device), device);
        
        prepareOutgoingSearchResponse(message);
        getUpnpService().getRouter().send(message);
      }
    }
  }
  
  protected void sendSearchResponseUDN(UDN udn, NetworkAddress activeStreamServer)
    throws RouterException
  {
    Device device = getUpnpService().getRegistry().getDevice(udn, false);
    if ((device != null) && ((device instanceof LocalDevice)))
    {
      if (isAdvertisementDisabled((LocalDevice)device)) {
        return;
      }
      log.fine("Responding to UDN device search: " + udn);
      
      OutgoingSearchResponse message = new OutgoingSearchResponseUDN((IncomingDatagramMessage)getInputMessage(), getDescriptorLocation(activeStreamServer, (LocalDevice)device), (LocalDevice)device);
      
      prepareOutgoingSearchResponse(message);
      getUpnpService().getRouter().send(message);
    }
  }
  
  protected void sendSearchResponseDeviceType(DeviceType deviceType, NetworkAddress activeStreamServer)
    throws RouterException
  {
    log.fine("Responding to device type search: " + deviceType);
    Collection<Device> devices = getUpnpService().getRegistry().getDevices(deviceType);
    for (Device device : devices) {
      if ((device instanceof LocalDevice)) {
        if (!isAdvertisementDisabled((LocalDevice)device))
        {
          log.finer("Sending matching device type search result for: " + device);
          
          OutgoingSearchResponse message = new OutgoingSearchResponseDeviceType((IncomingDatagramMessage)getInputMessage(), getDescriptorLocation(activeStreamServer, (LocalDevice)device), (LocalDevice)device);
          
          prepareOutgoingSearchResponse(message);
          getUpnpService().getRouter().send(message);
        }
      }
    }
  }
  
  protected void sendSearchResponseServiceType(ServiceType serviceType, NetworkAddress activeStreamServer)
    throws RouterException
  {
    log.fine("Responding to service type search: " + serviceType);
    Collection<Device> devices = getUpnpService().getRegistry().getDevices(serviceType);
    for (Device device : devices) {
      if ((device instanceof LocalDevice)) {
        if (!isAdvertisementDisabled((LocalDevice)device))
        {
          log.finer("Sending matching service type search result: " + device);
          
          OutgoingSearchResponse message = new OutgoingSearchResponseServiceType((IncomingDatagramMessage)getInputMessage(), getDescriptorLocation(activeStreamServer, (LocalDevice)device), (LocalDevice)device, serviceType);
          
          prepareOutgoingSearchResponse(message);
          getUpnpService().getRouter().send(message);
        }
      }
    }
  }
  
  protected Location getDescriptorLocation(NetworkAddress activeStreamServer, LocalDevice device)
  {
    return new Location(activeStreamServer, getUpnpService().getConfiguration().getNamespace().getDescriptorPathString(device));
  }
  
  protected boolean isAdvertisementDisabled(LocalDevice device)
  {
    DiscoveryOptions options = getUpnpService().getRegistry().getDiscoveryOptions(device.getIdentity().getUdn());
    return (options != null) && (!options.isAdvertised());
  }
  
  protected void prepareOutgoingSearchResponse(OutgoingSearchResponse message) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\async\ReceivingSearch.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */