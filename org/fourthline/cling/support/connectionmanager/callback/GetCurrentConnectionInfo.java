package org.fourthline.cling.support.connectionmanager.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.model.ConnectionInfo;
import org.fourthline.cling.support.model.ConnectionInfo.Direction;
import org.fourthline.cling.support.model.ConnectionInfo.Status;
import org.fourthline.cling.support.model.ProtocolInfo;

public abstract class GetCurrentConnectionInfo
  extends ActionCallback
{
  public GetCurrentConnectionInfo(Service service, int connectionID)
  {
    this(service, null, connectionID);
  }
  
  protected GetCurrentConnectionInfo(Service service, ControlPoint controlPoint, int connectionID)
  {
    super(new ActionInvocation(service.getAction("GetCurrentConnectionInfo")), controlPoint);
    getActionInvocation().setInput("ConnectionID", Integer.valueOf(connectionID));
  }
  
  public void success(ActionInvocation invocation)
  {
    try
    {
      ConnectionInfo info = new ConnectionInfo(((Integer)invocation.getInput("ConnectionID").getValue()).intValue(), ((Integer)invocation.getOutput("RcsID").getValue()).intValue(), ((Integer)invocation.getOutput("AVTransportID").getValue()).intValue(), new ProtocolInfo(invocation.getOutput("ProtocolInfo").toString()), new ServiceReference(invocation.getOutput("PeerConnectionManager").toString()), ((Integer)invocation.getOutput("PeerConnectionID").getValue()).intValue(), ConnectionInfo.Direction.valueOf(invocation.getOutput("Direction").toString()), ConnectionInfo.Status.valueOf(invocation.getOutput("Status").toString()));
      
      received(invocation, info);
    }
    catch (Exception ex)
    {
      invocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, "Can't parse ConnectionInfo response: " + ex, ex));
      
      failure(invocation, null);
    }
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, ConnectionInfo paramConnectionInfo);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\connectionmanager\callback\GetCurrentConnectionInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */