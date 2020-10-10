package org.fourthline.cling.registry.event;

import org.fourthline.cling.model.meta.RemoteDevice;

public class FailedRemoteDeviceDiscovery
  extends DeviceDiscovery<RemoteDevice>
{
  protected Exception exception;
  
  public FailedRemoteDeviceDiscovery(RemoteDevice device, Exception ex)
  {
    super(device);
    this.exception = ex;
  }
  
  public Exception getException()
  {
    return this.exception;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\event\FailedRemoteDeviceDiscovery.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */