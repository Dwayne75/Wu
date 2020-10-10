package org.fourthline.cling.model.types;

public final class UnsignedIntegerTwoBytes
  extends UnsignedVariableInteger
{
  public UnsignedIntegerTwoBytes(long value)
    throws NumberFormatException
  {
    super(value);
  }
  
  public UnsignedIntegerTwoBytes(String s)
    throws NumberFormatException
  {
    super(s);
  }
  
  public UnsignedVariableInteger.Bits getBits()
  {
    return UnsignedVariableInteger.Bits.SIXTEEN;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\UnsignedIntegerTwoBytes.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */