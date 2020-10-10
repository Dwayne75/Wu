package com.wurmonline.server.villages;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchRoleException
  extends WurmServerException
{
  private static final long serialVersionUID = -6630727392157751483L;
  
  public NoSuchRoleException(String message)
  {
    super(message);
  }
  
  public NoSuchRoleException(Throwable cause)
  {
    super(cause);
  }
  
  public NoSuchRoleException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\villages\NoSuchRoleException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */