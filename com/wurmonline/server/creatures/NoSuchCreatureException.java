package com.wurmonline.server.creatures;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchCreatureException
  extends WurmServerException
{
  private static final long serialVersionUID = -843014199612164008L;
  
  public NoSuchCreatureException(String message)
  {
    super(message);
  }
  
  NoSuchCreatureException(Throwable cause)
  {
    super(cause);
  }
  
  NoSuchCreatureException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\NoSuchCreatureException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */