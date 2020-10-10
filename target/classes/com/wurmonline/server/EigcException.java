package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class EigcException
  extends WurmServerException
{
  private static final long serialVersionUID = 5813764704231108263L;
  
  public EigcException(String message)
  {
    super(message);
  }
  
  public EigcException(Throwable cause)
  {
    super(cause);
  }
  
  public EigcException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\EigcException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */