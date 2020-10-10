package org.fourthline.cling.support.avtransport.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.TransportAction;

public abstract class GetCurrentTransportActions
  extends ActionCallback
{
  private static Logger log = Logger.getLogger(GetCurrentTransportActions.class.getName());
  
  public GetCurrentTransportActions(Service service)
  {
    this(new UnsignedIntegerFourBytes(0L), service);
  }
  
  public GetCurrentTransportActions(UnsignedIntegerFourBytes instanceId, Service service)
  {
    super(new ActionInvocation(service.getAction("GetCurrentTransportActions")));
    getActionInvocation().setInput("InstanceID", instanceId);
  }
  
  public void success(ActionInvocation invocation)
  {
    String actionsString = (String)invocation.getOutput("Actions").getValue();
    received(invocation, TransportAction.valueOfCommaSeparatedList(actionsString));
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, TransportAction[] paramArrayOfTransportAction);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\callback\GetCurrentTransportActions.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */