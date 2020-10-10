package org.fourthline.cling.transport.impl;

import org.fourthline.cling.transport.spi.DatagramIOConfiguration;

public class DatagramIOConfigurationImpl
  implements DatagramIOConfiguration
{
  private int timeToLive = 4;
  private int maxDatagramBytes = 640;
  
  public DatagramIOConfigurationImpl() {}
  
  public DatagramIOConfigurationImpl(int timeToLive, int maxDatagramBytes)
  {
    this.timeToLive = timeToLive;
    this.maxDatagramBytes = maxDatagramBytes;
  }
  
  public int getTimeToLive()
  {
    return this.timeToLive;
  }
  
  public void setTimeToLive(int timeToLive)
  {
    this.timeToLive = timeToLive;
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\DatagramIOConfigurationImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */