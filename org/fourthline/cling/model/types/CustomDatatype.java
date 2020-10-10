package org.fourthline.cling.model.types;

public class CustomDatatype
  extends AbstractDatatype<String>
{
  private String name;
  
  public CustomDatatype(String name)
  {
    this.name = name;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    return s;
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") '" + getName() + "'";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\CustomDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */