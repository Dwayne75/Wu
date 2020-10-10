package org.fourthline.cling.android;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.registry.Registry;

public abstract interface AndroidUpnpService
{
  public abstract UpnpService get();
  
  public abstract UpnpServiceConfiguration getConfiguration();
  
  public abstract Registry getRegistry();
  
  public abstract ControlPoint getControlPoint();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\android\AndroidUpnpService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */