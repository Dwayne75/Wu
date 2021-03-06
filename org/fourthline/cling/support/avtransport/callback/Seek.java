package org.fourthline.cling.support.avtransport.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.SeekMode;

public abstract class Seek
  extends ActionCallback
{
  private static Logger log = Logger.getLogger(Seek.class.getName());
  
  public Seek(Service service, String relativeTimeTarget)
  {
    this(new UnsignedIntegerFourBytes(0L), service, SeekMode.REL_TIME, relativeTimeTarget);
  }
  
  public Seek(UnsignedIntegerFourBytes instanceId, Service service, String relativeTimeTarget)
  {
    this(instanceId, service, SeekMode.REL_TIME, relativeTimeTarget);
  }
  
  public Seek(Service service, SeekMode mode, String target)
  {
    this(new UnsignedIntegerFourBytes(0L), service, mode, target);
  }
  
  public Seek(UnsignedIntegerFourBytes instanceId, Service service, SeekMode mode, String target)
  {
    super(new ActionInvocation(service.getAction("Seek")));
    getActionInvocation().setInput("InstanceID", instanceId);
    getActionInvocation().setInput("Unit", mode.name());
    getActionInvocation().setInput("Target", target);
  }
  
  public void success(ActionInvocation invocation)
  {
    log.fine("Execution successful");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\callback\Seek.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */