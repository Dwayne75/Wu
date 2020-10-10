package org.fourthline.cling.transport;

import java.net.InetAddress;
import java.util.List;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.UpnpStream;

public abstract interface Router
{
  public abstract UpnpServiceConfiguration getConfiguration();
  
  public abstract ProtocolFactory getProtocolFactory();
  
  public abstract boolean enable()
    throws RouterException;
  
  public abstract boolean disable()
    throws RouterException;
  
  public abstract void shutdown()
    throws RouterException;
  
  public abstract boolean isEnabled()
    throws RouterException;
  
  public abstract void handleStartFailure(InitializationException paramInitializationException)
    throws InitializationException;
  
  public abstract List<NetworkAddress> getActiveStreamServers(InetAddress paramInetAddress)
    throws RouterException;
  
  public abstract void received(IncomingDatagramMessage paramIncomingDatagramMessage);
  
  public abstract void received(UpnpStream paramUpnpStream);
  
  public abstract void send(OutgoingDatagramMessage paramOutgoingDatagramMessage)
    throws RouterException;
  
  public abstract StreamResponseMessage send(StreamRequestMessage paramStreamRequestMessage)
    throws RouterException;
  
  public abstract void broadcast(byte[] paramArrayOfByte)
    throws RouterException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\Router.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */