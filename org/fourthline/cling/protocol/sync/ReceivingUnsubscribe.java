package org.fourthline.cling.protocol.sync;

import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.gena.IncomingUnsubscribeRequestMessage;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.resource.ServiceEventSubscriptionResource;
import org.fourthline.cling.protocol.ReceivingSync;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.RouterException;

public class ReceivingUnsubscribe
  extends ReceivingSync<StreamRequestMessage, StreamResponseMessage>
{
  private static final Logger log = Logger.getLogger(ReceivingUnsubscribe.class.getName());
  
  public ReceivingUnsubscribe(UpnpService upnpService, StreamRequestMessage inputMessage)
  {
    super(upnpService, inputMessage);
  }
  
  protected StreamResponseMessage executeSync()
    throws RouterException
  {
    ServiceEventSubscriptionResource resource = (ServiceEventSubscriptionResource)getUpnpService().getRegistry().getResource(ServiceEventSubscriptionResource.class, 
    
      ((StreamRequestMessage)getInputMessage()).getUri());
    if (resource == null)
    {
      log.fine("No local resource found: " + getInputMessage());
      return null;
    }
    log.fine("Found local event subscription matching relative request URI: " + ((StreamRequestMessage)getInputMessage()).getUri());
    
    IncomingUnsubscribeRequestMessage requestMessage = new IncomingUnsubscribeRequestMessage((StreamRequestMessage)getInputMessage(), (LocalService)resource.getModel());
    if ((requestMessage.getSubscriptionId() != null) && (
      (requestMessage.hasNotificationHeader()) || (requestMessage.hasCallbackHeader())))
    {
      log.fine("Subscription ID and NT or Callback in unsubcribe request: " + getInputMessage());
      return new StreamResponseMessage(UpnpResponse.Status.BAD_REQUEST);
    }
    LocalGENASubscription subscription = getUpnpService().getRegistry().getLocalSubscription(requestMessage.getSubscriptionId());
    if (subscription == null)
    {
      log.fine("Invalid subscription ID for unsubscribe request: " + getInputMessage());
      return new StreamResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
    }
    log.fine("Unregistering subscription: " + subscription);
    if (getUpnpService().getRegistry().removeLocalSubscription(subscription)) {
      subscription.end(null);
    } else {
      log.fine("Subscription was already removed from registry");
    }
    return new StreamResponseMessage(UpnpResponse.Status.OK);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\sync\ReceivingUnsubscribe.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */