package org.fourthline.cling.model.types;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceType
{
  private static final Logger log = Logger.getLogger(DeviceType.class.getName());
  public static final String UNKNOWN = "UNKNOWN";
  public static final Pattern PATTERN = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):device:([a-zA-Z_0-9\\-]{1,64}):([0-9]+).*");
  private String namespace;
  private String type;
  private int version = 1;
  
  public DeviceType(String namespace, String type)
  {
    this(namespace, type, 1);
  }
  
  public DeviceType(String namespace, String type, int version)
  {
    if ((namespace != null) && (!namespace.matches("[a-zA-Z0-9\\-\\.]+"))) {
      throw new IllegalArgumentException("Device type namespace contains illegal characters");
    }
    this.namespace = namespace;
    if ((type != null) && (!type.matches("[a-zA-Z_0-9\\-]{1,64}"))) {
      throw new IllegalArgumentException("Device type suffix too long (64) or contains illegal characters");
    }
    this.type = type;
    
    this.version = version;
  }
  
  public String getNamespace()
  {
    return this.namespace;
  }
  
  public String getType()
  {
    return this.type;
  }
  
  public int getVersion()
  {
    return this.version;
  }
  
  public static DeviceType valueOf(String s)
    throws InvalidValueException
  {
    DeviceType deviceType = null;
    
    s = s.replaceAll("\\s", "");
    try
    {
      deviceType = UDADeviceType.valueOf(s);
    }
    catch (Exception localException) {}
    if (deviceType != null) {
      return deviceType;
    }
    try
    {
      Matcher matcher = PATTERN.matcher(s);
      if (matcher.matches()) {
        return new DeviceType(matcher.group(1), matcher.group(2), Integer.valueOf(matcher.group(3)).intValue());
      }
      matcher = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):device::([0-9]+).*").matcher(s);
      if ((matcher.matches()) && (matcher.groupCount() >= 2))
      {
        log.warning("UPnP specification violation, no device type token, defaulting to UNKNOWN: " + s);
        return new DeviceType(matcher.group(1), "UNKNOWN", Integer.valueOf(matcher.group(2)).intValue());
      }
      matcher = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):device:(.+?):([0-9]+).*").matcher(s);
      if ((matcher.matches()) && (matcher.groupCount() >= 3))
      {
        String cleanToken = matcher.group(2).replaceAll("[^a-zA-Z_0-9\\-]", "-");
        log.warning("UPnP specification violation, replacing invalid device type token '" + matcher
        
          .group(2) + "' with: " + cleanToken);
        
        return new DeviceType(matcher.group(1), cleanToken, Integer.valueOf(matcher.group(3)).intValue());
      }
    }
    catch (RuntimeException e)
    {
      throw new InvalidValueException(String.format("Can't parse device type string (namespace/type/version) '%s': %s", new Object[] { s, e
        .toString() }));
    }
    throw new InvalidValueException("Can't parse device type string (namespace/type/version): " + s);
  }
  
  public boolean implementsVersion(DeviceType that)
  {
    if (!this.namespace.equals(that.namespace)) {
      return false;
    }
    if (!this.type.equals(that.type)) {
      return false;
    }
    if (this.version < that.version) {
      return false;
    }
    return true;
  }
  
  public String getDisplayString()
  {
    return getType();
  }
  
  public String toString()
  {
    return "urn:" + getNamespace() + ":device:" + getType() + ":" + getVersion();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (!(o instanceof DeviceType))) {
      return false;
    }
    DeviceType that = (DeviceType)o;
    if (this.version != that.version) {
      return false;
    }
    if (!this.namespace.equals(that.namespace)) {
      return false;
    }
    if (!this.type.equals(that.type)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.namespace.hashCode();
    result = 31 * result + this.type.hashCode();
    result = 31 * result + this.version;
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\DeviceType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */