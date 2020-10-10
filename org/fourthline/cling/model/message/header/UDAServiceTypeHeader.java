package org.fourthline.cling.model.message.header;

import java.net.URI;
import org.fourthline.cling.model.types.UDAServiceType;

public class UDAServiceTypeHeader
  extends ServiceTypeHeader
{
  public UDAServiceTypeHeader() {}
  
  public UDAServiceTypeHeader(URI uri)
  {
    super(uri);
  }
  
  public UDAServiceTypeHeader(UDAServiceType value)
  {
    super(value);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(UDAServiceType.valueOf(s));
    }
    catch (Exception ex)
    {
      throw new InvalidHeaderException("Invalid UDA service type header value, " + ex.getMessage());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\UDAServiceTypeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */