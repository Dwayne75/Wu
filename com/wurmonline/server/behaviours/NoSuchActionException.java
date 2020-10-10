package com.wurmonline.server.behaviours;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchActionException
  extends WurmServerException
{
  private static final long serialVersionUID = 7185872169527936353L;
  
  public NoSuchActionException(String message)
  {
    super(message);
  }
  
  public NoSuchActionException(Throwable cause)
  {
    super(cause);
  }
  
  public NoSuchActionException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\NoSuchActionException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */