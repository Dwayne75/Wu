package com.wurmonline.server.zones;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchZoneException
  extends WurmServerException
{
  private static final long serialVersionUID = 7094119477458750028L;
  
  NoSuchZoneException(String message)
  {
    super(message);
  }
  
  NoSuchZoneException(Throwable cause)
  {
    super(cause);
  }
  
  NoSuchZoneException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\NoSuchZoneException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */