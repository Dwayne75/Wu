package org.fourthline.cling.model.types;

public class NamedDeviceType
{
  private UDN udn;
  private DeviceType deviceType;
  
  public NamedDeviceType(UDN udn, DeviceType deviceType)
  {
    this.udn = udn;
    this.deviceType = deviceType;
  }
  
  public UDN getUdn()
  {
    return this.udn;
  }
  
  public DeviceType getDeviceType()
  {
    return this.deviceType;
  }
  
  public static NamedDeviceType valueOf(String s)
    throws InvalidValueException
  {
    String[] strings = s.split("::");
    if (strings.length != 2) {
      throw new InvalidValueException("Can't parse UDN::DeviceType from: " + s);
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
    DeviceType deviceType = DeviceType.valueOf(strings[1]);
    return new NamedDeviceType(udn, deviceType);
  }
  
  public String toString()
  {
    return getUdn().toString() + "::" + getDeviceType().toString();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (!(o instanceof NamedDeviceType))) {
      return false;
    }
    NamedDeviceType that = (NamedDeviceType)o;
    if (!this.deviceType.equals(that.deviceType)) {
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
    result = 31 * result + this.deviceType.hashCode();
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\NamedDeviceType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */