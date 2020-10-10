package org.fourthline.cling.support.renderingcontrol.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Channel;

public abstract class GetMute
  extends ActionCallback
{
  private static Logger log = Logger.getLogger(GetMute.class.getName());
  
  public GetMute(Service service)
  {
    this(new UnsignedIntegerFourBytes(0L), service);
  }
  
  public GetMute(UnsignedIntegerFourBytes instanceId, Service service)
  {
    super(new ActionInvocation(service.getAction("GetMute")));
    getActionInvocation().setInput("InstanceID", instanceId);
    getActionInvocation().setInput("Channel", Channel.Master.toString());
  }
  
  public void success(ActionInvocation invocation)
  {
    boolean currentMute = ((Boolean)invocation.getOutput("CurrentMute").getValue()).booleanValue();
    received(invocation, currentMute);
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, boolean paramBoolean);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\callback\GetMute.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */