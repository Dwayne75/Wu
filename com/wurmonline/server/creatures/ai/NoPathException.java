package com.wurmonline.server.creatures.ai;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoPathException
  extends WurmServerException
{
  private static final long serialVersionUID = 372320709229086812L;
  
  public NoPathException(String message)
  {
    super(message);
  }
  
  NoPathException(Throwable cause)
  {
    super(cause);
  }
  
  NoPathException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\ai\NoPathException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */