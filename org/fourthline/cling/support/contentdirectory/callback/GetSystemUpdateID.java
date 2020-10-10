package org.fourthline.cling.support.contentdirectory.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;

public abstract class GetSystemUpdateID
  extends ActionCallback
{
  public GetSystemUpdateID(Service service)
  {
    super(new ActionInvocation(service.getAction("GetSystemUpdateID")));
  }
  
  public void success(ActionInvocation invocation)
  {
    boolean ok = true;
    long id = 0L;
    try
    {
      id = Long.valueOf(invocation.getOutput("Id").getValue().toString()).longValue();
    }
    catch (Exception ex)
    {
      invocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, "Can't parse GetSystemUpdateID response: " + ex, ex));
      failure(invocation, null);
      ok = false;
    }
    if (ok) {
      received(invocation, id);
    }
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, long paramLong);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\callback\GetSystemUpdateID.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */