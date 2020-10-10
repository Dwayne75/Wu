package org.fourthline.cling.support.model;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class Connection
{
  public static class StatusInfo
  {
    private Connection.Status status;
    private long uptimeSeconds;
    private Connection.Error lastError;
    
    public StatusInfo(Connection.Status status, UnsignedIntegerFourBytes uptime, Connection.Error lastError)
    {
      this(status, uptime.getValue().longValue(), lastError);
    }
    
    public StatusInfo(Connection.Status status, long uptimeSeconds, Connection.Error lastError)
    {
      this.status = status;
      this.uptimeSeconds = uptimeSeconds;
      this.lastError = lastError;
    }
    
    public Connection.Status getStatus()
    {
      return this.status;
    }
    
    public long getUptimeSeconds()
    {
      return this.uptimeSeconds;
    }
    
    public UnsignedIntegerFourBytes getUptime()
    {
      return new UnsignedIntegerFourBytes(getUptimeSeconds());
    }
    
    public Connection.Error getLastError()
    {
      return this.lastError;
    }
    
    public boolean equals(Object o)
    {
      if (this == o) {
        return true;
      }
      if ((o == null) || (getClass() != o.getClass())) {
        return false;
      }
      StatusInfo that = (StatusInfo)o;
      if (this.uptimeSeconds != that.uptimeSeconds) {
        return false;
      }
      if (this.lastError != that.lastError) {
        return false;
      }
      if (this.status != that.status) {
        return false;
      }
      return true;
    }
    
    public int hashCode()
    {
      int result = this.status.hashCode();
      result = 31 * result + (int)(this.uptimeSeconds ^ this.uptimeSeconds >>> 32);
      result = 31 * result + this.lastError.hashCode();
      return result;
    }
    
    public String toString()
    {
      return "(" + getClass().getSimpleName() + ") " + getStatus();
    }
  }
  
  public static enum Type
  {
    Unconfigured,  IP_Routed,  IP_Bridged;
    
    private Type() {}
  }
  
  public static enum Status
  {
    Unconfigured,  Connecting,  Connected,  PendingDisconnect,  Disconnecting,  Disconnected;
    
    private Status() {}
  }
  
  public static enum Error
  {
    ERROR_NONE,  ERROR_COMMAND_ABORTED,  ERROR_NOT_ENABLED_FOR_INTERNET,  ERROR_USER_DISCONNECT,  ERROR_ISP_DISCONNECT,  ERROR_IDLE_DISCONNECT,  ERROR_FORCED_DISCONNECT,  ERROR_NO_CARRIER,  ERROR_IP_CONFIGURATION,  ERROR_UNKNOWN;
    
    private Error() {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\Connection.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */