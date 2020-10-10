package org.fourthline.cling.model.action;

import org.fourthline.cling.model.meta.LocalService;

public abstract interface ActionExecutor
{
  public abstract void execute(ActionInvocation<LocalService> paramActionInvocation);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\action\ActionExecutor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */