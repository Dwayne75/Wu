package com.wurmonline.server.structures;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchLockException
  extends WurmServerException
{
  private static final long serialVersionUID = 2894616265258932169L;
  
  NoSuchLockException(String message)
  {
    super(message);
  }
  
  NoSuchLockException(Throwable cause)
  {
    super(cause);
  }
  
  NoSuchLockException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\NoSuchLockException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */