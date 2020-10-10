package org.fourthline.cling.protocol.sync;

import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.gena.OutgoingUnsubscribeRequestMessage;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.protocol.SendingSync;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;

public class SendingUnsubscribe
  extends SendingSync<OutgoingUnsubscribeRequestMessage, StreamResponseMessage>
{
  private static final Logger log = Logger.getLogger(SendingUnsubscribe.class.getName());
  protected final RemoteGENASubscription subscription;
  
  public SendingUnsubscribe(UpnpService upnpService, RemoteGENASubscription subscription)
  {
    super(upnpService, new OutgoingUnsubscribeRequestMessage(subscription, upnpService
    
      .getConfiguration().getEventSubscriptionHeaders((RemoteService)subscription.getService())));
    
    this.subscription = subscription;
  }
  
  protected StreamResponseMessage executeSync()
    throws RouterException
  {
    log.fine("Sending unsubscribe request: " + getInputMessage());
    
    StreamResponseMessage response = null;
    try
    {
      response = getUpnpService().getRouter().send(getInputMessage());
      return response;
    }
    finally
    {
      onUnsubscribe(response);
    }
  }
  
  protected void onUnsubscribe(final StreamResponseMessage response)
  {
    getUpnpService().getRegistry().removeRemoteSubscription(this.subscription);
    
    getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
    {
      public void run()
      {
        if (response == null)
        {
          SendingUnsubscribe.log.fine("Unsubscribe failed, no response received");
          SendingUnsubscribe.this.subscription.end(CancelReason.UNSUBSCRIBE_FAILED, null);
        }
        else if (((UpnpResponse)response.getOperation()).isFailed())
        {
          SendingUnsubscribe.log.fine("Unsubscribe failed, response was: " + response);
          SendingUnsubscribe.this.subscription.end(CancelReason.UNSUBSCRIBE_FAILED, (UpnpResponse)response.getOperation());
        }
        else
        {
          SendingUnsubscribe.log.fine("Unsubscribe successful, response was: " + response);
          SendingUnsubscribe.this.subscription.end(null, (UpnpResponse)response.getOperation());
        }
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\sync\SendingUnsubscribe.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */