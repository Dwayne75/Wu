package org.fourthline.cling.protocol.sync;

import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.gena.IncomingSubscribeResponseMessage;
import org.fourthline.cling.model.message.gena.OutgoingRenewalRequestMessage;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.protocol.SendingSync;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;

public class SendingRenewal
  extends SendingSync<OutgoingRenewalRequestMessage, IncomingSubscribeResponseMessage>
{
  private static final Logger log = Logger.getLogger(SendingRenewal.class.getName());
  protected final RemoteGENASubscription subscription;
  
  public SendingRenewal(UpnpService upnpService, RemoteGENASubscription subscription)
  {
    super(upnpService, new OutgoingRenewalRequestMessage(subscription, upnpService
    
      .getConfiguration().getEventSubscriptionHeaders((RemoteService)subscription.getService())));
    
    this.subscription = subscription;
  }
  
  protected IncomingSubscribeResponseMessage executeSync()
    throws RouterException
  {
    log.fine("Sending subscription renewal request: " + getInputMessage());
    try
    {
      response = getUpnpService().getRouter().send(getInputMessage());
    }
    catch (RouterException ex)
    {
      StreamResponseMessage response;
      onRenewalFailure();
      throw ex;
    }
    StreamResponseMessage response;
    if (response == null)
    {
      onRenewalFailure();
      return null;
    }
    final IncomingSubscribeResponseMessage responseMessage = new IncomingSubscribeResponseMessage(response);
    if (((UpnpResponse)response.getOperation()).isFailed())
    {
      log.fine("Subscription renewal failed, response was: " + response);
      getUpnpService().getRegistry().removeRemoteSubscription(this.subscription);
      getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
      {
        public void run()
        {
          SendingRenewal.this.subscription.end(CancelReason.RENEWAL_FAILED, (UpnpResponse)responseMessage.getOperation());
        }
      });
    }
    else if (!responseMessage.isValidHeaders())
    {
      log.severe("Subscription renewal failed, invalid or missing (SID, Timeout) response headers");
      getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
      {
        public void run()
        {
          SendingRenewal.this.subscription.end(CancelReason.RENEWAL_FAILED, (UpnpResponse)responseMessage.getOperation());
        }
      });
    }
    else
    {
      log.fine("Subscription renewed, updating in registry, response was: " + response);
      this.subscription.setActualSubscriptionDurationSeconds(responseMessage.getSubscriptionDurationSeconds());
      getUpnpService().getRegistry().updateRemoteSubscription(this.subscription);
    }
    return responseMessage;
  }
  
  protected void onRenewalFailure()
  {
    log.fine("Subscription renewal failed, removing subscription from registry");
    getUpnpService().getRegistry().removeRemoteSubscription(this.subscription);
    getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
    {
      public void run()
      {
        SendingRenewal.this.subscription.end(CancelReason.RENEWAL_FAILED, null);
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\sync\SendingRenewal.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */