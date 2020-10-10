package org.fourthline.cling.model;

import java.net.InetAddress;
import java.util.Arrays;

public class NetworkAddress
{
  protected InetAddress address;
  protected int port;
  protected byte[] hardwareAddress;
  
  public NetworkAddress(InetAddress address, int port)
  {
    this(address, port, null);
  }
  
  public NetworkAddress(InetAddress address, int port, byte[] hardwareAddress)
  {
    this.address = address;
    this.port = port;
    this.hardwareAddress = hardwareAddress;
  }
  
  public InetAddress getAddress()
  {
    return this.address;
  }
  
  public int getPort()
  {
    return this.port;
  }
  
  public byte[] getHardwareAddress()
  {
    return this.hardwareAddress;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    NetworkAddress that = (NetworkAddress)o;
    if (this.port != that.port) {
      return false;
    }
    if (!this.address.equals(that.address)) {
      return false;
    }
    if (!Arrays.equals(this.hardwareAddress, that.hardwareAddress)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.address.hashCode();
    result = 31 * result + this.port;
    result = 31 * result + (this.hardwareAddress != null ? Arrays.hashCode(this.hardwareAddress) : 0);
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\NetworkAddress.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */