package org.fourthline.cling.protocol.sync;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.gena.IncomingSubscribeRequestMessage;
import org.fourthline.cling.model.message.gena.OutgoingSubscribeResponseMessage;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.resource.ServiceEventSubscriptionResource;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ReceivingSync;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;

public class ReceivingSubscribe
  extends ReceivingSync<StreamRequestMessage, OutgoingSubscribeResponseMessage>
{
  private static final Logger log = Logger.getLogger(ReceivingSubscribe.class.getName());
  protected LocalGENASubscription subscription;
  
  public ReceivingSubscribe(UpnpService upnpService, StreamRequestMessage inputMessage)
  {
    super(upnpService, inputMessage);
  }
  
  protected OutgoingSubscribeResponseMessage executeSync()
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
    
    IncomingSubscribeRequestMessage requestMessage = new IncomingSubscribeRequestMessage((StreamRequestMessage)getInputMessage(), (LocalService)resource.getModel());
    if ((requestMessage.getSubscriptionId() != null) && (
      (requestMessage.hasNotificationHeader()) || (requestMessage.getCallbackURLs() != null)))
    {
      log.fine("Subscription ID and NT or Callback in subscribe request: " + getInputMessage());
      return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.BAD_REQUEST);
    }
    if (requestMessage.getSubscriptionId() != null) {
      return processRenewal((LocalService)resource.getModel(), requestMessage);
    }
    if ((requestMessage.hasNotificationHeader()) && (requestMessage.getCallbackURLs() != null)) {
      return processNewSubscription((LocalService)resource.getModel(), requestMessage);
    }
    log.fine("No subscription ID, no NT or Callback, neither subscription or renewal: " + getInputMessage());
    return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
  }
  
  protected OutgoingSubscribeResponseMessage processRenewal(LocalService service, IncomingSubscribeRequestMessage requestMessage)
  {
    this.subscription = getUpnpService().getRegistry().getLocalSubscription(requestMessage.getSubscriptionId());
    if (this.subscription == null)
    {
      log.fine("Invalid subscription ID for renewal request: " + getInputMessage());
      return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
    }
    log.fine("Renewing subscription: " + this.subscription);
    this.subscription.setSubscriptionDuration(requestMessage.getRequestedTimeoutSeconds());
    if (getUpnpService().getRegistry().updateLocalSubscription(this.subscription)) {
      return new OutgoingSubscribeResponseMessage(this.subscription);
    }
    log.fine("Subscription went away before it could be renewed: " + getInputMessage());
    return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
  }
  
  protected OutgoingSubscribeResponseMessage processNewSubscription(LocalService service, IncomingSubscribeRequestMessage requestMessage)
  {
    List<URL> callbackURLs = requestMessage.getCallbackURLs();
    if ((callbackURLs == null) || (callbackURLs.size() == 0))
    {
      log.fine("Missing or invalid Callback URLs in subscribe request: " + getInputMessage());
      return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
    }
    if (!requestMessage.hasNotificationHeader())
    {
      log.fine("Missing or invalid NT header in subscribe request: " + getInputMessage());
      return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
    }
    Integer timeoutSeconds;
    Integer timeoutSeconds;
    if (getUpnpService().getConfiguration().isReceivedSubscriptionTimeoutIgnored()) {
      timeoutSeconds = null;
    } else {
      timeoutSeconds = requestMessage.getRequestedTimeoutSeconds();
    }
    try
    {
      this.subscription = new LocalGENASubscription(service, timeoutSeconds, callbackURLs)
      {
        public void established() {}
        
        public void ended(CancelReason reason) {}
        
        public void eventReceived()
        {
          ReceivingSubscribe.this.getUpnpService().getConfiguration().getSyncProtocolExecutorService().execute(ReceivingSubscribe.this
            .getUpnpService().getProtocolFactory().createSendingEvent(this));
        }
      };
    }
    catch (Exception ex)
    {
      log.warning("Couldn't create local subscription to service: " + Exceptions.unwrap(ex));
      return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
    }
    log.fine("Adding subscription to registry: " + this.subscription);
    getUpnpService().getRegistry().addLocalSubscription(this.subscription);
    
    log.fine("Returning subscription response, waiting to send initial event");
    return new OutgoingSubscribeResponseMessage(this.subscription);
  }
  
  public void responseSent(StreamResponseMessage responseMessage)
  {
    if (this.subscription == null) {
      return;
    }
    if ((responseMessage != null) && 
      (!((UpnpResponse)responseMessage.getOperation()).isFailed()) && 
      (this.subscription.getCurrentSequence().getValue().longValue() == 0L))
    {
      log.fine("Establishing subscription");
      this.subscription.registerOnService();
      this.subscription.establish();
      
      log.fine("Response to subscription sent successfully, now sending initial event asynchronously");
      getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(
        getUpnpService().getProtocolFactory().createSendingEvent(this.subscription));
    }
    else if (this.subscription.getCurrentSequence().getValue().longValue() == 0L)
    {
      log.fine("Subscription request's response aborted, not sending initial event");
      if (responseMessage == null) {
        log.fine("Reason: No response at all from subscriber");
      } else {
        log.fine("Reason: " + responseMessage.getOperation());
      }
      log.fine("Removing subscription from registry: " + this.subscription);
      getUpnpService().getRegistry().removeLocalSubscription(this.subscription);
    }
  }
  
  public void responseException(Throwable t)
  {
    if (this.subscription == null) {
      return;
    }
    log.fine("Response could not be send to subscriber, removing local GENA subscription: " + this.subscription);
    getUpnpService().getRegistry().removeLocalSubscription(this.subscription);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\sync\ReceivingSubscribe.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */