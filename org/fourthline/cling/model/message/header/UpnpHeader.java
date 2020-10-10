package org.fourthline.cling.model.message.header;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.seamless.util.Exceptions;

public abstract class UpnpHeader<T>
{
  private static final Logger log = Logger.getLogger(UpnpHeader.class.getName());
  private T value;
  
  public static enum Type
  {
    USN("USN", new Class[] { USNRootDeviceHeader.class, DeviceUSNHeader.class, ServiceUSNHeader.class, UDNHeader.class }),  NT("NT", new Class[] { RootDeviceHeader.class, UDADeviceTypeHeader.class, UDAServiceTypeHeader.class, DeviceTypeHeader.class, ServiceTypeHeader.class, UDNHeader.class, NTEventHeader.class }),  NTS("NTS", new Class[] { NTSHeader.class }),  HOST("HOST", new Class[] { HostHeader.class }),  SERVER("SERVER", new Class[] { ServerHeader.class }),  LOCATION("LOCATION", new Class[] { LocationHeader.class }),  MAX_AGE("CACHE-CONTROL", new Class[] { MaxAgeHeader.class }),  USER_AGENT("USER-AGENT", new Class[] { UserAgentHeader.class }),  CONTENT_TYPE("CONTENT-TYPE", new Class[] { ContentTypeHeader.class }),  MAN("MAN", new Class[] { MANHeader.class }),  MX("MX", new Class[] { MXHeader.class }),  ST("ST", new Class[] { STAllHeader.class, RootDeviceHeader.class, UDADeviceTypeHeader.class, UDAServiceTypeHeader.class, DeviceTypeHeader.class, ServiceTypeHeader.class, UDNHeader.class }),  EXT("EXT", new Class[] { EXTHeader.class }),  SOAPACTION("SOAPACTION", new Class[] { SoapActionHeader.class }),  TIMEOUT("TIMEOUT", new Class[] { TimeoutHeader.class }),  CALLBACK("CALLBACK", new Class[] { CallbackHeader.class }),  SID("SID", new Class[] { SubscriptionIdHeader.class }),  SEQ("SEQ", new Class[] { EventSequenceHeader.class }),  RANGE("RANGE", new Class[] { RangeHeader.class }),  CONTENT_RANGE("CONTENT-RANGE", new Class[] { ContentRangeHeader.class }),  PRAGMA("PRAGMA", new Class[] { PragmaHeader.class }),  EXT_IFACE_MAC("X-CLING-IFACE-MAC", new Class[] { InterfaceMacHeader.class }),  EXT_AV_CLIENT_INFO("X-AV-CLIENT-INFO", new Class[] { AVClientInfoHeader.class });
    
    private static Map<String, Type> byName = new HashMap() {};
    private String httpName;
    private Class<? extends UpnpHeader>[] headerTypes;
    
    @SafeVarargs
    private Type(String httpName, Class<? extends UpnpHeader>... headerClass)
    {
      this.httpName = httpName;
      this.headerTypes = headerClass;
    }
    
    public String getHttpName()
    {
      return this.httpName;
    }
    
    public Class<? extends UpnpHeader>[] getHeaderTypes()
    {
      return this.headerTypes;
    }
    
    public boolean isValidHeaderType(Class<? extends UpnpHeader> clazz)
    {
      for (Class<? extends UpnpHeader> permissibleType : getHeaderTypes()) {
        if (permissibleType.isAssignableFrom(clazz)) {
          return true;
        }
      }
      return false;
    }
    
    public static Type getByHttpName(String httpName)
    {
      if (httpName == null) {
        return null;
      }
      return (Type)byName.get(httpName.toUpperCase(Locale.ROOT));
    }
  }
  
  public void setValue(T value)
  {
    this.value = value;
  }
  
  public T getValue()
  {
    return (T)this.value;
  }
  
  public abstract void setString(String paramString)
    throws InvalidHeaderException;
  
  public abstract String getString();
  
  public static UpnpHeader newInstance(Type type, String headerValue)
  {
    UpnpHeader upnpHeader = null;
    for (int i = 0; (i < type.getHeaderTypes().length) && (upnpHeader == null); i++)
    {
      Class<? extends UpnpHeader> headerClass = type.getHeaderTypes()[i];
      try
      {
        log.finest("Trying to parse '" + type + "' with class: " + headerClass.getSimpleName());
        upnpHeader = (UpnpHeader)headerClass.newInstance();
        if (headerValue != null) {
          upnpHeader.setString(headerValue);
        }
      }
      catch (InvalidHeaderException ex)
      {
        log.finest("Invalid header value for tested type: " + headerClass.getSimpleName() + " - " + ex.getMessage());
        upnpHeader = null;
      }
      catch (Exception ex)
      {
        log.severe("Error instantiating header of type '" + type + "' with value: " + headerValue);
        log.log(Level.SEVERE, "Exception root cause: ", Exceptions.unwrap(ex));
      }
    }
    return upnpHeader;
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") '" + getValue() + "'";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\UpnpHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */