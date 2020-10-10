package org.fourthline.cling.protocol.async;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.discovery.OutgoingNotificationRequest;
import org.fourthline.cling.model.message.discovery.OutgoingNotificationRequestDeviceType;
import org.fourthline.cling.model.message.discovery.OutgoingNotificationRequestRootDevice;
import org.fourthline.cling.model.message.discovery.OutgoingNotificationRequestServiceType;
import org.fourthline.cling.model.message.discovery.OutgoingNotificationRequestUDN;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.NotificationSubtype;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.protocol.SendingAsync;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;

public abstract class SendingNotification
  extends SendingAsync
{
  private static final Logger log = Logger.getLogger(SendingNotification.class.getName());
  private LocalDevice device;
  
  public SendingNotification(UpnpService upnpService, LocalDevice device)
  {
    super(upnpService);
    this.device = device;
  }
  
  public LocalDevice getDevice()
  {
    return this.device;
  }
  
  protected void execute()
    throws RouterException
  {
    List<NetworkAddress> activeStreamServers = getUpnpService().getRouter().getActiveStreamServers(null);
    if (activeStreamServers.size() == 0)
    {
      log.fine("Aborting notifications, no active stream servers found (network disabled?)");
      return;
    }
    List<Location> descriptorLocations = new ArrayList();
    for (Iterator localIterator = activeStreamServers.iterator(); localIterator.hasNext();)
    {
      activeStreamServer = (NetworkAddress)localIterator.next();
      descriptorLocations.add(new Location(activeStreamServer, 
      
        getUpnpService().getConfiguration().getNamespace().getDescriptorPathString(getDevice())));
    }
    NetworkAddress activeStreamServer;
    for (int i = 0; i < getBulkRepeat(); i++) {
      try
      {
        for (Location descriptorLocation : descriptorLocations) {
          sendMessages(descriptorLocation);
        }
        log.finer("Sleeping " + getBulkIntervalMilliseconds() + " milliseconds");
        Thread.sleep(getBulkIntervalMilliseconds());
      }
      catch (InterruptedException ex)
      {
        log.warning("Advertisement thread was interrupted: " + ex);
      }
    }
  }
  
  protected int getBulkRepeat()
  {
    return 3;
  }
  
  protected int getBulkIntervalMilliseconds()
  {
    return 150;
  }
  
  public void sendMessages(Location descriptorLocation)
    throws RouterException
  {
    log.finer("Sending root device messages: " + getDevice());
    
    List<OutgoingNotificationRequest> rootDeviceMsgs = createDeviceMessages(getDevice(), descriptorLocation);
    for (Object localObject1 = rootDeviceMsgs.iterator(); ((Iterator)localObject1).hasNext();)
    {
      upnpMessage = (OutgoingNotificationRequest)((Iterator)localObject1).next();
      getUpnpService().getRouter().send(upnpMessage);
    }
    OutgoingNotificationRequest upnpMessage;
    if (getDevice().hasEmbeddedDevices())
    {
      localObject1 = (LocalDevice[])getDevice().findEmbeddedDevices();upnpMessage = localObject1.length;
      for (OutgoingNotificationRequest localOutgoingNotificationRequest1 = 0; localOutgoingNotificationRequest1 < upnpMessage; localOutgoingNotificationRequest1++)
      {
        LocalDevice embeddedDevice = localObject1[localOutgoingNotificationRequest1];
        log.finer("Sending embedded device messages: " + embeddedDevice);
        
        List<OutgoingNotificationRequest> embeddedDeviceMsgs = createDeviceMessages(embeddedDevice, descriptorLocation);
        for (OutgoingNotificationRequest upnpMessage : embeddedDeviceMsgs) {
          getUpnpService().getRouter().send(upnpMessage);
        }
      }
    }
    Object serviceTypeMsgs = createServiceTypeMessages(getDevice(), descriptorLocation);
    if (((List)serviceTypeMsgs).size() > 0)
    {
      log.finer("Sending service type messages");
      for (OutgoingNotificationRequest upnpMessage : (List)serviceTypeMsgs) {
        getUpnpService().getRouter().send(upnpMessage);
      }
    }
  }
  
  protected List<OutgoingNotificationRequest> createDeviceMessages(LocalDevice device, Location descriptorLocation)
  {
    List<OutgoingNotificationRequest> msgs = new ArrayList();
    if (device.isRoot()) {
      msgs.add(new OutgoingNotificationRequestRootDevice(descriptorLocation, device, 
      
        getNotificationSubtype()));
    }
    msgs.add(new OutgoingNotificationRequestUDN(descriptorLocation, device, 
    
      getNotificationSubtype()));
    
    msgs.add(new OutgoingNotificationRequestDeviceType(descriptorLocation, device, 
    
      getNotificationSubtype()));
    
    return msgs;
  }
  
  protected List<OutgoingNotificationRequest> createServiceTypeMessages(LocalDevice device, Location descriptorLocation)
  {
    List<OutgoingNotificationRequest> msgs = new ArrayList();
    for (ServiceType serviceType : device.findServiceTypes()) {
      msgs.add(new OutgoingNotificationRequestServiceType(descriptorLocation, device, 
      
        getNotificationSubtype(), serviceType));
    }
    return msgs;
  }
  
  protected abstract NotificationSubtype getNotificationSubtype();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\async\SendingNotification.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */