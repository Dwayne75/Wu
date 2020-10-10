package org.fourthline.cling.support.connectionmanager.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.model.ProtocolInfos;

public abstract class GetProtocolInfo
  extends ActionCallback
{
  public GetProtocolInfo(Service service)
  {
    this(service, null);
  }
  
  protected GetProtocolInfo(Service service, ControlPoint controlPoint)
  {
    super(new ActionInvocation(service.getAction("GetProtocolInfo")), controlPoint);
  }
  
  public void success(ActionInvocation invocation)
  {
    try
    {
      ActionArgumentValue sink = invocation.getOutput("Sink");
      ActionArgumentValue source = invocation.getOutput("Source");
      
      received(invocation, sink != null ? new ProtocolInfos(sink
      
        .toString()) : null, source != null ? new ProtocolInfos(source
        .toString()) : null);
    }
    catch (Exception ex)
    {
      invocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, "Can't parse ProtocolInfo response: " + ex, ex));
      
      failure(invocation, null);
    }
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, ProtocolInfos paramProtocolInfos1, ProtocolInfos paramProtocolInfos2);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\connectionmanager\callback\GetProtocolInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */