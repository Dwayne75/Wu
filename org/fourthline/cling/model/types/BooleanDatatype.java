package org.fourthline.cling.model.types;

import java.util.Locale;

public class BooleanDatatype
  extends AbstractDatatype<Boolean>
{
  public boolean isHandlingJavaType(Class type)
  {
    return (type == Boolean.TYPE) || (Boolean.class.isAssignableFrom(type));
  }
  
  public Boolean valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    if ((s.equals("1")) || (s.toUpperCase(Locale.ROOT).equals("YES")) || (s.toUpperCase(Locale.ROOT).equals("TRUE"))) {
      return Boolean.valueOf(true);
    }
    if ((s.equals("0")) || (s.toUpperCase(Locale.ROOT).equals("NO")) || (s.toUpperCase(Locale.ROOT).equals("FALSE"))) {
      return Boolean.valueOf(false);
    }
    throw new InvalidValueException("Invalid boolean value string: " + s);
  }
  
  public String getString(Boolean value)
    throws InvalidValueException
  {
    if (value == null) {
      return "";
    }
    return value.booleanValue() ? "1" : "0";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\BooleanDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */