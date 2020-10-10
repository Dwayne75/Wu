package com.wurmonline.shared.exceptions;

public class WurmException
  extends Exception
{
  private static final long serialVersionUID = 1268608703615765075L;
  
  public WurmException(String message)
  {
    super(message);
  }
  
  public WurmException(Throwable cause)
  {
    super(cause);
  }
  
  public WurmException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\exceptions\WurmException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */