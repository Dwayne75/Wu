package org.fourthline.cling.support.renderingcontrol.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Channel;

public abstract class GetVolume
  extends ActionCallback
{
  private static Logger log = Logger.getLogger(GetVolume.class.getName());
  
  public GetVolume(Service service)
  {
    this(new UnsignedIntegerFourBytes(0L), service);
  }
  
  public GetVolume(UnsignedIntegerFourBytes instanceId, Service service)
  {
    super(new ActionInvocation(service.getAction("GetVolume")));
    getActionInvocation().setInput("InstanceID", instanceId);
    getActionInvocation().setInput("Channel", Channel.Master.toString());
  }
  
  public void success(ActionInvocation invocation)
  {
    boolean ok = true;
    int currentVolume = 0;
    try
    {
      currentVolume = Integer.valueOf(invocation.getOutput("CurrentVolume").getValue().toString()).intValue();
    }
    catch (Exception ex)
    {
      invocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, "Can't parse ProtocolInfo response: " + ex, ex));
      
      failure(invocation, null);
      ok = false;
    }
    if (ok) {
      received(invocation, currentVolume);
    }
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, int paramInt);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\callback\GetVolume.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */