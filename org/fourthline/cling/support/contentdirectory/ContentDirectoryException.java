package org.fourthline.cling.support.contentdirectory;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.ErrorCode;

public class ContentDirectoryException
  extends ActionException
{
  public ContentDirectoryException(int errorCode, String message)
  {
    super(errorCode, message);
  }
  
  public ContentDirectoryException(int errorCode, String message, Throwable cause)
  {
    super(errorCode, message, cause);
  }
  
  public ContentDirectoryException(ErrorCode errorCode, String message)
  {
    super(errorCode, message);
  }
  
  public ContentDirectoryException(ErrorCode errorCode)
  {
    super(errorCode);
  }
  
  public ContentDirectoryException(ContentDirectoryErrorCode errorCode, String message)
  {
    super(errorCode.getCode(), errorCode.getDescription() + ". " + message + ".");
  }
  
  public ContentDirectoryException(ContentDirectoryErrorCode errorCode)
  {
    super(errorCode.getCode(), errorCode.getDescription());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\ContentDirectoryException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */