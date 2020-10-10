package org.fourthline.cling.support.avtransport.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.DeviceCapabilities;

public abstract class GetDeviceCapabilities
  extends ActionCallback
{
  private static Logger log = Logger.getLogger(GetDeviceCapabilities.class.getName());
  
  public GetDeviceCapabilities(Service service)
  {
    this(new UnsignedIntegerFourBytes(0L), service);
  }
  
  public GetDeviceCapabilities(UnsignedIntegerFourBytes instanceId, Service service)
  {
    super(new ActionInvocation(service.getAction("GetDeviceCapabilities")));
    getActionInvocation().setInput("InstanceID", instanceId);
  }
  
  public void success(ActionInvocation invocation)
  {
    DeviceCapabilities caps = new DeviceCapabilities(invocation.getOutputMap());
    received(invocation, caps);
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, DeviceCapabilities paramDeviceCapabilities);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\callback\GetDeviceCapabilities.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */