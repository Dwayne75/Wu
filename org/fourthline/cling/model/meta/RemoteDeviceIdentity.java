package org.fourthline.cling.model.meta;

import java.net.InetAddress;
import java.net.URL;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.message.discovery.IncomingNotificationRequest;
import org.fourthline.cling.model.message.discovery.IncomingSearchResponse;
import org.fourthline.cling.model.types.UDN;

public class RemoteDeviceIdentity
  extends DeviceIdentity
{
  private final URL descriptorURL;
  private final byte[] interfaceMacAddress;
  private final InetAddress discoveredOnLocalAddress;
  
  public RemoteDeviceIdentity(UDN udn, RemoteDeviceIdentity template)
  {
    this(udn, template.getMaxAgeSeconds(), template.getDescriptorURL(), template.getInterfaceMacAddress(), template.getDiscoveredOnLocalAddress());
  }
  
  public RemoteDeviceIdentity(UDN udn, Integer maxAgeSeconds, URL descriptorURL, byte[] interfaceMacAddress, InetAddress discoveredOnLocalAddress)
  {
    super(udn, maxAgeSeconds);
    this.descriptorURL = descriptorURL;
    this.interfaceMacAddress = interfaceMacAddress;
    this.discoveredOnLocalAddress = discoveredOnLocalAddress;
  }
  
  public RemoteDeviceIdentity(IncomingNotificationRequest notificationRequest)
  {
    this(notificationRequest.getUDN(), notificationRequest
      .getMaxAge(), notificationRequest
      .getLocationURL(), notificationRequest
      .getInterfaceMacHeader(), notificationRequest
      .getLocalAddress());
  }
  
  public RemoteDeviceIdentity(IncomingSearchResponse searchResponse)
  {
    this(searchResponse.getRootDeviceUDN(), searchResponse
      .getMaxAge(), searchResponse
      .getLocationURL(), searchResponse
      .getInterfaceMacHeader(), searchResponse
      .getLocalAddress());
  }
  
  public URL getDescriptorURL()
  {
    return this.descriptorURL;
  }
  
  public byte[] getInterfaceMacAddress()
  {
    return this.interfaceMacAddress;
  }
  
  public InetAddress getDiscoveredOnLocalAddress()
  {
    return this.discoveredOnLocalAddress;
  }
  
  public byte[] getWakeOnLANBytes()
  {
    if (getInterfaceMacAddress() == null) {
      return null;
    }
    byte[] bytes = new byte[6 + 16 * getInterfaceMacAddress().length];
    for (int i = 0; i < 6; i++) {
      bytes[i] = -1;
    }
    for (int i = 6; i < bytes.length; i += getInterfaceMacAddress().length) {
      System.arraycopy(getInterfaceMacAddress(), 0, bytes, i, getInterfaceMacAddress().length);
    }
    return bytes;
  }
  
  public String toString()
  {
    if (ModelUtil.ANDROID_RUNTIME) {
      return "(RemoteDeviceIdentity) UDN: " + getUdn() + ", Descriptor: " + getDescriptorURL();
    }
    return "(" + getClass().getSimpleName() + ") UDN: " + getUdn() + ", Descriptor: " + getDescriptorURL();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\RemoteDeviceIdentity.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */