package org.fourthline.cling.model.types;

public class CharacterDatatype
  extends AbstractDatatype<Character>
{
  public boolean isHandlingJavaType(Class type)
  {
    return (type == Character.TYPE) || (Character.class.isAssignableFrom(type));
  }
  
  public Character valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    return Character.valueOf(s.charAt(0));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\CharacterDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */