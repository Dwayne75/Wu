package org.fourthline.cling.model.action;

import org.fourthline.cling.model.types.ErrorCode;

public class ActionException
  extends Exception
{
  private int errorCode;
  
  public ActionException(int errorCode, String message)
  {
    super(message);
    this.errorCode = errorCode;
  }
  
  public ActionException(int errorCode, String message, Throwable cause)
  {
    super(message, cause);
    this.errorCode = errorCode;
  }
  
  public ActionException(ErrorCode errorCode)
  {
    this(errorCode.getCode(), errorCode.getDescription());
  }
  
  public ActionException(ErrorCode errorCode, String message)
  {
    this(errorCode, message, true);
  }
  
  public ActionException(ErrorCode errorCode, String message, boolean concatMessages)
  {
    this(errorCode.getCode(), concatMessages ? errorCode.getDescription() + ". " + message + "." : message);
  }
  
  public ActionException(ErrorCode errorCode, String message, Throwable cause)
  {
    this(errorCode.getCode(), errorCode.getDescription() + ". " + message + ".", cause);
  }
  
  public int getErrorCode()
  {
    return this.errorCode;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\action\ActionException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */