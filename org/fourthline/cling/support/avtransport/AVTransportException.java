package org.fourthline.cling.support.avtransport;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.ErrorCode;

public class AVTransportException
  extends ActionException
{
  public AVTransportException(int errorCode, String message)
  {
    super(errorCode, message);
  }
  
  public AVTransportException(int errorCode, String message, Throwable cause)
  {
    super(errorCode, message, cause);
  }
  
  public AVTransportException(ErrorCode errorCode, String message)
  {
    super(errorCode, message);
  }
  
  public AVTransportException(ErrorCode errorCode)
  {
    super(errorCode);
  }
  
  public AVTransportException(AVTransportErrorCode errorCode, String message)
  {
    super(errorCode.getCode(), errorCode.getDescription() + ". " + message + ".");
  }
  
  public AVTransportException(AVTransportErrorCode errorCode)
  {
    super(errorCode.getCode(), errorCode.getDescription());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\AVTransportException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */