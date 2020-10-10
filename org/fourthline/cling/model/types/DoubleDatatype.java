package org.fourthline.cling.model.types;

public class DoubleDatatype
  extends AbstractDatatype<Double>
{
  public boolean isHandlingJavaType(Class type)
  {
    return (type == Double.TYPE) || (Double.class.isAssignableFrom(type));
  }
  
  public Double valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    try
    {
      return Double.valueOf(Double.parseDouble(s));
    }
    catch (NumberFormatException ex)
    {
      throw new InvalidValueException("Can't convert string to number: " + s, ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\DoubleDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */