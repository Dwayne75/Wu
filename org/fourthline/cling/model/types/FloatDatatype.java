package org.fourthline.cling.model.types;

public class FloatDatatype
  extends AbstractDatatype<Float>
{
  public boolean isHandlingJavaType(Class type)
  {
    return (type == Float.TYPE) || (Float.class.isAssignableFrom(type));
  }
  
  public Float valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    try
    {
      return Float.valueOf(Float.parseFloat(s.trim()));
    }
    catch (NumberFormatException ex)
    {
      throw new InvalidValueException("Can't convert string to number: " + s, ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\FloatDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */