package org.fourthline.cling.transport.impl;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.NoNetworkException;
import org.seamless.util.Iterators.Synchronized;

public class NetworkAddressFactoryImpl
  implements NetworkAddressFactory
{
  public static final int DEFAULT_TCP_HTTP_LISTEN_PORT = 0;
  private static Logger log = Logger.getLogger(NetworkAddressFactoryImpl.class.getName());
  protected final Set<String> useInterfaces = new HashSet();
  protected final Set<String> useAddresses = new HashSet();
  protected final List<NetworkInterface> networkInterfaces = new ArrayList();
  protected final List<InetAddress> bindAddresses = new ArrayList();
  protected int streamListenPort;
  
  public NetworkAddressFactoryImpl()
    throws InitializationException
  {
    this(0);
  }
  
  public NetworkAddressFactoryImpl(int streamListenPort)
    throws InitializationException
  {
    System.setProperty("java.net.preferIPv4Stack", "true");
    
    String useInterfacesString = System.getProperty("org.fourthline.cling.network.useInterfaces");
    if (useInterfacesString != null)
    {
      String[] userInterfacesStrings = useInterfacesString.split(",");
      this.useInterfaces.addAll(Arrays.asList(userInterfacesStrings));
    }
    String useAddressesString = System.getProperty("org.fourthline.cling.network.useAddresses");
    if (useAddressesString != null)
    {
      String[] useAddressesStrings = useAddressesString.split(",");
      this.useAddresses.addAll(Arrays.asList(useAddressesStrings));
    }
    discoverNetworkInterfaces();
    discoverBindAddresses();
    if ((this.networkInterfaces.size() == 0) || (this.bindAddresses.size() == 0))
    {
      log.warning("No usable network interface or addresses found");
      if (requiresNetworkInterface()) {
        throw new NoNetworkException("Could not discover any usable network interfaces and/or addresses");
      }
    }
    this.streamListenPort = streamListenPort;
  }
  
  protected boolean requiresNetworkInterface()
  {
    return true;
  }
  
  public void logInterfaceInformation()
  {
    synchronized (this.networkInterfaces)
    {
      if (this.networkInterfaces.isEmpty())
      {
        log.info("No network interface to display!");
        return;
      }
      for (NetworkInterface networkInterface : this.networkInterfaces) {
        try
        {
          logInterfaceInformation(networkInterface);
        }
        catch (SocketException ex)
        {
          log.log(Level.WARNING, "Exception while logging network interface information", ex);
        }
      }
    }
  }
  
  public InetAddress getMulticastGroup()
  {
    try
    {
      return InetAddress.getByName("239.255.255.250");
    }
    catch (UnknownHostException ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public int getMulticastPort()
  {
    return 1900;
  }
  
  public int getStreamListenPort()
  {
    return this.streamListenPort;
  }
  
  public Iterator<NetworkInterface> getNetworkInterfaces()
  {
    new Iterators.Synchronized(this.networkInterfaces)
    {
      protected void synchronizedRemove(int index)
      {
        synchronized (NetworkAddressFactoryImpl.this.networkInterfaces)
        {
          NetworkAddressFactoryImpl.this.networkInterfaces.remove(index);
        }
      }
    };
  }
  
  public Iterator<InetAddress> getBindAddresses()
  {
    new Iterators.Synchronized(this.bindAddresses)
    {
      protected void synchronizedRemove(int index)
      {
        synchronized (NetworkAddressFactoryImpl.this.bindAddresses)
        {
          NetworkAddressFactoryImpl.this.bindAddresses.remove(index);
        }
      }
    };
  }
  
  public boolean hasUsableNetwork()
  {
    return (this.networkInterfaces.size() > 0) && (this.bindAddresses.size() > 0);
  }
  
  public byte[] getHardwareAddress(InetAddress inetAddress)
  {
    try
    {
      NetworkInterface iface = NetworkInterface.getByInetAddress(inetAddress);
      return iface != null ? iface.getHardwareAddress() : null;
    }
    catch (Throwable ex)
    {
      log.log(Level.WARNING, "Cannot get hardware address for: " + inetAddress, ex);
    }
    return null;
  }
  
  public InetAddress getBroadcastAddress(InetAddress inetAddress)
  {
    synchronized (this.networkInterfaces)
    {
      for (NetworkInterface iface : this.networkInterfaces) {
        for (InterfaceAddress interfaceAddress : getInterfaceAddresses(iface)) {
          if ((interfaceAddress != null) && (interfaceAddress.getAddress().equals(inetAddress))) {
            return interfaceAddress.getBroadcast();
          }
        }
      }
    }
    return null;
  }
  
  public Short getAddressNetworkPrefixLength(InetAddress inetAddress)
  {
    synchronized (this.networkInterfaces)
    {
      for (NetworkInterface iface : this.networkInterfaces) {
        for (InterfaceAddress interfaceAddress : getInterfaceAddresses(iface)) {
          if ((interfaceAddress != null) && (interfaceAddress.getAddress().equals(inetAddress)))
          {
            short prefix = interfaceAddress.getNetworkPrefixLength();
            if ((prefix > 0) && (prefix < 32)) {
              return Short.valueOf(prefix);
            }
            return null;
          }
        }
      }
    }
    return null;
  }
  
  public InetAddress getLocalAddress(NetworkInterface networkInterface, boolean isIPv6, InetAddress remoteAddress)
  {
    InetAddress localIPInSubnet = getBindAddressInSubnetOf(remoteAddress);
    if (localIPInSubnet != null) {
      return localIPInSubnet;
    }
    log.finer("Could not find local bind address in same subnet as: " + remoteAddress.getHostAddress());
    for (InetAddress interfaceAddress : getInetAddresses(networkInterface))
    {
      if ((isIPv6) && ((interfaceAddress instanceof Inet6Address))) {
        return interfaceAddress;
      }
      if ((!isIPv6) && ((interfaceAddress instanceof Inet4Address))) {
        return interfaceAddress;
      }
    }
    throw new IllegalStateException("Can't find any IPv4 or IPv6 address on interface: " + networkInterface.getDisplayName());
  }
  
  protected List<InterfaceAddress> getInterfaceAddresses(NetworkInterface networkInterface)
  {
    return networkInterface.getInterfaceAddresses();
  }
  
  protected List<InetAddress> getInetAddresses(NetworkInterface networkInterface)
  {
    return Collections.list(networkInterface.getInetAddresses());
  }
  
  protected InetAddress getBindAddressInSubnetOf(InetAddress inetAddress)
  {
    synchronized (this.networkInterfaces)
    {
      for (NetworkInterface iface : this.networkInterfaces) {
        for (InterfaceAddress ifaceAddress : getInterfaceAddresses(iface))
        {
          synchronized (this.bindAddresses)
          {
            if ((ifaceAddress == null) || (!this.bindAddresses.contains(ifaceAddress.getAddress()))) {
              continue;
            }
          }
          if (isInSubnet(inetAddress
            .getAddress(), ifaceAddress
            .getAddress().getAddress(), ifaceAddress
            .getNetworkPrefixLength())) {
            return ifaceAddress.getAddress();
          }
        }
      }
    }
    return null;
  }
  
  protected boolean isInSubnet(byte[] ip, byte[] network, short prefix)
  {
    if (ip.length != network.length) {
      return false;
    }
    if (prefix / 8 > ip.length) {
      return false;
    }
    int i = 0;
    while ((prefix >= 8) && (i < ip.length))
    {
      if (ip[i] != network[i]) {
        return false;
      }
      i++;
      prefix = (short)(prefix - 8);
    }
    if (i == ip.length) {
      return true;
    }
    byte mask = (byte)((1 << 8 - prefix) - 1 ^ 0xFFFFFFFF);
    
    return (ip[i] & mask) == (network[i] & mask);
  }
  
  protected void discoverNetworkInterfaces()
    throws InitializationException
  {
    try
    {
      Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
      for (NetworkInterface iface : Collections.list(interfaceEnumeration))
      {
        log.finer("Analyzing network interface: " + iface.getDisplayName());
        if (isUsableNetworkInterface(iface))
        {
          log.fine("Discovered usable network interface: " + iface.getDisplayName());
          synchronized (this.networkInterfaces)
          {
            this.networkInterfaces.add(iface);
          }
        }
        else
        {
          log.finer("Ignoring non-usable network interface: " + iface.getDisplayName());
        }
      }
    }
    catch (Exception ex)
    {
      throw new InitializationException("Could not not analyze local network interfaces: " + ex, ex);
    }
  }
  
  protected boolean isUsableNetworkInterface(NetworkInterface iface)
    throws Exception
  {
    if (!iface.isUp())
    {
      log.finer("Skipping network interface (down): " + iface.getDisplayName());
      return false;
    }
    if (getInetAddresses(iface).size() == 0)
    {
      log.finer("Skipping network interface without bound IP addresses: " + iface.getDisplayName());
      return false;
    }
    if ((iface.getName().toLowerCase(Locale.ROOT).startsWith("vmnet")) || (
      (iface.getDisplayName() != null) && (iface.getDisplayName().toLowerCase(Locale.ROOT).contains("vmnet"))))
    {
      log.finer("Skipping network interface (VMWare): " + iface.getDisplayName());
      return false;
    }
    if (iface.getName().toLowerCase(Locale.ROOT).startsWith("vnic"))
    {
      log.finer("Skipping network interface (Parallels): " + iface.getDisplayName());
      return false;
    }
    if (iface.getName().toLowerCase(Locale.ROOT).startsWith("vboxnet"))
    {
      log.finer("Skipping network interface (Virtual Box): " + iface.getDisplayName());
      return false;
    }
    if (iface.getName().toLowerCase(Locale.ROOT).contains("virtual"))
    {
      log.finer("Skipping network interface (named '*virtual*'): " + iface.getDisplayName());
      return false;
    }
    if (iface.getName().toLowerCase(Locale.ROOT).startsWith("ppp"))
    {
      log.finer("Skipping network interface (PPP): " + iface.getDisplayName());
      return false;
    }
    if (iface.isLoopback())
    {
      log.finer("Skipping network interface (ignoring loopback): " + iface.getDisplayName());
      return false;
    }
    if ((this.useInterfaces.size() > 0) && (!this.useInterfaces.contains(iface.getName())))
    {
      log.finer("Skipping unwanted network interface (-Dorg.fourthline.cling.network.useInterfaces): " + iface.getName());
      return false;
    }
    if (!iface.supportsMulticast()) {
      log.warning("Network interface may not be multicast capable: " + iface.getDisplayName());
    }
    return true;
  }
  
  protected void discoverBindAddresses()
    throws InitializationException
  {
    try
    {
      synchronized (this.networkInterfaces)
      {
        Iterator<NetworkInterface> it = this.networkInterfaces.iterator();
        while (it.hasNext())
        {
          NetworkInterface networkInterface = (NetworkInterface)it.next();
          
          log.finer("Discovering addresses of interface: " + networkInterface.getDisplayName());
          int usableAddresses = 0;
          for (InetAddress inetAddress : getInetAddresses(networkInterface)) {
            if (inetAddress == null)
            {
              log.warning("Network has a null address: " + networkInterface.getDisplayName());
            }
            else if (isUsableAddress(networkInterface, inetAddress))
            {
              log.fine("Discovered usable network interface address: " + inetAddress.getHostAddress());
              usableAddresses++;
              synchronized (this.bindAddresses)
              {
                this.bindAddresses.add(inetAddress);
              }
            }
            else
            {
              log.finer("Ignoring non-usable network interface address: " + inetAddress.getHostAddress());
            }
          }
          if (usableAddresses == 0)
          {
            log.finer("Network interface has no usable addresses, removing: " + networkInterface.getDisplayName());
            it.remove();
          }
        }
      }
    }
    catch (Exception ex)
    {
      throw new InitializationException("Could not not analyze local network interfaces: " + ex, ex);
    }
  }
  
  protected boolean isUsableAddress(NetworkInterface networkInterface, InetAddress address)
  {
    if (!(address instanceof Inet4Address))
    {
      log.finer("Skipping unsupported non-IPv4 address: " + address);
      return false;
    }
    if (address.isLoopbackAddress())
    {
      log.finer("Skipping loopback address: " + address);
      return false;
    }
    if ((this.useAddresses.size() > 0) && (!this.useAddresses.contains(address.getHostAddress())))
    {
      log.finer("Skipping unwanted address: " + address);
      return false;
    }
    return true;
  }
  
  protected void logInterfaceInformation(NetworkInterface networkInterface)
    throws SocketException
  {
    log.info("---------------------------------------------------------------------------------");
    log.info(String.format("Interface display name: %s", new Object[] { networkInterface.getDisplayName() }));
    if (networkInterface.getParent() != null) {
      log.info(String.format("Parent Info: %s", new Object[] { networkInterface.getParent() }));
    }
    log.info(String.format("Name: %s", new Object[] { networkInterface.getName() }));
    
    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
    for (Iterator localIterator = Collections.list(inetAddresses).iterator(); localIterator.hasNext();)
    {
      inetAddress = (InetAddress)localIterator.next();
      log.info(String.format("InetAddress: %s", new Object[] { inetAddress }));
    }
    Object interfaceAddresses = networkInterface.getInterfaceAddresses();
    for (InetAddress inetAddress = ((List)interfaceAddresses).iterator(); inetAddress.hasNext();)
    {
      interfaceAddress = (InterfaceAddress)inetAddress.next();
      if (interfaceAddress == null)
      {
        log.warning("Skipping null InterfaceAddress!");
      }
      else
      {
        log.info(" Interface Address");
        log.info("  Address: " + interfaceAddress.getAddress());
        log.info("  Broadcast: " + interfaceAddress.getBroadcast());
        log.info("  Prefix length: " + interfaceAddress.getNetworkPrefixLength());
      }
    }
    InterfaceAddress interfaceAddress;
    Enumeration<NetworkInterface> subIfs = networkInterface.getSubInterfaces();
    for (NetworkInterface subIf : Collections.list(subIfs)) {
      if (subIf == null)
      {
        log.warning("Skipping null NetworkInterface sub-interface");
      }
      else
      {
        log.info(String.format("\tSub Interface Display name: %s", new Object[] { subIf.getDisplayName() }));
        log.info(String.format("\tSub Interface Name: %s", new Object[] { subIf.getName() }));
      }
    }
    log.info(String.format("Up? %s", new Object[] { Boolean.valueOf(networkInterface.isUp()) }));
    log.info(String.format("Loopback? %s", new Object[] { Boolean.valueOf(networkInterface.isLoopback()) }));
    log.info(String.format("PointToPoint? %s", new Object[] { Boolean.valueOf(networkInterface.isPointToPoint()) }));
    log.info(String.format("Supports multicast? %s", new Object[] { Boolean.valueOf(networkInterface.supportsMulticast()) }));
    log.info(String.format("Virtual? %s", new Object[] { Boolean.valueOf(networkInterface.isVirtual()) }));
    log.info(String.format("Hardware address: %s", new Object[] { Arrays.toString(networkInterface.getHardwareAddress()) }));
    log.info(String.format("MTU: %s", new Object[] { Integer.valueOf(networkInterface.getMTU()) }));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\NetworkAddressFactoryImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */