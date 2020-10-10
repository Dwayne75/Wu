package org.fourthline.cling.transport.spi;

import java.net.NetworkInterface;
import org.fourthline.cling.transport.Router;

public abstract interface MulticastReceiver<C extends MulticastReceiverConfiguration>
  extends Runnable
{
  public abstract void init(NetworkInterface paramNetworkInterface, Router paramRouter, NetworkAddressFactory paramNetworkAddressFactory, DatagramProcessor paramDatagramProcessor)
    throws InitializationException;
  
  public abstract void stop();
  
  public abstract C getConfiguration();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\MulticastReceiver.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */