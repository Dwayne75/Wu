package org.fourthline.cling.model.types;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDAServiceType
  extends ServiceType
{
  public static final String DEFAULT_NAMESPACE = "schemas-upnp-org";
  public static final Pattern PATTERN = Pattern.compile("urn:schemas-upnp-org:service:([a-zA-Z_0-9\\-]{1,64}):([0-9]+).*");
  
  public UDAServiceType(String type)
  {
    this(type, 1);
  }
  
  public UDAServiceType(String type, int version)
  {
    super("schemas-upnp-org", type, version);
  }
  
  public static UDAServiceType valueOf(String s)
    throws InvalidValueException
  {
    Matcher matcher = PATTERN.matcher(s);
    try
    {
      if (matcher.matches()) {
        return new UDAServiceType(matcher.group(1), Integer.valueOf(matcher.group(2)).intValue());
      }
    }
    catch (RuntimeException e)
    {
      throw new InvalidValueException(String.format("Can't parse UDA service type string (namespace/type/version) '%s': %s", new Object[] { s, e
        .toString() }));
    }
    throw new InvalidValueException("Can't parse UDA service type string (namespace/type/version): " + s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\UDAServiceType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */