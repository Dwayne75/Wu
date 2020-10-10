package org.fourthline.cling.model.message.header;

import java.net.URI;
import org.fourthline.cling.model.types.ServiceType;

public class ServiceTypeHeader
  extends UpnpHeader<ServiceType>
{
  public ServiceTypeHeader() {}
  
  public ServiceTypeHeader(URI uri)
  {
    setString(uri.toString());
  }
  
  public ServiceTypeHeader(ServiceType value)
  {
    setValue(value);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(ServiceType.valueOf(s));
    }
    catch (RuntimeException ex)
    {
      throw new InvalidHeaderException("Invalid service type header value, " + ex.getMessage());
    }
  }
  
  public String getString()
  {
    return ((ServiceType)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\ServiceTypeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */