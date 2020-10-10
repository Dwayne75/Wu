package org.fourthline.cling.model.types;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.ModelUtil;

public class SoapActionType
{
  public static final String MAGIC_CONTROL_NS = "schemas-upnp-org";
  public static final String MAGIC_CONTROL_TYPE = "control-1-0";
  public static final Pattern PATTERN_MAGIC_CONTROL = Pattern.compile("urn:schemas-upnp-org:control-1-0#([a-zA-Z0-9^-_\\p{L}\\p{N}]{1}[a-zA-Z0-9^-_\\.\\\\p{L}\\\\p{N}\\p{Mc}\\p{Sk}]*)");
  public static final Pattern PATTERN = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):service:([a-zA-Z_0-9\\-]{1,64}):([0-9]+)#([a-zA-Z0-9^-_\\p{L}\\p{N}]{1}[a-zA-Z0-9^-_\\.\\\\p{L}\\\\p{N}\\p{Mc}\\p{Sk}]*)");
  private String namespace;
  private String type;
  private String actionName;
  private Integer version;
  
  public SoapActionType(ServiceType serviceType, String actionName)
  {
    this(serviceType.getNamespace(), serviceType.getType(), Integer.valueOf(serviceType.getVersion()), actionName);
  }
  
  public SoapActionType(String namespace, String type, Integer version, String actionName)
  {
    this.namespace = namespace;
    this.type = type;
    this.version = version;
    this.actionName = actionName;
    if ((actionName != null) && (!ModelUtil.isValidUDAName(actionName))) {
      throw new IllegalArgumentException("Action name contains illegal characters: " + actionName);
    }
  }
  
  public String getActionName()
  {
    return this.actionName;
  }
  
  public String getNamespace()
  {
    return this.namespace;
  }
  
  public String getType()
  {
    return this.type;
  }
  
  public Integer getVersion()
  {
    return this.version;
  }
  
  public static SoapActionType valueOf(String s)
    throws InvalidValueException
  {
    Matcher magicControlMatcher = PATTERN_MAGIC_CONTROL.matcher(s);
    try
    {
      if (magicControlMatcher.matches()) {
        return new SoapActionType("schemas-upnp-org", "control-1-0", null, magicControlMatcher.group(1));
      }
      Matcher matcher = PATTERN.matcher(s);
      if (matcher.matches()) {
        return new SoapActionType(matcher.group(1), matcher.group(2), Integer.valueOf(matcher.group(3)), matcher.group(4));
      }
    }
    catch (RuntimeException e)
    {
      throw new InvalidValueException(String.format("Can't parse action type string (namespace/type/version#actionName) '%s': %s", new Object[] { s, e
        .toString() }));
    }
    throw new InvalidValueException("Can't parse action type string (namespace/type/version#actionName): " + s);
  }
  
  public ServiceType getServiceType()
  {
    if (this.version == null) {
      return null;
    }
    return new ServiceType(this.namespace, this.type, this.version.intValue());
  }
  
  public String toString()
  {
    return getTypeString() + "#" + getActionName();
  }
  
  public String getTypeString()
  {
    if (this.version == null) {
      return "urn:" + getNamespace() + ":" + getType();
    }
    return "urn:" + getNamespace() + ":service:" + getType() + ":" + getVersion();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (!(o instanceof SoapActionType))) {
      return false;
    }
    SoapActionType that = (SoapActionType)o;
    if (!this.actionName.equals(that.actionName)) {
      return false;
    }
    if (!this.namespace.equals(that.namespace)) {
      return false;
    }
    if (!this.type.equals(that.type)) {
      return false;
    }
    if (this.version != null ? !this.version.equals(that.version) : that.version != null) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.namespace.hashCode();
    result = 31 * result + this.type.hashCode();
    result = 31 * result + this.actionName.hashCode();
    result = 31 * result + (this.version != null ? this.version.hashCode() : 0);
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\SoapActionType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */