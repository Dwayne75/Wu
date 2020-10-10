package com.wurmonline.server.items;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchTemplateException
  extends WurmServerException
{
  private static final long serialVersionUID = 1157557174258373795L;
  
  public NoSuchTemplateException(String message)
  {
    super(message);
  }
  
  public NoSuchTemplateException(Throwable cause)
  {
    super(cause);
  }
  
  public NoSuchTemplateException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\NoSuchTemplateException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */