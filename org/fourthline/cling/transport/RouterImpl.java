package org.fourthline.cling.transport;

import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.protocol.ProtocolCreationException;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ReceivingAsync;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.MulticastReceiver;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.NoNetworkException;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;
import org.fourthline.cling.transport.spi.UpnpStream;
import org.seamless.util.Exceptions;

@ApplicationScoped
public class RouterImpl
  implements Router
{
  private static Logger log = Logger.getLogger(Router.class.getName());
  protected UpnpServiceConfiguration configuration;
  protected ProtocolFactory protocolFactory;
  protected volatile boolean enabled;
  protected ReentrantReadWriteLock routerLock = new ReentrantReadWriteLock(true);
  protected Lock readLock = this.routerLock.readLock();
  protected Lock writeLock = this.routerLock.writeLock();
  protected NetworkAddressFactory networkAddressFactory;
  protected StreamClient streamClient;
  protected final Map<NetworkInterface, MulticastReceiver> multicastReceivers = new HashMap();
  protected final Map<InetAddress, DatagramIO> datagramIOs = new HashMap();
  protected final Map<InetAddress, StreamServer> streamServers = new HashMap();
  
  protected RouterImpl() {}
  
  @Inject
  public RouterImpl(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory)
  {
    log.info("Creating Router: " + getClass().getName());
    this.configuration = configuration;
    this.protocolFactory = protocolFactory;
  }
  
  public boolean enable(@Observes @Default EnableRouter event)
    throws RouterException
  {
    return enable();
  }
  
  public boolean disable(@Observes @Default DisableRouter event)
    throws RouterException
  {
    return disable();
  }
  
  public UpnpServiceConfiguration getConfiguration()
  {
    return this.configuration;
  }
  
  public ProtocolFactory getProtocolFactory()
  {
    return this.protocolFactory;
  }
  
  public boolean enable()
    throws RouterException
  {
    lock(this.writeLock);
    try
    {
      if (!this.enabled) {
        try
        {
          log.fine("Starting networking services...");
          this.networkAddressFactory = getConfiguration().createNetworkAddressFactory();
          
          startInterfaceBasedTransports(this.networkAddressFactory.getNetworkInterfaces());
          startAddressBasedTransports(this.networkAddressFactory.getBindAddresses());
          if (!this.networkAddressFactory.hasUsableNetwork()) {
            throw new NoNetworkException("No usable network interface and/or addresses available, check the log for errors.");
          }
          this.streamClient = getConfiguration().createStreamClient();
          
          this.enabled = true;
          return true;
        }
        catch (InitializationException ex)
        {
          handleStartFailure(ex);
        }
      }
      return false;
    }
    finally
    {
      unlock(this.writeLock);
    }
  }
  
  public boolean disable()
    throws RouterException
  {
    lock(this.writeLock);
    try
    {
      boolean bool;
      if (this.enabled)
      {
        log.fine("Disabling network services...");
        if (this.streamClient != null)
        {
          log.fine("Stopping stream client connection management/pool");
          this.streamClient.stop();
          this.streamClient = null;
        }
        for (Map.Entry<InetAddress, StreamServer> entry : this.streamServers.entrySet())
        {
          log.fine("Stopping stream server on address: " + entry.getKey());
          ((StreamServer)entry.getValue()).stop();
        }
        this.streamServers.clear();
        for (Map.Entry<NetworkInterface, MulticastReceiver> entry : this.multicastReceivers.entrySet())
        {
          log.fine("Stopping multicast receiver on interface: " + ((NetworkInterface)entry.getKey()).getDisplayName());
          ((MulticastReceiver)entry.getValue()).stop();
        }
        this.multicastReceivers.clear();
        for (Map.Entry<InetAddress, DatagramIO> entry : this.datagramIOs.entrySet())
        {
          log.fine("Stopping datagram I/O on address: " + entry.getKey());
          ((DatagramIO)entry.getValue()).stop();
        }
        this.datagramIOs.clear();
        
        this.networkAddressFactory = null;
        this.enabled = false;
        return true;
      }
      return false;
    }
    finally
    {
      unlock(this.writeLock);
    }
  }
  
  public void shutdown()
    throws RouterException
  {
    disable();
  }
  
  public boolean isEnabled()
  {
    return this.enabled;
  }
  
  public void handleStartFailure(InitializationException ex)
    throws InitializationException
  {
    if ((ex instanceof NoNetworkException))
    {
      log.info("Unable to initialize network router, no network found.");
    }
    else
    {
      log.severe("Unable to initialize network router: " + ex);
      log.severe("Cause: " + Exceptions.unwrap(ex));
    }
  }
  
  public List<NetworkAddress> getActiveStreamServers(InetAddress preferredAddress)
    throws RouterException
  {
    lock(this.readLock);
    try
    {
      List<NetworkAddress> streamServerAddresses;
      if ((this.enabled) && (this.streamServers.size() > 0))
      {
        streamServerAddresses = new ArrayList();
        StreamServer preferredServer;
        if ((preferredAddress != null) && 
          ((preferredServer = (StreamServer)this.streamServers.get(preferredAddress)) != null))
        {
          streamServerAddresses.add(new NetworkAddress(preferredAddress, preferredServer
          
            .getPort(), this.networkAddressFactory
            .getHardwareAddress(preferredAddress)));
          
          return streamServerAddresses;
        }
        for (Object localObject1 = this.streamServers.entrySet().iterator(); ((Iterator)localObject1).hasNext();)
        {
          Map.Entry<InetAddress, StreamServer> entry = (Map.Entry)((Iterator)localObject1).next();
          byte[] hardwareAddress = this.networkAddressFactory.getHardwareAddress((InetAddress)entry.getKey());
          streamServerAddresses.add(new NetworkAddress(
            (InetAddress)entry.getKey(), ((StreamServer)entry.getValue()).getPort(), hardwareAddress));
        }
        return streamServerAddresses;
      }
      return Collections.EMPTY_LIST;
    }
    finally
    {
      unlock(this.readLock);
    }
  }
  
  public void received(IncomingDatagramMessage msg)
  {
    if (!this.enabled)
    {
      log.fine("Router disabled, ignoring incoming message: " + msg);
      return;
    }
    try
    {
      ReceivingAsync protocol = getProtocolFactory().createReceivingAsync(msg);
      if (protocol == null)
      {
        if (log.isLoggable(Level.FINEST)) {
          log.finest("No protocol, ignoring received message: " + msg);
        }
        return;
      }
      if (log.isLoggable(Level.FINE)) {
        log.fine("Received asynchronous message: " + msg);
      }
      getConfiguration().getAsyncProtocolExecutor().execute(protocol);
    }
    catch (ProtocolCreationException ex)
    {
      log.warning("Handling received datagram failed - " + Exceptions.unwrap(ex).toString());
    }
  }
  
  public void received(UpnpStream stream)
  {
    if (!this.enabled)
    {
      log.fine("Router disabled, ignoring incoming: " + stream);
      return;
    }
    log.fine("Received synchronous stream: " + stream);
    getConfiguration().getSyncProtocolExecutorService().execute(stream);
  }
  
  public void send(OutgoingDatagramMessage msg)
    throws RouterException
  {
    lock(this.readLock);
    try
    {
      if (this.enabled) {
        for (DatagramIO datagramIO : this.datagramIOs.values()) {
          datagramIO.send(msg);
        }
      } else {
        log.fine("Router disabled, not sending datagram: " + msg);
      }
    }
    finally
    {
      unlock(this.readLock);
    }
  }
  
  public StreamResponseMessage send(StreamRequestMessage msg)
    throws RouterException
  {
    lock(this.readLock);
    try
    {
      if (this.enabled)
      {
        StreamResponseMessage localStreamResponseMessage;
        if (this.streamClient == null)
        {
          log.fine("No StreamClient available, not sending: " + msg);
          return null;
        }
        log.fine("Sending via TCP unicast stream: " + msg);
        try
        {
          return this.streamClient.sendRequest(msg);
        }
        catch (InterruptedException ex)
        {
          throw new RouterException("Sending stream request was interrupted", ex);
        }
      }
      log.fine("Router disabled, not sending stream request: " + msg);
      return null;
    }
    finally
    {
      unlock(this.readLock);
    }
  }
  
  public void broadcast(byte[] bytes)
    throws RouterException
  {
    lock(this.readLock);
    try
    {
      if (this.enabled) {
        for (Map.Entry<InetAddress, DatagramIO> entry : this.datagramIOs.entrySet())
        {
          InetAddress broadcast = this.networkAddressFactory.getBroadcastAddress((InetAddress)entry.getKey());
          if (broadcast != null)
          {
            log.fine("Sending UDP datagram to broadcast address: " + broadcast.getHostAddress());
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcast, 9);
            ((DatagramIO)entry.getValue()).send(packet);
          }
        }
      } else {
        log.fine("Router disabled, not broadcasting bytes: " + bytes.length);
      }
    }
    finally
    {
      unlock(this.readLock);
    }
  }
  
  protected void startInterfaceBasedTransports(Iterator<NetworkInterface> interfaces)
    throws InitializationException
  {
    NetworkInterface networkInterface;
    while (interfaces.hasNext())
    {
      networkInterface = (NetworkInterface)interfaces.next();
      
      MulticastReceiver multicastReceiver = getConfiguration().createMulticastReceiver(this.networkAddressFactory);
      if (multicastReceiver == null) {
        log.info("Configuration did not create a MulticastReceiver for: " + networkInterface);
      } else {
        try
        {
          if (log.isLoggable(Level.FINE)) {
            log.fine("Init multicast receiver on interface: " + networkInterface.getDisplayName());
          }
          multicastReceiver.init(networkInterface, this, this.networkAddressFactory, 
          
            getConfiguration().getDatagramProcessor());
          
          this.multicastReceivers.put(networkInterface, multicastReceiver);
        }
        catch (InitializationException ex)
        {
          throw ex;
        }
      }
    }
    for (Map.Entry<NetworkInterface, MulticastReceiver> entry : this.multicastReceivers.entrySet())
    {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Starting multicast receiver on interface: " + ((NetworkInterface)entry.getKey()).getDisplayName());
      }
      getConfiguration().getMulticastReceiverExecutor().execute((Runnable)entry.getValue());
    }
  }
  
  protected void startAddressBasedTransports(Iterator<InetAddress> addresses)
    throws InitializationException
  {
    InetAddress address;
    while (addresses.hasNext())
    {
      address = (InetAddress)addresses.next();
      
      StreamServer streamServer = getConfiguration().createStreamServer(this.networkAddressFactory);
      if (streamServer == null) {
        log.info("Configuration did not create a StreamServer for: " + address);
      } else {
        try
        {
          if (log.isLoggable(Level.FINE)) {
            log.fine("Init stream server on address: " + address);
          }
          streamServer.init(address, this);
          this.streamServers.put(address, streamServer);
        }
        catch (InitializationException ex)
        {
          Throwable cause = Exceptions.unwrap(ex);
          if ((cause instanceof BindException))
          {
            log.warning("Failed to init StreamServer: " + cause);
            if (log.isLoggable(Level.FINE)) {
              log.log(Level.FINE, "Initialization exception root cause", cause);
            }
            log.warning("Removing unusable address: " + address);
            addresses.remove();
            continue;
          }
          throw ex;
        }
      }
      DatagramIO datagramIO = getConfiguration().createDatagramIO(this.networkAddressFactory);
      if (datagramIO == null) {
        log.info("Configuration did not create a StreamServer for: " + address);
      } else {
        try
        {
          if (log.isLoggable(Level.FINE)) {
            log.fine("Init datagram I/O on address: " + address);
          }
          datagramIO.init(address, this, getConfiguration().getDatagramProcessor());
          this.datagramIOs.put(address, datagramIO);
        }
        catch (InitializationException ex)
        {
          throw ex;
        }
      }
    }
    for (Map.Entry<InetAddress, StreamServer> entry : this.streamServers.entrySet())
    {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Starting stream server on address: " + entry.getKey());
      }
      getConfiguration().getStreamServerExecutorService().execute((Runnable)entry.getValue());
    }
    for (Map.Entry<InetAddress, DatagramIO> entry : this.datagramIOs.entrySet())
    {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Starting datagram I/O on address: " + entry.getKey());
      }
      getConfiguration().getDatagramIOExecutor().execute((Runnable)entry.getValue());
    }
  }
  
  protected void lock(Lock lock, int timeoutMilliseconds)
    throws RouterException
  {
    try
    {
      log.finest("Trying to obtain lock with timeout milliseconds '" + timeoutMilliseconds + "': " + lock.getClass().getSimpleName());
      if (lock.tryLock(timeoutMilliseconds, TimeUnit.MILLISECONDS)) {
        log.finest("Acquired router lock: " + lock.getClass().getSimpleName());
      } else {
        throw new RouterException("Router wasn't available exclusively after waiting " + timeoutMilliseconds + "ms, lock failed: " + lock.getClass().getSimpleName());
      }
    }
    catch (InterruptedException ex)
    {
      throw new RouterException("Interruption while waiting for exclusive access: " + lock.getClass().getSimpleName(), ex);
    }
  }
  
  protected void lock(Lock lock)
    throws RouterException
  {
    lock(lock, getLockTimeoutMillis());
  }
  
  protected void unlock(Lock lock)
  {
    log.finest("Releasing router lock: " + lock.getClass().getSimpleName());
    lock.unlock();
  }
  
  protected int getLockTimeoutMillis()
  {
    return 6000;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\RouterImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */