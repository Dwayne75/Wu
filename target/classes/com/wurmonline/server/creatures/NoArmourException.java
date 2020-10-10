package com.wurmonline.server.creatures;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoArmourException
  extends WurmServerException
{
  private static final long serialVersionUID = 9021493151024263335L;
  
  public NoArmourException(String message)
  {
    super(message);
  }
  
  NoArmourException(Throwable cause)
  {
    super(cause);
  }
  
  NoArmourException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\creatures\NoArmourException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */