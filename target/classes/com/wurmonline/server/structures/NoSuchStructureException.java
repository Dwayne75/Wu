package com.wurmonline.server.structures;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchStructureException
  extends WurmServerException
{
  private static final long serialVersionUID = 7841234936326217783L;
  
  public NoSuchStructureException(String message)
  {
    super(message);
  }
  
  NoSuchStructureException(Throwable cause)
  {
    super(cause);
  }
  
  NoSuchStructureException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\NoSuchStructureException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */