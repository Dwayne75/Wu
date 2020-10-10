package org.fourthline.cling.model.message.header;

import java.net.URI;
import org.fourthline.cling.model.types.SoapActionType;

public class SoapActionHeader
  extends UpnpHeader<SoapActionType>
{
  public SoapActionHeader() {}
  
  public SoapActionHeader(URI uri)
  {
    setValue(SoapActionType.valueOf(uri.toString()));
  }
  
  public SoapActionHeader(SoapActionType value)
  {
    setValue(value);
  }
  
  public SoapActionHeader(String s)
    throws InvalidHeaderException
  {
    setString(s);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      if ((!s.startsWith("\"")) && (s.endsWith("\""))) {
        throw new InvalidHeaderException("Invalid SOAP action header, must be enclosed in doublequotes:" + s);
      }
      SoapActionType t = SoapActionType.valueOf(s.substring(1, s.length() - 1));
      setValue(t);
    }
    catch (RuntimeException ex)
    {
      throw new InvalidHeaderException("Invalid SOAP action header value, " + ex.getMessage());
    }
  }
  
  public String getString()
  {
    return "\"" + ((SoapActionType)getValue()).toString() + "\"";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\SoapActionHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */