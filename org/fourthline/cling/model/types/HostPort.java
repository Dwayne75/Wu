package org.fourthline.cling.model.types;

public class HostPort
{
  private String host;
  private int port;
  
  public HostPort() {}
  
  public HostPort(String host, int port)
  {
    this.host = host;
    this.port = port;
  }
  
  public String getHost()
  {
    return this.host;
  }
  
  public void setHost(String host)
  {
    this.host = host;
  }
  
  public int getPort()
  {
    return this.port;
  }
  
  public void setPort(int port)
  {
    this.port = port;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    HostPort hostPort = (HostPort)o;
    if (this.port != hostPort.port) {
      return false;
    }
    if (!this.host.equals(hostPort.host)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.host.hashCode();
    result = 31 * result + this.port;
    return result;
  }
  
  public String toString()
  {
    return this.host + ":" + this.port;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\HostPort.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */