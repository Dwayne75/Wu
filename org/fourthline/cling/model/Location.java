package org.fourthline.cling.model;

import java.net.InetAddress;
import java.net.URL;

public class Location
{
  protected final NetworkAddress networkAddress;
  protected final String path;
  protected final URL url;
  
  public Location(NetworkAddress networkAddress, String path)
  {
    this.networkAddress = networkAddress;
    this.path = path;
    this.url = createAbsoluteURL(networkAddress.getAddress(), networkAddress.getPort(), path);
  }
  
  public NetworkAddress getNetworkAddress()
  {
    return this.networkAddress;
  }
  
  public String getPath()
  {
    return this.path;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Location location = (Location)o;
    if (!this.networkAddress.equals(location.networkAddress)) {
      return false;
    }
    if (!this.path.equals(location.path)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.networkAddress.hashCode();
    result = 31 * result + this.path.hashCode();
    return result;
  }
  
  public URL getURL()
  {
    return this.url;
  }
  
  private static URL createAbsoluteURL(InetAddress address, int localStreamPort, String path)
  {
    try
    {
      return new URL("http", address.getHostAddress(), localStreamPort, path);
    }
    catch (Exception ex)
    {
      throw new IllegalArgumentException("Address, port, and URI can not be converted to URL", ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\Location.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */