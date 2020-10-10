package org.fourthline.cling.model.types;

public class InvalidValueException
  extends RuntimeException
{
  public InvalidValueException(String s)
  {
    super(s);
  }
  
  public InvalidValueException(String s, Throwable throwable)
  {
    super(s, throwable);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\InvalidValueException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */