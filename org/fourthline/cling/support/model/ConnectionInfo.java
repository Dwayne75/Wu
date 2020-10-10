package org.fourthline.cling.support.model;

import org.fourthline.cling.model.ServiceReference;

public class ConnectionInfo
{
  protected final int connectionID;
  protected final int rcsID;
  protected final int avTransportID;
  protected final ProtocolInfo protocolInfo;
  protected final ServiceReference peerConnectionManager;
  protected final int peerConnectionID;
  protected final Direction direction;
  
  public static enum Status
  {
    OK,  ContentFormatMismatch,  InsufficientBandwidth,  UnreliableChannel,  Unknown;
    
    private Status() {}
  }
  
  public static enum Direction
  {
    Output,  Input;
    
    private Direction() {}
    
    public Direction getOpposite()
    {
      return equals(Output) ? Input : Output;
    }
  }
  
  protected Status connectionStatus = Status.Unknown;
  
  public ConnectionInfo()
  {
    this(0, 0, 0, null, null, -1, Direction.Input, Status.Unknown);
  }
  
  public ConnectionInfo(int connectionID, int rcsID, int avTransportID, ProtocolInfo protocolInfo, ServiceReference peerConnectionManager, int peerConnectionID, Direction direction, Status connectionStatus)
  {
    this.connectionID = connectionID;
    this.rcsID = rcsID;
    this.avTransportID = avTransportID;
    this.protocolInfo = protocolInfo;
    this.peerConnectionManager = peerConnectionManager;
    this.peerConnectionID = peerConnectionID;
    this.direction = direction;
    this.connectionStatus = connectionStatus;
  }
  
  public int getConnectionID()
  {
    return this.connectionID;
  }
  
  public int getRcsID()
  {
    return this.rcsID;
  }
  
  public int getAvTransportID()
  {
    return this.avTransportID;
  }
  
  public ProtocolInfo getProtocolInfo()
  {
    return this.protocolInfo;
  }
  
  public ServiceReference getPeerConnectionManager()
  {
    return this.peerConnectionManager;
  }
  
  public int getPeerConnectionID()
  {
    return this.peerConnectionID;
  }
  
  public Direction getDirection()
  {
    return this.direction;
  }
  
  public synchronized Status getConnectionStatus()
  {
    return this.connectionStatus;
  }
  
  public synchronized void setConnectionStatus(Status connectionStatus)
  {
    this.connectionStatus = connectionStatus;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    ConnectionInfo that = (ConnectionInfo)o;
    if (this.avTransportID != that.avTransportID) {
      return false;
    }
    if (this.connectionID != that.connectionID) {
      return false;
    }
    if (this.peerConnectionID != that.peerConnectionID) {
      return false;
    }
    if (this.rcsID != that.rcsID) {
      return false;
    }
    if (this.connectionStatus != that.connectionStatus) {
      return false;
    }
    if (this.direction != that.direction) {
      return false;
    }
    if (this.peerConnectionManager != null ? !this.peerConnectionManager.equals(that.peerConnectionManager) : that.peerConnectionManager != null) {
      return false;
    }
    if (this.protocolInfo != null ? !this.protocolInfo.equals(that.protocolInfo) : that.protocolInfo != null) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.connectionID;
    result = 31 * result + this.rcsID;
    result = 31 * result + this.avTransportID;
    result = 31 * result + (this.protocolInfo != null ? this.protocolInfo.hashCode() : 0);
    result = 31 * result + (this.peerConnectionManager != null ? this.peerConnectionManager.hashCode() : 0);
    result = 31 * result + this.peerConnectionID;
    result = 31 * result + this.direction.hashCode();
    result = 31 * result + this.connectionStatus.hashCode();
    return result;
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") ID: " + getConnectionID() + ", Status: " + getConnectionStatus();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\ConnectionInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */