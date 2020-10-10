package org.fourthline.cling.support.avtransport.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public abstract class SetAVTransportURI
  extends ActionCallback
{
  private static Logger log = Logger.getLogger(SetAVTransportURI.class.getName());
  
  public SetAVTransportURI(Service service, String uri)
  {
    this(new UnsignedIntegerFourBytes(0L), service, uri, null);
  }
  
  public SetAVTransportURI(Service service, String uri, String metadata)
  {
    this(new UnsignedIntegerFourBytes(0L), service, uri, metadata);
  }
  
  public SetAVTransportURI(UnsignedIntegerFourBytes instanceId, Service service, String uri)
  {
    this(instanceId, service, uri, null);
  }
  
  public SetAVTransportURI(UnsignedIntegerFourBytes instanceId, Service service, String uri, String metadata)
  {
    super(new ActionInvocation(service.getAction("SetAVTransportURI")));
    log.fine("Creating SetAVTransportURI action for URI: " + uri);
    getActionInvocation().setInput("InstanceID", instanceId);
    getActionInvocation().setInput("CurrentURI", uri);
    getActionInvocation().setInput("CurrentURIMetaData", metadata);
  }
  
  public void success(ActionInvocation invocation)
  {
    log.fine("Execution successful");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\callback\SetAVTransportURI.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */