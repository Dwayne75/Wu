package org.fourthline.cling.transport.impl;

import org.fourthline.cling.transport.spi.StreamServerConfiguration;

public class StreamServerConfigurationImpl
  implements StreamServerConfiguration
{
  private int listenPort;
  private int tcpConnectionBacklog;
  
  public StreamServerConfigurationImpl() {}
  
  public StreamServerConfigurationImpl(int listenPort)
  {
    this.listenPort = listenPort;
  }
  
  public int getListenPort()
  {
    return this.listenPort;
  }
  
  public void setListenPort(int listenPort)
  {
    this.listenPort = listenPort;
  }
  
  public int getTcpConnectionBacklog()
  {
    return this.tcpConnectionBacklog;
  }
  
  public void setTcpConnectionBacklog(int tcpConnectionBacklog)
  {
    this.tcpConnectionBacklog = tcpConnectionBacklog;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\StreamServerConfigurationImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */