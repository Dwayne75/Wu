package org.fourthline.cling;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;

public abstract interface UpnpService
{
  public abstract UpnpServiceConfiguration getConfiguration();
  
  public abstract ControlPoint getControlPoint();
  
  public abstract ProtocolFactory getProtocolFactory();
  
  public abstract Registry getRegistry();
  
  public abstract Router getRouter();
  
  public abstract void shutdown();
  
  public static class Shutdown {}
  
  public static class Start {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\UpnpService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */