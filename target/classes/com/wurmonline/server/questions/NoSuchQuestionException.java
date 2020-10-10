package com.wurmonline.server.questions;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchQuestionException
  extends WurmServerException
{
  private static final long serialVersionUID = -3831089100054421576L;
  
  public NoSuchQuestionException(String message)
  {
    super(message);
  }
  
  public NoSuchQuestionException(Throwable cause)
  {
    super(cause);
  }
  
  public NoSuchQuestionException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\NoSuchQuestionException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */