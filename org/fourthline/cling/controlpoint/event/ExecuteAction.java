package org.fourthline.cling.controlpoint.event;

import org.fourthline.cling.controlpoint.ActionCallback;

public class ExecuteAction
{
  protected ActionCallback callback;
  
  public ExecuteAction(ActionCallback callback)
  {
    this.callback = callback;
  }
  
  public ActionCallback getCallback()
  {
    return this.callback;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\controlpoint\event\ExecuteAction.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */