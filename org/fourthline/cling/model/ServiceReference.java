package org.fourthline.cling.model;

import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDN;

public class ServiceReference
{
  public static final String DELIMITER = "/";
  private final UDN udn;
  private final ServiceId serviceId;
  
  public ServiceReference(String s)
  {
    String[] split = s.split("/");
    if (split.length == 2)
    {
      this.udn = UDN.valueOf(split[0]);
      this.serviceId = ServiceId.valueOf(split[1]);
    }
    else
    {
      this.udn = null;
      this.serviceId = null;
    }
  }
  
  public ServiceReference(UDN udn, ServiceId serviceId)
  {
    this.udn = udn;
    this.serviceId = serviceId;
  }
  
  public UDN getUdn()
  {
    return this.udn;
  }
  
  public ServiceId getServiceId()
  {
    return this.serviceId;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    ServiceReference that = (ServiceReference)o;
    if (!this.serviceId.equals(that.serviceId)) {
      return false;
    }
    if (!this.udn.equals(that.udn)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.udn.hashCode();
    result = 31 * result + this.serviceId.hashCode();
    return result;
  }
  
  public String toString()
  {
    return this.udn.toString() + "/" + this.serviceId.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\ServiceReference.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */