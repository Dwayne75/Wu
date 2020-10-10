package org.fourthline.cling.transport.spi;

import java.net.InetAddress;

public abstract interface MulticastReceiverConfiguration
{
  public abstract InetAddress getGroup();
  
  public abstract int getPort();
  
  public abstract int getMaxDatagramBytes();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\MulticastReceiverConfiguration.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */