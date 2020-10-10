package org.fourthline.cling.support.igd.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;

public abstract class GetExternalIP
  extends ActionCallback
{
  public GetExternalIP(Service service)
  {
    super(new ActionInvocation(service.getAction("GetExternalIPAddress")));
  }
  
  public void success(ActionInvocation invocation)
  {
    success((String)invocation.getOutput("NewExternalIPAddress").getValue());
  }
  
  protected abstract void success(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\igd\callback\GetExternalIP.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */