package org.fourthline.cling.model.types;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceId
{
  private static final Logger log = Logger.getLogger(ServiceId.class.getName());
  public static final String UNKNOWN = "UNKNOWN";
  public static final Pattern PATTERN = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):serviceId:([a-zA-Z_0-9\\-:\\.]{1,64})");
  public static final Pattern BROKEN_PATTERN = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):service:([a-zA-Z_0-9\\-:\\.]{1,64})");
  private String namespace;
  private String id;
  
  public ServiceId(String namespace, String id)
  {
    if ((namespace != null) && (!namespace.matches("[a-zA-Z0-9\\-\\.]+"))) {
      throw new IllegalArgumentException("Service ID namespace contains illegal characters");
    }
    this.namespace = namespace;
    if ((id != null) && (!id.matches("[a-zA-Z_0-9\\-:\\.]{1,64}"))) {
      throw new IllegalArgumentException("Service ID suffix too long (64) or contains illegal characters");
    }
    this.id = id;
  }
  
  public String getNamespace()
  {
    return this.namespace;
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public static ServiceId valueOf(String s)
    throws InvalidValueException
  {
    ServiceId serviceId = null;
    try
    {
      serviceId = UDAServiceId.valueOf(s);
    }
    catch (Exception localException) {}
    if (serviceId != null) {
      return serviceId;
    }
    Matcher matcher = PATTERN.matcher(s);
    if ((matcher.matches()) && (matcher.groupCount() >= 2)) {
      return new ServiceId(matcher.group(1), matcher.group(2));
    }
    matcher = BROKEN_PATTERN.matcher(s);
    if ((matcher.matches()) && (matcher.groupCount() >= 2)) {
      return new ServiceId(matcher.group(1), matcher.group(2));
    }
    matcher = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):serviceId:").matcher(s);
    if ((matcher.matches()) && (matcher.groupCount() >= 1))
    {
      log.warning("UPnP specification violation, no service ID token, defaulting to UNKNOWN: " + s);
      return new ServiceId(matcher.group(1), "UNKNOWN");
    }
    String[] tokens = s.split("[:]");
    if (tokens.length == 4)
    {
      log.warning("UPnP specification violation, trying a simple colon-split of: " + s);
      return new ServiceId(tokens[1], tokens[3]);
    }
    throw new InvalidValueException("Can't parse service ID string (namespace/id): " + s);
  }
  
  public String toString()
  {
    return "urn:" + getNamespace() + ":serviceId:" + getId();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (!(o instanceof ServiceId))) {
      return false;
    }
    ServiceId serviceId = (ServiceId)o;
    if (!this.id.equals(serviceId.id)) {
      return false;
    }
    if (!this.namespace.equals(serviceId.namespace)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.namespace.hashCode();
    result = 31 * result + this.id.hashCode();
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\ServiceId.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */