package org.fourthline.cling.support.igd.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Connection.Error;
import org.fourthline.cling.support.model.Connection.Status;
import org.fourthline.cling.support.model.Connection.StatusInfo;

public abstract class GetStatusInfo
  extends ActionCallback
{
  public GetStatusInfo(Service service)
  {
    super(new ActionInvocation(service.getAction("GetStatusInfo")));
  }
  
  public void success(ActionInvocation invocation)
  {
    try
    {
      Connection.Status status = Connection.Status.valueOf(invocation.getOutput("NewConnectionStatus").getValue().toString());
      
      Connection.Error lastError = Connection.Error.valueOf(invocation.getOutput("NewLastConnectionError").getValue().toString());
      
      success(new Connection.StatusInfo(status, (UnsignedIntegerFourBytes)invocation.getOutput("NewUptime").getValue(), lastError));
    }
    catch (Exception ex)
    {
      invocation.setFailure(new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Invalid status or last error string: " + ex, ex));
      
      failure(invocation, null);
    }
  }
  
  protected abstract void success(Connection.StatusInfo paramStatusInfo);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\igd\callback\GetStatusInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */