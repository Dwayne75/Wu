package org.fourthline.cling.model.action;

import org.fourthline.cling.model.types.ErrorCode;

public class ActionCancelledException
  extends ActionException
{
  public ActionCancelledException(InterruptedException cause)
  {
    super(ErrorCode.ACTION_FAILED, "Action execution interrupted", cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\action\ActionCancelledException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */