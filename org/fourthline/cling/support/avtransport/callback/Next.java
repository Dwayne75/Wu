package org.fourthline.cling.support.avtransport.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public abstract class Next
  extends ActionCallback
{
  private static Logger log = Logger.getLogger(Next.class.getName());
  
  protected Next(ActionInvocation actionInvocation, ControlPoint controlPoint)
  {
    super(actionInvocation, controlPoint);
  }
  
  protected Next(ActionInvocation actionInvocation)
  {
    super(actionInvocation);
  }
  
  public Next(Service service)
  {
    this(new UnsignedIntegerFourBytes(0L), service);
  }
  
  public Next(UnsignedIntegerFourBytes instanceId, Service service)
  {
    super(new ActionInvocation(service.getAction("Next")));
    getActionInvocation().setInput("InstanceID", instanceId);
  }
  
  public void success(ActionInvocation invocation)
  {
    log.fine("Execution successful");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\callback\Next.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */