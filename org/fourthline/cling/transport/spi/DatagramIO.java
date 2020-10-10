package org.fourthline.cling.transport.spi;

import java.net.DatagramPacket;
import java.net.InetAddress;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.transport.Router;

public abstract interface DatagramIO<C extends DatagramIOConfiguration>
  extends Runnable
{
  public abstract void init(InetAddress paramInetAddress, Router paramRouter, DatagramProcessor paramDatagramProcessor)
    throws InitializationException;
  
  public abstract void stop();
  
  public abstract C getConfiguration();
  
  public abstract void send(OutgoingDatagramMessage paramOutgoingDatagramMessage);
  
  public abstract void send(DatagramPacket paramDatagramPacket);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\DatagramIO.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */