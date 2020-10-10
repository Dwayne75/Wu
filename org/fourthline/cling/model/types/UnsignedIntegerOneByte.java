package org.fourthline.cling.model.types;

public final class UnsignedIntegerOneByte
  extends UnsignedVariableInteger
{
  public UnsignedIntegerOneByte(long value)
    throws NumberFormatException
  {
    super(value);
  }
  
  public UnsignedIntegerOneByte(String s)
    throws NumberFormatException
  {
    super(s);
  }
  
  public UnsignedVariableInteger.Bits getBits()
  {
    return UnsignedVariableInteger.Bits.EIGHT;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\UnsignedIntegerOneByte.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */