package org.fourthline.cling.support.connectionmanager;

import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariables;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.csv.CSV;
import org.fourthline.cling.model.types.csv.CSVUnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.ConnectionInfo;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;

@UpnpService(serviceId=@UpnpServiceId("ConnectionManager"), serviceType=@UpnpServiceType(value="ConnectionManager", version=1), stringConvertibleTypes={ProtocolInfo.class, ProtocolInfos.class, ServiceReference.class})
@UpnpStateVariables({@org.fourthline.cling.binding.annotations.UpnpStateVariable(name="SourceProtocolInfo", datatype="string"), @org.fourthline.cling.binding.annotations.UpnpStateVariable(name="SinkProtocolInfo", datatype="string"), @org.fourthline.cling.binding.annotations.UpnpStateVariable(name="CurrentConnectionIDs", datatype="string"), @org.fourthline.cling.binding.annotations.UpnpStateVariable(name="A_ARG_TYPE_ConnectionStatus", allowedValuesEnum=org.fourthline.cling.support.model.ConnectionInfo.Status.class, sendEvents=false), @org.fourthline.cling.binding.annotations.UpnpStateVariable(name="A_ARG_TYPE_ConnectionManager", datatype="string", sendEvents=false), @org.fourthline.cling.binding.annotations.UpnpStateVariable(name="A_ARG_TYPE_Direction", allowedValuesEnum=org.fourthline.cling.support.model.ConnectionInfo.Direction.class, sendEvents=false), @org.fourthline.cling.binding.annotations.UpnpStateVariable(name="A_ARG_TYPE_ProtocolInfo", datatype="string", sendEvents=false), @org.fourthline.cling.binding.annotations.UpnpStateVariable(name="A_ARG_TYPE_ConnectionID", datatype="i4", sendEvents=false), @org.fourthline.cling.binding.annotations.UpnpStateVariable(name="A_ARG_TYPE_AVTransportID", datatype="i4", sendEvents=false), @org.fourthline.cling.binding.annotations.UpnpStateVariable(name="A_ARG_TYPE_RcsID", datatype="i4", sendEvents=false)})
public class ConnectionManagerService
{
  private static final Logger log = Logger.getLogger(ConnectionManagerService.class.getName());
  protected final PropertyChangeSupport propertyChangeSupport;
  protected final Map<Integer, ConnectionInfo> activeConnections = new ConcurrentHashMap();
  protected final ProtocolInfos sourceProtocolInfo;
  protected final ProtocolInfos sinkProtocolInfo;
  
  public ConnectionManagerService()
  {
    this(new ConnectionInfo[] { new ConnectionInfo() });
  }
  
  public ConnectionManagerService(ProtocolInfos sourceProtocolInfo, ProtocolInfos sinkProtocolInfo)
  {
    this(sourceProtocolInfo, sinkProtocolInfo, new ConnectionInfo[] { new ConnectionInfo() });
  }
  
  public ConnectionManagerService(ConnectionInfo... activeConnections)
  {
    this(null, new ProtocolInfos(new ProtocolInfo[0]), new ProtocolInfos(new ProtocolInfo[0]), activeConnections);
  }
  
  public ConnectionManagerService(ProtocolInfos sourceProtocolInfo, ProtocolInfos sinkProtocolInfo, ConnectionInfo... activeConnections)
  {
    this(null, sourceProtocolInfo, sinkProtocolInfo, activeConnections);
  }
  
  public ConnectionManagerService(PropertyChangeSupport propertyChangeSupport, ProtocolInfos sourceProtocolInfo, ProtocolInfos sinkProtocolInfo, ConnectionInfo... activeConnections)
  {
    this.propertyChangeSupport = (propertyChangeSupport == null ? new PropertyChangeSupport(this) : propertyChangeSupport);
    
    this.sourceProtocolInfo = sourceProtocolInfo;
    this.sinkProtocolInfo = sinkProtocolInfo;
    for (ConnectionInfo activeConnection : activeConnections) {
      this.activeConnections.put(Integer.valueOf(activeConnection.getConnectionID()), activeConnection);
    }
  }
  
  public PropertyChangeSupport getPropertyChangeSupport()
  {
    return this.propertyChangeSupport;
  }
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="RcsID", getterName="getRcsID"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="AVTransportID", getterName="getAvTransportID"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="ProtocolInfo", getterName="getProtocolInfo"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="PeerConnectionManager", stateVariable="A_ARG_TYPE_ConnectionManager", getterName="getPeerConnectionManager"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="PeerConnectionID", stateVariable="A_ARG_TYPE_ConnectionID", getterName="getPeerConnectionID"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="Direction", getterName="getDirection"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="Status", stateVariable="A_ARG_TYPE_ConnectionStatus", getterName="getConnectionStatus")})
  public synchronized ConnectionInfo getCurrentConnectionInfo(@UpnpInputArgument(name="ConnectionID") int connectionId)
    throws ActionException
  {
    log.fine("Getting connection information of connection ID: " + connectionId);
    ConnectionInfo info;
    if ((info = (ConnectionInfo)this.activeConnections.get(Integer.valueOf(connectionId))) == null) {
      throw new ConnectionManagerException(ConnectionManagerErrorCode.INVALID_CONNECTION_REFERENCE, "Non-active connection ID: " + connectionId);
    }
    return info;
  }
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="ConnectionIDs")})
  public synchronized CSV<UnsignedIntegerFourBytes> getCurrentConnectionIDs()
  {
    CSV<UnsignedIntegerFourBytes> csv = new CSVUnsignedIntegerFourBytes();
    for (Integer connectionID : this.activeConnections.keySet()) {
      csv.add(new UnsignedIntegerFourBytes(connectionID.intValue()));
    }
    log.fine("Returning current connection IDs: " + csv.size());
    return csv;
  }
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="Source", stateVariable="SourceProtocolInfo", getterName="getSourceProtocolInfo"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="Sink", stateVariable="SinkProtocolInfo", getterName="getSinkProtocolInfo")})
  public synchronized void getProtocolInfo()
    throws ActionException
  {}
  
  public synchronized ProtocolInfos getSourceProtocolInfo()
  {
    return this.sourceProtocolInfo;
  }
  
  public synchronized ProtocolInfos getSinkProtocolInfo()
  {
    return this.sinkProtocolInfo;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\connectionmanager\ConnectionManagerService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */