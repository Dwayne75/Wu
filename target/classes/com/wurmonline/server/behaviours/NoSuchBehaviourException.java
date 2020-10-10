package com.wurmonline.server.behaviours;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchBehaviourException
  extends WurmServerException
{
  private static final long serialVersionUID = -7889104664023078651L;
  
  public NoSuchBehaviourException(String message)
  {
    super(message);
  }
  
  public NoSuchBehaviourException(Throwable cause)
  {
    super(cause);
  }
  
  public NoSuchBehaviourException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\NoSuchBehaviourException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */