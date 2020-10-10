package org.fourthline.cling;

import java.io.PrintStream;
import java.util.Collection;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

public class Main
{
  public static void main(String[] args)
    throws Exception
  {
    RegistryListener listener = new RegistryListener()
    {
      public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device)
      {
        System.out.println("Discovery started: " + device
          .getDisplayString());
      }
      
      public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex)
      {
        System.out.println("Discovery failed: " + device
          .getDisplayString() + " => " + ex);
      }
      
      public void remoteDeviceAdded(Registry registry, RemoteDevice device)
      {
        System.out.println("Remote device available: " + device
          .getDisplayString());
      }
      
      public void remoteDeviceUpdated(Registry registry, RemoteDevice device)
      {
        System.out.println("Remote device updated: " + device
          .getDisplayString());
      }
      
      public void remoteDeviceRemoved(Registry registry, RemoteDevice device)
      {
        System.out.println("Remote device removed: " + device
          .getDisplayString());
      }
      
      public void localDeviceAdded(Registry registry, LocalDevice device)
      {
        System.out.println("Local device added: " + device
          .getDisplayString());
      }
      
      public void localDeviceRemoved(Registry registry, LocalDevice device)
      {
        System.out.println("Local device removed: " + device
          .getDisplayString());
      }
      
      public void beforeShutdown(Registry registry)
      {
        System.out.println("Before shutdown, the registry has devices: " + registry
          .getDevices().size());
      }
      
      public void afterShutdown()
      {
        System.out.println("Shutdown of registry complete!");
      }
    };
    System.out.println("Starting Cling...");
    UpnpService upnpService = new UpnpServiceImpl(new RegistryListener[] { listener });
    
    System.out.println("Sending SEARCH message to all devices...");
    upnpService.getControlPoint().search(new STAllHeader());
    
    System.out.println("Waiting 10 seconds before shutting down...");
    Thread.sleep(10000L);
    
    System.out.println("Stopping Cling...");
    upnpService.shutdown();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\Main.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */