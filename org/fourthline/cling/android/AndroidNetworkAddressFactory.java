package org.fourthline.cling.android;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl;
import org.fourthline.cling.transport.spi.InitializationException;

public class AndroidNetworkAddressFactory
  extends NetworkAddressFactoryImpl
{
  private static final Logger log = Logger.getLogger(AndroidUpnpServiceConfiguration.class.getName());
  
  public AndroidNetworkAddressFactory(int streamListenPort)
  {
    super(streamListenPort);
  }
  
  protected boolean requiresNetworkInterface()
  {
    return false;
  }
  
  protected boolean isUsableAddress(NetworkInterface networkInterface, InetAddress address)
  {
    boolean result = super.isUsableAddress(networkInterface, address);
    if (result)
    {
      String hostName = address.getHostAddress();
      
      Field field0 = null;
      Object target = null;
      try
      {
        try
        {
          field0 = InetAddress.class.getDeclaredField("holder");
          field0.setAccessible(true);
          target = field0.get(address);
          field0 = target.getClass().getDeclaredField("hostName");
        }
        catch (NoSuchFieldException e)
        {
          field0 = InetAddress.class.getDeclaredField("hostName");
          target = address;
        }
        if ((field0 != null) && (target != null) && (hostName != null))
        {
          field0.setAccessible(true);
          field0.set(target, hostName);
        }
        else
        {
          return false;
        }
      }
      catch (Exception ex)
      {
        log.log(Level.SEVERE, "Failed injecting hostName to work around Android InetAddress DNS bug: " + address, ex);
        
        return false;
      }
    }
    return result;
  }
  
  public InetAddress getLocalAddress(NetworkInterface networkInterface, boolean isIPv6, InetAddress remoteAddress)
  {
    for (InetAddress localAddress : getInetAddresses(networkInterface))
    {
      if ((isIPv6) && ((localAddress instanceof Inet6Address))) {
        return localAddress;
      }
      if ((!isIPv6) && ((localAddress instanceof Inet4Address))) {
        return localAddress;
      }
    }
    throw new IllegalStateException("Can't find any IPv4 or IPv6 address on interface: " + networkInterface.getDisplayName());
  }
  
  protected void discoverNetworkInterfaces()
    throws InitializationException
  {
    try
    {
      super.discoverNetworkInterfaces();
    }
    catch (Exception ex)
    {
      log.warning("Exception while enumerating network interfaces, trying once more: " + ex);
      super.discoverNetworkInterfaces();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\android\AndroidNetworkAddressFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */