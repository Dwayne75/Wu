package org.fourthline.cling.model.types;

public class UnsignedIntegerFourBytesDatatype
  extends AbstractDatatype<UnsignedIntegerFourBytes>
{
  public UnsignedIntegerFourBytes valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    try
    {
      return new UnsignedIntegerFourBytes(s);
    }
    catch (NumberFormatException ex)
    {
      throw new InvalidValueException("Can't convert string to number or not in range: " + s, ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\UnsignedIntegerFourBytesDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */