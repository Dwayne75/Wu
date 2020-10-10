package org.fourthline.cling.model.types;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceType
{
  private static final Logger log = Logger.getLogger(ServiceType.class.getName());
  public static final Pattern PATTERN = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):service:([a-zA-Z_0-9\\-]{1,64}):([0-9]+).*");
  public static final Pattern BROKEN_PATTERN = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):serviceId:([a-zA-Z_0-9\\-]{1,64}):([0-9]+).*");
  private String namespace;
  private String type;
  private int version = 1;
  
  public ServiceType(String namespace, String type)
  {
    this(namespace, type, 1);
  }
  
  public ServiceType(String namespace, String type, int version)
  {
    if ((namespace != null) && (!namespace.matches("[a-zA-Z0-9\\-\\.]+"))) {
      throw new IllegalArgumentException("Service type namespace contains illegal characters");
    }
    this.namespace = namespace;
    if ((type != null) && (!type.matches("[a-zA-Z_0-9\\-]{1,64}"))) {
      throw new IllegalArgumentException("Service type suffix too long (64) or contains illegal characters");
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
  
  public static ServiceType valueOf(String s)
    throws InvalidValueException
  {
    if (s == null) {
      throw new InvalidValueException("Can't parse null string");
    }
    ServiceType serviceType = null;
    
    s = s.replaceAll("\\s", "");
    try
    {
      serviceType = UDAServiceType.valueOf(s);
    }
    catch (Exception localException) {}
    if (serviceType != null) {
      return serviceType;
    }
    try
    {
      Matcher matcher = PATTERN.matcher(s);
      if ((matcher.matches()) && (matcher.groupCount() >= 3)) {
        return new ServiceType(matcher.group(1), matcher.group(2), Integer.valueOf(matcher.group(3)).intValue());
      }
      matcher = BROKEN_PATTERN.matcher(s);
      if ((matcher.matches()) && (matcher.groupCount() >= 3)) {
        return new ServiceType(matcher.group(1), matcher.group(2), Integer.valueOf(matcher.group(3)).intValue());
      }
      matcher = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):service:(.+?):([0-9]+).*").matcher(s);
      if ((matcher.matches()) && (matcher.groupCount() >= 3))
      {
        String cleanToken = matcher.group(2).replaceAll("[^a-zA-Z_0-9\\-]", "-");
        log.warning("UPnP specification violation, replacing invalid service type token '" + matcher
        
          .group(2) + "' with: " + cleanToken);
        
        return new ServiceType(matcher.group(1), cleanToken, Integer.valueOf(matcher.group(3)).intValue());
      }
      matcher = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):serviceId:(.+?):([0-9]+).*").matcher(s);
      if ((matcher.matches()) && (matcher.groupCount() >= 3))
      {
        String cleanToken = matcher.group(2).replaceAll("[^a-zA-Z_0-9\\-]", "-");
        log.warning("UPnP specification violation, replacing invalid service type token '" + matcher
        
          .group(2) + "' with: " + cleanToken);
        
        return new ServiceType(matcher.group(1), cleanToken, Integer.valueOf(matcher.group(3)).intValue());
      }
    }
    catch (RuntimeException e)
    {
      throw new InvalidValueException(String.format("Can't parse service type string (namespace/type/version) '%s': %s", new Object[] { s, e
        .toString() }));
    }
    throw new InvalidValueException("Can't parse service type string (namespace/type/version): " + s);
  }
  
  public boolean implementsVersion(ServiceType that)
  {
    if (that == null) {
      return false;
    }
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
  
  public String toFriendlyString()
  {
    return getNamespace() + ":" + getType() + ":" + getVersion();
  }
  
  public String toString()
  {
    return "urn:" + getNamespace() + ":service:" + getType() + ":" + getVersion();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (!(o instanceof ServiceType))) {
      return false;
    }
    ServiceType that = (ServiceType)o;
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\ServiceType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */