package org.fourthline.cling.support.connectionmanager.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.ConnectionInfo.Direction;
import org.fourthline.cling.support.model.ProtocolInfo;

public abstract class PrepareForConnection
  extends ActionCallback
{
  public PrepareForConnection(Service service, ProtocolInfo remoteProtocolInfo, ServiceReference peerConnectionManager, int peerConnectionID, ConnectionInfo.Direction direction)
  {
    this(service, null, remoteProtocolInfo, peerConnectionManager, peerConnectionID, direction);
  }
  
  public PrepareForConnection(Service service, ControlPoint controlPoint, ProtocolInfo remoteProtocolInfo, ServiceReference peerConnectionManager, int peerConnectionID, ConnectionInfo.Direction direction)
  {
    super(new ActionInvocation(service.getAction("PrepareForConnection")), controlPoint);
    
    getActionInvocation().setInput("RemoteProtocolInfo", remoteProtocolInfo.toString());
    getActionInvocation().setInput("PeerConnectionManager", peerConnectionManager.toString());
    getActionInvocation().setInput("PeerConnectionID", Integer.valueOf(peerConnectionID));
    getActionInvocation().setInput("Direction", direction.toString());
  }
  
  public void success(ActionInvocation invocation)
  {
    received(invocation, 
    
      ((Integer)invocation.getOutput("ConnectionID").getValue()).intValue(), 
      ((Integer)invocation.getOutput("RcsID").getValue()).intValue(), 
      ((Integer)invocation.getOutput("AVTransportID").getValue()).intValue());
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, int paramInt1, int paramInt2, int paramInt3);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\connectionmanager\callback\PrepareForConnection.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */