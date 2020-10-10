package com.wurmonline.server.items;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSpaceException
  extends WurmServerException
{
  private static final long serialVersionUID = -7007492502695022234L;
  
  public NoSpaceException(String message)
  {
    super(message);
  }
  
  public NoSpaceException(Throwable cause)
  {
    super(cause);
  }
  
  public NoSpaceException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\items\NoSpaceException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */