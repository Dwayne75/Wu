package org.fourthline.cling.support.renderingcontrol;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.ErrorCode;

public class RenderingControlException
  extends ActionException
{
  public RenderingControlException(int errorCode, String message)
  {
    super(errorCode, message);
  }
  
  public RenderingControlException(int errorCode, String message, Throwable cause)
  {
    super(errorCode, message, cause);
  }
  
  public RenderingControlException(ErrorCode errorCode, String message)
  {
    super(errorCode, message);
  }
  
  public RenderingControlException(ErrorCode errorCode)
  {
    super(errorCode);
  }
  
  public RenderingControlException(RenderingControlErrorCode errorCode, String message)
  {
    super(errorCode.getCode(), errorCode.getDescription() + ". " + message + ".");
  }
  
  public RenderingControlException(RenderingControlErrorCode errorCode)
  {
    super(errorCode.getCode(), errorCode.getDescription());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\RenderingControlException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */