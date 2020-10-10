package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchItemException
  extends WurmServerException
{
  private static final long serialVersionUID = -4699460609829035442L;
  
  public NoSuchItemException(String message)
  {
    super(message);
  }
  
  public NoSuchItemException(Throwable cause)
  {
    super(cause);
  }
  
  public NoSuchItemException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\NoSuchItemException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */