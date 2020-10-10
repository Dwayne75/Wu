package com.wurmonline.server.support;

public class TrelloException
  extends RuntimeException
{
  private static final long serialVersionUID = 7427993543996731840L;
  
  public TrelloException() {}
  
  public TrelloException(String msg)
  {
    super(msg);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\support\TrelloException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */