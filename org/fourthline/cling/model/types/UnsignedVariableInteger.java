package org.fourthline.cling.model.types;

import java.util.logging.Logger;

public abstract class UnsignedVariableInteger
{
  private static final Logger log = Logger.getLogger(UnsignedVariableInteger.class.getName());
  protected long value;
  protected UnsignedVariableInteger() {}
  
  public static enum Bits
  {
    EIGHT(255L),  SIXTEEN(65535L),  TWENTYFOUR(16777215L),  THIRTYTWO(4294967295L);
    
    private long maxValue;
    
    private Bits(long maxValue)
    {
      this.maxValue = maxValue;
    }
    
    public long getMaxValue()
    {
      return this.maxValue;
    }
  }
  
  public UnsignedVariableInteger(long value)
    throws NumberFormatException
  {
    setValue(value);
  }
  
  public UnsignedVariableInteger(String s)
    throws NumberFormatException
  {
    if (s.startsWith("-"))
    {
      log.warning("Invalid negative integer value '" + s + "', assuming value 0!");
      s = "0";
    }
    setValue(Long.parseLong(s.trim()));
  }
  
  protected UnsignedVariableInteger setValue(long value)
  {
    isInRange(value);
    this.value = value;
    return this;
  }
  
  public Long getValue()
  {
    return Long.valueOf(this.value);
  }
  
  public void isInRange(long value)
    throws NumberFormatException
  {
    if ((value < getMinValue()) || (value > getBits().getMaxValue())) {
      throw new NumberFormatException("Value must be between " + getMinValue() + " and " + getBits().getMaxValue() + ": " + value);
    }
  }
  
  public int getMinValue()
  {
    return 0;
  }
  
  public abstract Bits getBits();
  
  public UnsignedVariableInteger increment(boolean rolloverToOne)
  {
    if (this.value + 1L > getBits().getMaxValue()) {
      this.value = (rolloverToOne ? 1L : 0L);
    } else {
      this.value += 1L;
    }
    return this;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    UnsignedVariableInteger that = (UnsignedVariableInteger)o;
    if (this.value != that.value) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    return (int)(this.value ^ this.value >>> 32);
  }
  
  public String toString()
  {
    return Long.toString(this.value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\UnsignedVariableInteger.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */