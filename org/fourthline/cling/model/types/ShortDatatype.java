package org.fourthline.cling.model.types;

public class ShortDatatype
  extends AbstractDatatype<Short>
{
  public boolean isHandlingJavaType(Class type)
  {
    return (type == Short.TYPE) || (Short.class.isAssignableFrom(type));
  }
  
  public Short valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    try
    {
      Short value = Short.valueOf(Short.parseShort(s.trim()));
      if (!isValid(value)) {
        throw new InvalidValueException("Not a valid short: " + s);
      }
      return value;
    }
    catch (NumberFormatException ex)
    {
      throw new InvalidValueException("Can't convert string to number: " + s, ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\ShortDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */