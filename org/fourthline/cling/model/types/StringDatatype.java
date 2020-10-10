package org.fourthline.cling.model.types;

public class StringDatatype
  extends AbstractDatatype<String>
{
  public String valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    return s;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\StringDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */