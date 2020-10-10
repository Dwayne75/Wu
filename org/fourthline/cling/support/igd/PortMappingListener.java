package org.fourthline.cling.support.igd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.igd.callback.PortMappingAdd;
import org.fourthline.cling.support.igd.callback.PortMappingDelete;
import org.fourthline.cling.support.model.PortMapping;

public class PortMappingListener
  extends DefaultRegistryListener
{
  private static final Logger log = Logger.getLogger(PortMappingListener.class.getName());
  public static final DeviceType IGD_DEVICE_TYPE = new UDADeviceType("InternetGatewayDevice", 1);
  public static final DeviceType CONNECTION_DEVICE_TYPE = new UDADeviceType("WANConnectionDevice", 1);
  public static final ServiceType IP_SERVICE_TYPE = new UDAServiceType("WANIPConnection", 1);
  public static final ServiceType PPP_SERVICE_TYPE = new UDAServiceType("WANPPPConnection", 1);
  protected PortMapping[] portMappings;
  protected Map<Service, List<PortMapping>> activePortMappings = new HashMap();
  
  public PortMappingListener(PortMapping portMapping)
  {
    this(new PortMapping[] { portMapping });
  }
  
  public PortMappingListener(PortMapping[] portMappings)
  {
    this.portMappings = portMappings;
  }
  
  public synchronized void deviceAdded(Registry registry, Device device)
  {
    Service connectionService;
    if ((connectionService = discoverConnectionService(device)) == null) {
      return;
    }
    log.fine("Activating port mappings on: " + connectionService);
    
    final List<PortMapping> activeForService = new ArrayList();
    for (final PortMapping pm : this.portMappings) {
      new PortMappingAdd(connectionService, registry.getUpnpService().getControlPoint(), pm)
      {
        public void success(ActionInvocation invocation)
        {
          PortMappingListener.log.fine("Port mapping added: " + pm);
          activeForService.add(pm);
        }
        
        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
        {
          PortMappingListener.this.handleFailureMessage("Failed to add port mapping: " + pm);
          PortMappingListener.this.handleFailureMessage("Reason: " + defaultMsg);
        }
      }.run();
    }
    this.activePortMappings.put(connectionService, activeForService);
  }
  
  public synchronized void deviceRemoved(Registry registry, Device device)
  {
    for (Service service : device.findServices())
    {
      Iterator<Map.Entry<Service, List<PortMapping>>> it = this.activePortMappings.entrySet().iterator();
      while (it.hasNext())
      {
        Map.Entry<Service, List<PortMapping>> activeEntry = (Map.Entry)it.next();
        if (((Service)activeEntry.getKey()).equals(service))
        {
          if (((List)activeEntry.getValue()).size() > 0) {
            handleFailureMessage("Device disappeared, couldn't delete port mappings: " + ((List)activeEntry.getValue()).size());
          }
          it.remove();
        }
      }
    }
  }
  
  public synchronized void beforeShutdown(Registry registry)
  {
    for (Map.Entry<Service, List<PortMapping>> activeEntry : this.activePortMappings.entrySet())
    {
      final Iterator<PortMapping> it = ((List)activeEntry.getValue()).iterator();
      while (it.hasNext())
      {
        final PortMapping pm = (PortMapping)it.next();
        log.fine("Trying to delete port mapping on IGD: " + pm);
        new PortMappingDelete((Service)activeEntry.getKey(), registry.getUpnpService().getControlPoint(), pm)
        {
          public void success(ActionInvocation invocation)
          {
            PortMappingListener.log.fine("Port mapping deleted: " + pm);
            it.remove();
          }
          
          public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
          {
            PortMappingListener.this.handleFailureMessage("Failed to delete port mapping: " + pm);
            PortMappingListener.this.handleFailureMessage("Reason: " + defaultMsg);
          }
        }.run();
      }
    }
  }
  
  protected Service discoverConnectionService(Device device)
  {
    if (!device.getType().equals(IGD_DEVICE_TYPE)) {
      return null;
    }
    Device[] connectionDevices = device.findDevices(CONNECTION_DEVICE_TYPE);
    if (connectionDevices.length == 0)
    {
      log.fine("IGD doesn't support '" + CONNECTION_DEVICE_TYPE + "': " + device);
      return null;
    }
    Device connectionDevice = connectionDevices[0];
    log.fine("Using first discovered WAN connection device: " + connectionDevice);
    
    Service ipConnectionService = connectionDevice.findService(IP_SERVICE_TYPE);
    Service pppConnectionService = connectionDevice.findService(PPP_SERVICE_TYPE);
    if ((ipConnectionService == null) && (pppConnectionService == null)) {
      log.fine("IGD doesn't support IP or PPP WAN connection service: " + device);
    }
    return ipConnectionService != null ? ipConnectionService : pppConnectionService;
  }
  
  protected void handleFailureMessage(String s)
  {
    log.warning(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\igd\PortMappingListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */