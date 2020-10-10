package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class FailedException
  extends WurmServerException
{
  private static final long serialVersionUID = 3728193914548210778L;
  
  public FailedException(String message)
  {
    super(message);
  }
  
  public FailedException(Throwable cause)
  {
    super(cause);
  }
  
  public FailedException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\FailedException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */