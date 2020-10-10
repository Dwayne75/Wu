package org.fourthline.cling.transport.impl;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.InitializationException;

public class DatagramIOImpl
  implements DatagramIO<DatagramIOConfigurationImpl>
{
  private static Logger log = Logger.getLogger(DatagramIO.class.getName());
  protected final DatagramIOConfigurationImpl configuration;
  protected Router router;
  protected DatagramProcessor datagramProcessor;
  protected InetSocketAddress localAddress;
  protected MulticastSocket socket;
  
  public DatagramIOImpl(DatagramIOConfigurationImpl configuration)
  {
    this.configuration = configuration;
  }
  
  public DatagramIOConfigurationImpl getConfiguration()
  {
    return this.configuration;
  }
  
  public synchronized void init(InetAddress bindAddress, Router router, DatagramProcessor datagramProcessor)
    throws InitializationException
  {
    this.router = router;
    this.datagramProcessor = datagramProcessor;
    try
    {
      log.info("Creating bound socket (for datagram input/output) on: " + bindAddress);
      this.localAddress = new InetSocketAddress(bindAddress, 0);
      this.socket = new MulticastSocket(this.localAddress);
      this.socket.setTimeToLive(this.configuration.getTimeToLive());
      this.socket.setReceiveBufferSize(262144);
    }
    catch (Exception ex)
    {
      throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex);
    }
  }
  
  public synchronized void stop()
  {
    if ((this.socket != null) && (!this.socket.isClosed())) {
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
        
        log.fine("UDP datagram received from: " + datagram
        
          .getAddress().getHostAddress() + ":" + datagram
          .getPort() + " on: " + this.localAddress);
        
        this.router.received(this.datagramProcessor.read(this.localAddress.getAddress(), datagram));
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
        log.fine("Closing unicast socket");
        this.socket.close();
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public synchronized void send(OutgoingDatagramMessage message)
  {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Sending message from address: " + this.localAddress);
    }
    DatagramPacket packet = this.datagramProcessor.write(message);
    if (log.isLoggable(Level.FINE)) {
      log.fine("Sending UDP datagram packet to: " + message.getDestinationAddress() + ":" + message.getDestinationPort());
    }
    send(packet);
  }
  
  public synchronized void send(DatagramPacket datagram)
  {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Sending message from address: " + this.localAddress);
    }
    try
    {
      this.socket.send(datagram);
    }
    catch (SocketException ex)
    {
      log.fine("Socket closed, aborting datagram send to: " + datagram.getAddress());
    }
    catch (RuntimeException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "Exception sending datagram to: " + datagram.getAddress() + ": " + ex, ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\DatagramIOImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */