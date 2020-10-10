package com.wurmonline.server.zones;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchTileException
  extends WurmServerException
{
  private static final long serialVersionUID = -974263427293465936L;
  
  NoSuchTileException(String message)
  {
    super(message);
  }
  
  NoSuchTileException(Throwable cause)
  {
    super(cause);
  }
  
  NoSuchTileException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\NoSuchTileException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */