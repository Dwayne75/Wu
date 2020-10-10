package com.wurmonline.server.structures;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchWallException
  extends WurmServerException
{
  private static final long serialVersionUID = 2443093162318322030L;
  
  public NoSuchWallException(String message)
  {
    super(message);
  }
  
  NoSuchWallException(Throwable cause)
  {
    super(cause);
  }
  
  NoSuchWallException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\NoSuchWallException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */