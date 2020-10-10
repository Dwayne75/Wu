package org.fourthline.cling.support.avtransport.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public abstract class Play
  extends ActionCallback
{
  private static Logger log = Logger.getLogger(Play.class.getName());
  
  public Play(Service service)
  {
    this(new UnsignedIntegerFourBytes(0L), service, "1");
  }
  
  public Play(Service service, String speed)
  {
    this(new UnsignedIntegerFourBytes(0L), service, speed);
  }
  
  public Play(UnsignedIntegerFourBytes instanceId, Service service)
  {
    this(instanceId, service, "1");
  }
  
  public Play(UnsignedIntegerFourBytes instanceId, Service service, String speed)
  {
    super(new ActionInvocation(service.getAction("Play")));
    getActionInvocation().setInput("InstanceID", instanceId);
    getActionInvocation().setInput("Speed", speed);
  }
  
  public void success(ActionInvocation invocation)
  {
    log.fine("Execution successful");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\callback\Play.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */