package org.fourthline.cling.protocol.sync;

import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.gena.OutgoingEventRequestMessage;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.protocol.SendingSync;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.spi.GENAEventProcessor;

public class SendingEvent
  extends SendingSync<OutgoingEventRequestMessage, StreamResponseMessage>
{
  private static final Logger log = Logger.getLogger(SendingEvent.class.getName());
  protected final String subscriptionId;
  protected final OutgoingEventRequestMessage[] requestMessages;
  protected final UnsignedIntegerFourBytes currentSequence;
  
  public SendingEvent(UpnpService upnpService, LocalGENASubscription subscription)
  {
    super(upnpService, null);
    
    this.subscriptionId = subscription.getSubscriptionId();
    
    this.requestMessages = new OutgoingEventRequestMessage[subscription.getCallbackURLs().size()];
    int i = 0;
    for (URL url : subscription.getCallbackURLs())
    {
      this.requestMessages[i] = new OutgoingEventRequestMessage(subscription, url);
      getUpnpService().getConfiguration().getGenaEventProcessor().writeBody(this.requestMessages[i]);
      i++;
    }
    this.currentSequence = subscription.getCurrentSequence();
    
    subscription.incrementSequence();
  }
  
  protected StreamResponseMessage executeSync()
    throws RouterException
  {
    log.fine("Sending event for subscription: " + this.subscriptionId);
    
    StreamResponseMessage lastResponse = null;
    for (OutgoingEventRequestMessage requestMessage : this.requestMessages)
    {
      if (this.currentSequence.getValue().longValue() == 0L) {
        log.fine("Sending initial event message to callback URL: " + requestMessage.getUri());
      } else {
        log.fine("Sending event message '" + this.currentSequence + "' to callback URL: " + requestMessage.getUri());
      }
      lastResponse = getUpnpService().getRouter().send(requestMessage);
      log.fine("Received event callback response: " + lastResponse);
    }
    return lastResponse;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\sync\SendingEvent.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */