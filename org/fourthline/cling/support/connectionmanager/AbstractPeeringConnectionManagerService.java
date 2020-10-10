package org.fourthline.cling.support.connectionmanager;

import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.logging.Logger;
import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.csv.CSV;
import org.fourthline.cling.support.connectionmanager.callback.ConnectionComplete;
import org.fourthline.cling.support.connectionmanager.callback.PrepareForConnection;
import org.fourthline.cling.support.model.ConnectionInfo;
import org.fourthline.cling.support.model.ConnectionInfo.Direction;
import org.fourthline.cling.support.model.ConnectionInfo.Status;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;

public abstract class AbstractPeeringConnectionManagerService
  extends ConnectionManagerService
{
  private static final Logger log = Logger.getLogger(AbstractPeeringConnectionManagerService.class.getName());
  
  protected AbstractPeeringConnectionManagerService(ConnectionInfo... activeConnections)
  {
    super(activeConnections);
  }
  
  protected AbstractPeeringConnectionManagerService(ProtocolInfos sourceProtocolInfo, ProtocolInfos sinkProtocolInfo, ConnectionInfo... activeConnections)
  {
    super(sourceProtocolInfo, sinkProtocolInfo, activeConnections);
  }
  
  protected AbstractPeeringConnectionManagerService(PropertyChangeSupport propertyChangeSupport, ProtocolInfos sourceProtocolInfo, ProtocolInfos sinkProtocolInfo, ConnectionInfo... activeConnections)
  {
    super(propertyChangeSupport, sourceProtocolInfo, sinkProtocolInfo, activeConnections);
  }
  
  protected synchronized int getNewConnectionId()
  {
    int currentHighestID = -1;
    for (Integer key : this.activeConnections.keySet()) {
      if (key.intValue() > currentHighestID) {
        currentHighestID = key.intValue();
      }
    }
    currentHighestID++;return currentHighestID;
  }
  
  protected synchronized void storeConnection(ConnectionInfo info)
  {
    CSV<UnsignedIntegerFourBytes> oldConnectionIDs = getCurrentConnectionIDs();
    this.activeConnections.put(Integer.valueOf(info.getConnectionID()), info);
    log.fine("Connection stored, firing event: " + info.getConnectionID());
    CSV<UnsignedIntegerFourBytes> newConnectionIDs = getCurrentConnectionIDs();
    getPropertyChangeSupport().firePropertyChange("CurrentConnectionIDs", oldConnectionIDs, newConnectionIDs);
  }
  
  protected synchronized void removeConnection(int connectionID)
  {
    CSV<UnsignedIntegerFourBytes> oldConnectionIDs = getCurrentConnectionIDs();
    this.activeConnections.remove(Integer.valueOf(connectionID));
    log.fine("Connection removed, firing event: " + connectionID);
    CSV<UnsignedIntegerFourBytes> newConnectionIDs = getCurrentConnectionIDs();
    getPropertyChangeSupport().firePropertyChange("CurrentConnectionIDs", oldConnectionIDs, newConnectionIDs);
  }
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="ConnectionID", stateVariable="A_ARG_TYPE_ConnectionID", getterName="getConnectionID"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="AVTransportID", stateVariable="A_ARG_TYPE_AVTransportID", getterName="getAvTransportID"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="RcsID", stateVariable="A_ARG_TYPE_RcsID", getterName="getRcsID")})
  public synchronized ConnectionInfo prepareForConnection(@UpnpInputArgument(name="RemoteProtocolInfo", stateVariable="A_ARG_TYPE_ProtocolInfo") ProtocolInfo remoteProtocolInfo, @UpnpInputArgument(name="PeerConnectionManager", stateVariable="A_ARG_TYPE_ConnectionManager") ServiceReference peerConnectionManager, @UpnpInputArgument(name="PeerConnectionID", stateVariable="A_ARG_TYPE_ConnectionID") int peerConnectionId, @UpnpInputArgument(name="Direction", stateVariable="A_ARG_TYPE_Direction") String direction)
    throws ActionException
  {
    int connectionId = getNewConnectionId();
    try
    {
      dir = ConnectionInfo.Direction.valueOf(direction);
    }
    catch (Exception ex)
    {
      ConnectionInfo.Direction dir;
      throw new ConnectionManagerException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported direction: " + direction);
    }
    ConnectionInfo.Direction dir;
    log.fine("Preparing for connection with local new ID " + connectionId + " and peer connection ID: " + peerConnectionId);
    
    ConnectionInfo newConnectionInfo = createConnection(connectionId, peerConnectionId, peerConnectionManager, dir, remoteProtocolInfo);
    
    storeConnection(newConnectionInfo);
    
    return newConnectionInfo;
  }
  
  @UpnpAction
  public synchronized void connectionComplete(@UpnpInputArgument(name="ConnectionID", stateVariable="A_ARG_TYPE_ConnectionID") int connectionID)
    throws ActionException
  {
    ConnectionInfo info = getCurrentConnectionInfo(connectionID);
    log.fine("Closing connection ID " + connectionID);
    closeConnection(info);
    removeConnection(connectionID);
  }
  
  public synchronized int createConnectionWithPeer(ServiceReference localServiceReference, ControlPoint controlPoint, final Service peerService, final ProtocolInfo protInfo, final ConnectionInfo.Direction direction)
  {
    final int localConnectionID = getNewConnectionId();
    
    log.fine("Creating new connection ID " + localConnectionID + " with peer: " + peerService);
    final boolean[] failed = new boolean[1];
    new PrepareForConnection(peerService, controlPoint, protInfo, localServiceReference, localConnectionID, direction)
    {
      public void received(ActionInvocation invocation, int peerConnectionID, int rcsID, int avTransportID)
      {
        ConnectionInfo info = new ConnectionInfo(localConnectionID, rcsID, avTransportID, protInfo, peerService.getReference(), peerConnectionID, direction.getOpposite(), ConnectionInfo.Status.OK);
        
        AbstractPeeringConnectionManagerService.this.storeConnection(info);
      }
      
      public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
      {
        AbstractPeeringConnectionManagerService.this.peerFailure(invocation, operation, defaultMsg);
        
        failed[0] = true;
      }
    }.run();
    return failed[0] != 0 ? -1 : localConnectionID;
  }
  
  public synchronized void closeConnectionWithPeer(ControlPoint controlPoint, Service peerService, int connectionID)
    throws ActionException
  {
    closeConnectionWithPeer(controlPoint, peerService, getCurrentConnectionInfo(connectionID));
  }
  
  public synchronized void closeConnectionWithPeer(ControlPoint controlPoint, Service peerService, final ConnectionInfo connectionInfo)
    throws ActionException
  {
    log.fine("Closing connection ID " + connectionInfo.getConnectionID() + " with peer: " + peerService);
    new ConnectionComplete(peerService, controlPoint, connectionInfo
    
      .getPeerConnectionID())
      {
        public void success(ActionInvocation invocation)
        {
          AbstractPeeringConnectionManagerService.this.removeConnection(connectionInfo.getConnectionID());
        }
        
        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
        {
          AbstractPeeringConnectionManagerService.this.peerFailure(invocation, operation, defaultMsg);
        }
      }.run();
  }
  
  protected abstract ConnectionInfo createConnection(int paramInt1, int paramInt2, ServiceReference paramServiceReference, ConnectionInfo.Direction paramDirection, ProtocolInfo paramProtocolInfo)
    throws ActionException;
  
  protected abstract void closeConnection(ConnectionInfo paramConnectionInfo);
  
  protected abstract void peerFailure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse, String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\connectionmanager\AbstractPeeringConnectionManagerService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */