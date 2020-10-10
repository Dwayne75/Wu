package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.NamedServiceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;

public class ServiceUSNHeader
  extends UpnpHeader<NamedServiceType>
{
  public ServiceUSNHeader() {}
  
  public ServiceUSNHeader(UDN udn, ServiceType serviceType)
  {
    setValue(new NamedServiceType(udn, serviceType));
  }
  
  public ServiceUSNHeader(NamedServiceType value)
  {
    setValue(value);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(NamedServiceType.valueOf(s));
    }
    catch (Exception ex)
    {
      throw new InvalidHeaderException("Invalid service USN header value, " + ex.getMessage());
    }
  }
  
  public String getString()
  {
    return ((NamedServiceType)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\ServiceUSNHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */