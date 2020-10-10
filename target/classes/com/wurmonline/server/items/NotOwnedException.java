package com.wurmonline.server.items;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NotOwnedException
  extends WurmServerException
{
  private static final long serialVersionUID = 5160458755595540264L;
  
  public NotOwnedException(String message)
  {
    super(message);
  }
  
  public NotOwnedException(Throwable cause)
  {
    super(cause);
  }
  
  public NotOwnedException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\NotOwnedException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */