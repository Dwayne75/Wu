package org.fourthline.cling.transport.impl;

import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.logging.Logger;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.MulticastReceiver;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;

public class MulticastReceiverImpl
  implements MulticastReceiver<MulticastReceiverConfigurationImpl>
{
  private static Logger log = Logger.getLogger(MulticastReceiver.class.getName());
  protected final MulticastReceiverConfigurationImpl configuration;
  protected Router router;
  protected NetworkAddressFactory networkAddressFactory;
  protected DatagramProcessor datagramProcessor;
  protected NetworkInterface multicastInterface;
  protected InetSocketAddress multicastAddress;
  protected MulticastSocket socket;
  
  public MulticastReceiverImpl(MulticastReceiverConfigurationImpl configuration)
  {
    this.configuration = configuration;
  }
  
  public MulticastReceiverConfigurationImpl getConfiguration()
  {
    return this.configuration;
  }
  
  public synchronized void init(NetworkInterface networkInterface, Router router, NetworkAddressFactory networkAddressFactory, DatagramProcessor datagramProcessor)
    throws InitializationException
  {
    this.router = router;
    this.networkAddressFactory = networkAddressFactory;
    this.datagramProcessor = datagramProcessor;
    this.multicastInterface = networkInterface;
    try
    {
      log.info("Creating wildcard socket (for receiving multicast datagrams) on port: " + this.configuration.getPort());
      this.multicastAddress = new InetSocketAddress(this.configuration.getGroup(), this.configuration.getPort());
      
      this.socket = new MulticastSocket(this.configuration.getPort());
      this.socket.setReuseAddress(true);
      this.socket.setReceiveBufferSize(32768);
      
      log.info("Joining multicast group: " + this.multicastAddress + " on network interface: " + this.multicastInterface.getDisplayName());
      this.socket.joinGroup(this.multicastAddress, this.multicastInterface);
    }
    catch (Exception ex)
    {
      throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex);
    }
  }
  
  public synchronized void stop()
  {
    if ((this.socket != null) && (!this.socket.isClosed()))
    {
      try
      {
        log.fine("Leaving multicast group");
        this.socket.leaveGroup(this.multicastAddress, this.multicastInterface);
      }
      catch (Exception ex)
      {
        log.fine("Could not leave multicast group: " + ex);
      }
      this.socket.close();
    }
  }
  
  public void run()
  {
    log.fine("Entering blocking receiving loop, listening for UDP datagrams on: " + this.socket.getLocalAddress());
    try
    {
      for (;;)
      {
        byte[] buf = new byte[getConfiguration().getMaxDatagramBytes()];
        DatagramPacket datagram = new DatagramPacket(buf, buf.length);
        
        this.socket.receive(datagram);
        
        InetAddress receivedOnLocalAddress = this.networkAddressFactory.getLocalAddress(this.multicastInterface, this.multicastAddress
        
          .getAddress() instanceof Inet6Address, datagram
          .getAddress());
        
        log.fine("UDP datagram received from: " + datagram
          .getAddress().getHostAddress() + ":" + datagram
          .getPort() + " on local interface: " + this.multicastInterface
          .getDisplayName() + " and address: " + receivedOnLocalAddress
          .getHostAddress());
        
        this.router.received(this.datagramProcessor.read(receivedOnLocalAddress, datagram));
      }
    }
    catch (SocketException ex)
    {
      log.fine("Socket closed");
    }
    catch (UnsupportedDataException ex)
    {
      for (;;)
      {
        log.info("Could not read datagram: " + ex.getMessage());
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    try
    {
      if (!this.socket.isClosed())
      {
        log.fine("Closing multicast socket");
        this.socket.close();
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\MulticastReceiverImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */