package org.fourthline.cling.protocol;

import java.net.URL;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.protocol.async.SendingNotificationAlive;
import org.fourthline.cling.protocol.async.SendingNotificationByebye;
import org.fourthline.cling.protocol.async.SendingSearch;
import org.fourthline.cling.protocol.sync.SendingAction;
import org.fourthline.cling.protocol.sync.SendingEvent;
import org.fourthline.cling.protocol.sync.SendingRenewal;
import org.fourthline.cling.protocol.sync.SendingSubscribe;
import org.fourthline.cling.protocol.sync.SendingUnsubscribe;

public abstract interface ProtocolFactory
{
  public abstract UpnpService getUpnpService();
  
  public abstract ReceivingAsync createReceivingAsync(IncomingDatagramMessage paramIncomingDatagramMessage)
    throws ProtocolCreationException;
  
  public abstract ReceivingSync createReceivingSync(StreamRequestMessage paramStreamRequestMessage)
    throws ProtocolCreationException;
  
  public abstract SendingNotificationAlive createSendingNotificationAlive(LocalDevice paramLocalDevice);
  
  public abstract SendingNotificationByebye createSendingNotificationByebye(LocalDevice paramLocalDevice);
  
  public abstract SendingSearch createSendingSearch(UpnpHeader paramUpnpHeader, int paramInt);
  
  public abstract SendingAction createSendingAction(ActionInvocation paramActionInvocation, URL paramURL);
  
  public abstract SendingSubscribe createSendingSubscribe(RemoteGENASubscription paramRemoteGENASubscription)
    throws ProtocolCreationException;
  
  public abstract SendingRenewal createSendingRenewal(RemoteGENASubscription paramRemoteGENASubscription);
  
  public abstract SendingUnsubscribe createSendingUnsubscribe(RemoteGENASubscription paramRemoteGENASubscription);
  
  public abstract SendingEvent createSendingEvent(LocalGENASubscription paramLocalGENASubscription);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\ProtocolFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */