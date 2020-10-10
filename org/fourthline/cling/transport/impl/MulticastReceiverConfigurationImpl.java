package org.fourthline.cling.transport.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.fourthline.cling.transport.spi.MulticastReceiverConfiguration;

public class MulticastReceiverConfigurationImpl
  implements MulticastReceiverConfiguration
{
  private InetAddress group;
  private int port;
  private int maxDatagramBytes;
  
  public MulticastReceiverConfigurationImpl(InetAddress group, int port, int maxDatagramBytes)
  {
    this.group = group;
    this.port = port;
    this.maxDatagramBytes = maxDatagramBytes;
  }
  
  public MulticastReceiverConfigurationImpl(InetAddress group, int port)
  {
    this(group, port, 640);
  }
  
  public MulticastReceiverConfigurationImpl(String group, int port, int maxDatagramBytes)
    throws UnknownHostException
  {
    this(InetAddress.getByName(group), port, maxDatagramBytes);
  }
  
  public MulticastReceiverConfigurationImpl(String group, int port)
    throws UnknownHostException
  {
    this(InetAddress.getByName(group), port, 640);
  }
  
  public InetAddress getGroup()
  {
    return this.group;
  }
  
  public void setGroup(InetAddress group)
  {
    this.group = group;
  }
  
  public int getPort()
  {
    return this.port;
  }
  
  public void setPort(int port)
  {
    this.port = port;
  }
  
  public int getMaxDatagramBytes()
  {
    return this.maxDatagramBytes;
  }
  
  public void setMaxDatagramBytes(int maxDatagramBytes)
  {
    this.maxDatagramBytes = maxDatagramBytes;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\MulticastReceiverConfigurationImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */