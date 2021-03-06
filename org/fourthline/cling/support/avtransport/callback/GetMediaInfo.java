package org.fourthline.cling.support.avtransport.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.MediaInfo;

public abstract class GetMediaInfo
  extends ActionCallback
{
  private static Logger log = Logger.getLogger(GetMediaInfo.class.getName());
  
  public GetMediaInfo(Service service)
  {
    this(new UnsignedIntegerFourBytes(0L), service);
  }
  
  public GetMediaInfo(UnsignedIntegerFourBytes instanceId, Service service)
  {
    super(new ActionInvocation(service.getAction("GetMediaInfo")));
    getActionInvocation().setInput("InstanceID", instanceId);
  }
  
  public void success(ActionInvocation invocation)
  {
    MediaInfo mediaInfo = new MediaInfo(invocation.getOutputMap());
    received(invocation, mediaInfo);
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, MediaInfo paramMediaInfo);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\callback\GetMediaInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */