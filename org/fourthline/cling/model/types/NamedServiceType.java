package org.fourthline.cling.model.types;

public class NamedServiceType
{
  private UDN udn;
  private ServiceType serviceType;
  
  public NamedServiceType(UDN udn, ServiceType serviceType)
  {
    this.udn = udn;
    this.serviceType = serviceType;
  }
  
  public UDN getUdn()
  {
    return this.udn;
  }
  
  public ServiceType getServiceType()
  {
    return this.serviceType;
  }
  
  public static NamedServiceType valueOf(String s)
    throws InvalidValueException
  {
    String[] strings = s.split("::");
    if (strings.length != 2) {
      throw new InvalidValueException("Can't parse UDN::ServiceType from: " + s);
    }
    try
    {
      udn = UDN.valueOf(strings[0]);
    }
    catch (Exception ex)
    {
      UDN udn;
      throw new InvalidValueException("Can't parse UDN: " + strings[0]);
    }
    UDN udn;
    ServiceType serviceType = ServiceType.valueOf(strings[1]);
    return new NamedServiceType(udn, serviceType);
  }
  
  public String toString()
  {
    return getUdn().toString() + "::" + getServiceType().toString();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (!(o instanceof NamedServiceType))) {
      return false;
    }
    NamedServiceType that = (NamedServiceType)o;
    if (!this.serviceType.equals(that.serviceType)) {
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
    result = 31 * result + this.serviceType.hashCode();
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\NamedServiceType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */