package org.fourthline.cling.model.action;

import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.profile.RemoteClientInfo;

public class RemoteActionInvocation
  extends ActionInvocation
{
  protected final RemoteClientInfo remoteClientInfo;
  
  public RemoteActionInvocation(Action action, ActionArgumentValue[] input, ActionArgumentValue[] output, RemoteClientInfo remoteClientInfo)
  {
    super(action, input, output, null);
    this.remoteClientInfo = remoteClientInfo;
  }
  
  public RemoteActionInvocation(Action action, RemoteClientInfo remoteClientInfo)
  {
    super(action);
    this.remoteClientInfo = remoteClientInfo;
  }
  
  public RemoteActionInvocation(ActionException failure, RemoteClientInfo remoteClientInfo)
  {
    super(failure);
    this.remoteClientInfo = remoteClientInfo;
  }
  
  public RemoteClientInfo getRemoteClientInfo()
  {
    return this.remoteClientInfo;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\action\RemoteActionInvocation.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */