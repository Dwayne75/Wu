package org.fourthline.cling.model.message.gena;

import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest.Method;
import org.fourthline.cling.model.message.header.SubscriptionIdHeader;
import org.fourthline.cling.model.message.header.TimeoutHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;

public class OutgoingRenewalRequestMessage
  extends StreamRequestMessage
{
  public OutgoingRenewalRequestMessage(RemoteGENASubscription subscription, UpnpHeaders extraHeaders)
  {
    super(UpnpRequest.Method.SUBSCRIBE, subscription.getEventSubscriptionURL());
    
    getHeaders().add(UpnpHeader.Type.SID, new SubscriptionIdHeader(subscription
    
      .getSubscriptionId()));
    
    getHeaders().add(UpnpHeader.Type.TIMEOUT, new TimeoutHeader(subscription
    
      .getRequestedDurationSeconds()));
    if (extraHeaders != null) {
      getHeaders().putAll(extraHeaders);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\gena\OutgoingRenewalRequestMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */