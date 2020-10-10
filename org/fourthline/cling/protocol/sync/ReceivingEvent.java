package org.fourthline.cling.protocol.sync;

import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.gena.IncomingEventRequestMessage;
import org.fourthline.cling.model.message.gena.OutgoingEventResponseMessage;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.resource.ServiceEventCallbackResource;
import org.fourthline.cling.protocol.ReceivingSync;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.spi.GENAEventProcessor;

public class ReceivingEvent
  extends ReceivingSync<StreamRequestMessage, OutgoingEventResponseMessage>
{
  private static final Logger log = Logger.getLogger(ReceivingEvent.class.getName());
  
  public ReceivingEvent(UpnpService upnpService, StreamRequestMessage inputMessage)
  {
    super(upnpService, inputMessage);
  }
  
  protected OutgoingEventResponseMessage executeSync()
    throws RouterException
  {
    if (!((StreamRequestMessage)getInputMessage()).isContentTypeTextUDA()) {
      log.warning("Received without or with invalid Content-Type: " + getInputMessage());
    }
    ServiceEventCallbackResource resource = (ServiceEventCallbackResource)getUpnpService().getRegistry().getResource(ServiceEventCallbackResource.class, 
    
      ((StreamRequestMessage)getInputMessage()).getUri());
    if (resource == null)
    {
      log.fine("No local resource found: " + getInputMessage());
      return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.NOT_FOUND));
    }
    final IncomingEventRequestMessage requestMessage = new IncomingEventRequestMessage((StreamRequestMessage)getInputMessage(), (RemoteService)resource.getModel());
    if (requestMessage.getSubscrptionId() == null)
    {
      log.fine("Subscription ID missing in event request: " + getInputMessage());
      return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
    }
    if (!requestMessage.hasValidNotificationHeaders())
    {
      log.fine("Missing NT and/or NTS headers in event request: " + getInputMessage());
      return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.BAD_REQUEST));
    }
    if (!requestMessage.hasValidNotificationHeaders())
    {
      log.fine("Invalid NT and/or NTS headers in event request: " + getInputMessage());
      return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
    }
    if (requestMessage.getSequence() == null)
    {
      log.fine("Sequence missing in event request: " + getInputMessage());
      return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
    }
    try
    {
      getUpnpService().getConfiguration().getGenaEventProcessor().readBody(requestMessage);
    }
    catch (UnsupportedDataException ex)
    {
      log.fine("Can't read event message request body, " + ex);
      
      final RemoteGENASubscription subscription = getUpnpService().getRegistry().getRemoteSubscription(requestMessage.getSubscrptionId());
      if (subscription != null) {
        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
        {
          public void run()
          {
            subscription.invalidMessage(ex);
          }
        });
      }
      return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.INTERNAL_SERVER_ERROR));
    }
    final RemoteGENASubscription subscription = getUpnpService().getRegistry().getWaitRemoteSubscription(requestMessage.getSubscrptionId());
    if (subscription == null)
    {
      log.severe("Invalid subscription ID, no active subscription: " + requestMessage);
      return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
    }
    getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
    {
      public void run()
      {
        ReceivingEvent.log.fine("Calling active subscription with event state variable values");
        subscription.receive(requestMessage
          .getSequence(), requestMessage
          .getStateVariableValues());
      }
    });
    return new OutgoingEventResponseMessage();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\sync\ReceivingEvent.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */