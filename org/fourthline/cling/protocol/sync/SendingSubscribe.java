package org.fourthline.cling.protocol.sync;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.gena.IncomingSubscribeResponseMessage;
import org.fourthline.cling.model.message.gena.OutgoingSubscribeRequestMessage;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.protocol.SendingSync;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;

public class SendingSubscribe
  extends SendingSync<OutgoingSubscribeRequestMessage, IncomingSubscribeResponseMessage>
{
  private static final Logger log = Logger.getLogger(SendingSubscribe.class.getName());
  protected final RemoteGENASubscription subscription;
  
  public SendingSubscribe(UpnpService upnpService, RemoteGENASubscription subscription, List<NetworkAddress> activeStreamServers)
  {
    super(upnpService, new OutgoingSubscribeRequestMessage(subscription, subscription
    
      .getEventCallbackURLs(activeStreamServers, upnpService
      
      .getConfiguration().getNamespace()), upnpService
      
      .getConfiguration().getEventSubscriptionHeaders((RemoteService)subscription.getService())));
    
    this.subscription = subscription;
  }
  
  protected IncomingSubscribeResponseMessage executeSync()
    throws RouterException
  {
    if (!((OutgoingSubscribeRequestMessage)getInputMessage()).hasCallbackURLs())
    {
      log.fine("Subscription failed, no active local callback URLs available (network disabled?)");
      getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
      {
        public void run()
        {
          SendingSubscribe.this.subscription.fail(null);
        }
      });
      return null;
    }
    log.fine("Sending subscription request: " + getInputMessage());
    try
    {
      getUpnpService().getRegistry().registerPendingRemoteSubscription(this.subscription);
      
      StreamResponseMessage response = null;
      IncomingSubscribeResponseMessage localIncomingSubscribeResponseMessage1;
      try
      {
        response = getUpnpService().getRouter().send(getInputMessage());
      }
      catch (RouterException ex)
      {
        onSubscriptionFailure();
        return null;
      }
      if (response == null)
      {
        onSubscriptionFailure();
        return null;
      }
      final IncomingSubscribeResponseMessage responseMessage = new IncomingSubscribeResponseMessage(response);
      if (((UpnpResponse)response.getOperation()).isFailed())
      {
        log.fine("Subscription failed, response was: " + responseMessage);
        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
        {
          public void run()
          {
            SendingSubscribe.this.subscription.fail((UpnpResponse)responseMessage.getOperation());
          }
        });
      }
      else if (!responseMessage.isValidHeaders())
      {
        log.severe("Subscription failed, invalid or missing (SID, Timeout) response headers");
        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
        {
          public void run()
          {
            SendingSubscribe.this.subscription.fail((UpnpResponse)responseMessage.getOperation());
          }
        });
      }
      else
      {
        log.fine("Subscription established, adding to registry, response was: " + response);
        this.subscription.setSubscriptionId(responseMessage.getSubscriptionId());
        this.subscription.setActualSubscriptionDurationSeconds(responseMessage.getSubscriptionDurationSeconds());
        
        getUpnpService().getRegistry().addRemoteSubscription(this.subscription);
        
        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
        {
          public void run()
          {
            SendingSubscribe.this.subscription.establish();
          }
        });
      }
      return responseMessage;
    }
    finally
    {
      getUpnpService().getRegistry().unregisterPendingRemoteSubscription(this.subscription);
    }
  }
  
  protected void onSubscriptionFailure()
  {
    log.fine("Subscription failed");
    getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
    {
      public void run()
      {
        SendingSubscribe.this.subscription.fail(null);
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\sync\SendingSubscribe.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */