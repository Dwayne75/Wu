package org.fourthline.cling.model.types;

import org.seamless.util.io.HexBin;

public class BinHexDatatype
  extends AbstractDatatype<byte[]>
{
  public Class<byte[]> getValueType()
  {
    return byte[].class;
  }
  
  public byte[] valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    try
    {
      return HexBin.stringToBytes(s);
    }
    catch (Exception ex)
    {
      throw new InvalidValueException(ex.getMessage(), ex);
    }
  }
  
  public String getString(byte[] value)
    throws InvalidValueException
  {
    if (value == null) {
      return "";
    }
    try
    {
      return HexBin.bytesToString(value);
    }
    catch (Exception ex)
    {
      throw new InvalidValueException(ex.getMessage(), ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\BinHexDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */