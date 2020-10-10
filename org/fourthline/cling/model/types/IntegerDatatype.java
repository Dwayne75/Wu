package org.fourthline.cling.model.types;

public class IntegerDatatype
  extends AbstractDatatype<Integer>
{
  private int byteSize;
  
  public IntegerDatatype(int byteSize)
  {
    this.byteSize = byteSize;
  }
  
  public boolean isHandlingJavaType(Class type)
  {
    return (type == Integer.TYPE) || (Integer.class.isAssignableFrom(type));
  }
  
  public Integer valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    try
    {
      Integer value = Integer.valueOf(Integer.parseInt(s.trim()));
      if (!isValid(value)) {
        throw new InvalidValueException("Not a " + getByteSize() + " byte(s) integer: " + s);
      }
      return value;
    }
    catch (NumberFormatException ex)
    {
      if (s.equals("NOT_IMPLEMENTED")) {
        return Integer.valueOf(getMaxValue());
      }
      throw new InvalidValueException("Can't convert string to number: " + s, ex);
    }
  }
  
  public boolean isValid(Integer value)
  {
    return (value == null) || ((value.intValue() >= getMinValue()) && (value.intValue() <= getMaxValue()));
  }
  
  public int getMinValue()
  {
    switch (getByteSize())
    {
    case 1: 
      return -128;
    case 2: 
      return 32768;
    case 4: 
      return Integer.MIN_VALUE;
    }
    throw new IllegalArgumentException("Invalid integer byte size: " + getByteSize());
  }
  
  public int getMaxValue()
  {
    switch (getByteSize())
    {
    case 1: 
      return 127;
    case 2: 
      return 32767;
    case 4: 
      return Integer.MAX_VALUE;
    }
    throw new IllegalArgumentException("Invalid integer byte size: " + getByteSize());
  }
  
  public int getByteSize()
  {
    return this.byteSize;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\IntegerDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */