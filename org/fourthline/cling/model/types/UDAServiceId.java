package org.fourthline.cling.model.types;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDAServiceId
  extends ServiceId
{
  private static Logger log = Logger.getLogger(UDAServiceId.class.getName());
  public static final String DEFAULT_NAMESPACE = "upnp-org";
  public static final String BROKEN_DEFAULT_NAMESPACE = "schemas-upnp-org";
  public static final Pattern PATTERN = Pattern.compile("urn:upnp-org:serviceId:([a-zA-Z_0-9\\-:\\.]{1,64})");
  public static final Pattern BROKEN_PATTERN = Pattern.compile("urn:schemas-upnp-org:service:([a-zA-Z_0-9\\-:\\.]{1,64})");
  
  public UDAServiceId(String id)
  {
    super("upnp-org", id);
  }
  
  public static UDAServiceId valueOf(String s)
    throws InvalidValueException
  {
    Matcher matcher = PATTERN.matcher(s);
    if ((matcher.matches()) && (matcher.groupCount() >= 1)) {
      return new UDAServiceId(matcher.group(1));
    }
    matcher = BROKEN_PATTERN.matcher(s);
    if ((matcher.matches()) && (matcher.groupCount() >= 1)) {
      return new UDAServiceId(matcher.group(1));
    }
    matcher = Pattern.compile("urn:upnp-orgerviceId:urnchemas-upnp-orgervice:([a-zA-Z_0-9\\-:\\.]{1,64})").matcher(s);
    if (matcher.matches())
    {
      log.warning("UPnP specification violation, recovering from Eyecon garbage: " + s);
      return new UDAServiceId(matcher.group(1));
    }
    if (("ContentDirectory".equals(s)) || 
      ("ConnectionManager".equals(s)) || 
      ("RenderingControl".equals(s)) || 
      ("AVTransport".equals(s)))
    {
      log.warning("UPnP specification violation, fixing broken Service ID: " + s);
      return new UDAServiceId(s);
    }
    throw new InvalidValueException("Can't parse UDA service ID string (upnp-org/id): " + s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\UDAServiceId.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */