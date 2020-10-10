package org.fourthline.cling.support.renderingcontrol.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.model.Channel;

public abstract class SetVolume
  extends ActionCallback
{
  private static Logger log = Logger.getLogger(SetVolume.class.getName());
  
  public SetVolume(Service service, long newVolume)
  {
    this(new UnsignedIntegerFourBytes(0L), service, newVolume);
  }
  
  public SetVolume(UnsignedIntegerFourBytes instanceId, Service service, long newVolume)
  {
    super(new ActionInvocation(service.getAction("SetVolume")));
    getActionInvocation().setInput("InstanceID", instanceId);
    getActionInvocation().setInput("Channel", Channel.Master.toString());
    getActionInvocation().setInput("DesiredVolume", new UnsignedIntegerTwoBytes(newVolume));
  }
  
  public void success(ActionInvocation invocation)
  {
    log.fine("Executed successfully");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\callback\SetVolume.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */