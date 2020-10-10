package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.PragmaType;

public class PragmaHeader
  extends UpnpHeader<PragmaType>
{
  public PragmaHeader() {}
  
  public PragmaHeader(PragmaType value)
  {
    setValue(value);
  }
  
  public PragmaHeader(String s)
  {
    setString(s);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      setValue(PragmaType.valueOf(s));
    }
    catch (InvalidValueException invalidValueException)
    {
      throw new InvalidHeaderException("Invalid Range Header: " + invalidValueException.getMessage());
    }
  }
  
  public String getString()
  {
    return ((PragmaType)getValue()).getString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\PragmaHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */