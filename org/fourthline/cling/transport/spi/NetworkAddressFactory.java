package org.fourthline.cling.transport.spi;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Iterator;

public abstract interface NetworkAddressFactory
{
  public static final String SYSTEM_PROPERTY_NET_IFACES = "org.fourthline.cling.network.useInterfaces";
  public static final String SYSTEM_PROPERTY_NET_ADDRESSES = "org.fourthline.cling.network.useAddresses";
  
  public abstract InetAddress getMulticastGroup();
  
  public abstract int getMulticastPort();
  
  public abstract int getStreamListenPort();
  
  public abstract Iterator<NetworkInterface> getNetworkInterfaces();
  
  public abstract Iterator<InetAddress> getBindAddresses();
  
  public abstract boolean hasUsableNetwork();
  
  public abstract Short getAddressNetworkPrefixLength(InetAddress paramInetAddress);
  
  public abstract byte[] getHardwareAddress(InetAddress paramInetAddress);
  
  public abstract InetAddress getBroadcastAddress(InetAddress paramInetAddress);
  
  public abstract InetAddress getLocalAddress(NetworkInterface paramNetworkInterface, boolean paramBoolean, InetAddress paramInetAddress)
    throws IllegalStateException;
  
  public abstract void logInterfaceInformation();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\NetworkAddressFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */