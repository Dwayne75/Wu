package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchGroupException
  extends WurmServerException
{
  private static final long serialVersionUID = 6759054654154037924L;
  
  NoSuchGroupException(String message)
  {
    super(message);
  }
  
  NoSuchGroupException(Throwable cause)
  {
    super(cause);
  }
  
  NoSuchGroupException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\NoSuchGroupException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */