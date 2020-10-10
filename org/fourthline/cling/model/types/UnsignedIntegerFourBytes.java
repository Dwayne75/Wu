package org.fourthline.cling.model.types;

public final class UnsignedIntegerFourBytes
  extends UnsignedVariableInteger
{
  public UnsignedIntegerFourBytes(long value)
    throws NumberFormatException
  {
    super(value);
  }
  
  public UnsignedIntegerFourBytes(String s)
    throws NumberFormatException
  {
    super(s);
  }
  
  public UnsignedVariableInteger.Bits getBits()
  {
    return UnsignedVariableInteger.Bits.THIRTYTWO;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\UnsignedIntegerFourBytes.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */