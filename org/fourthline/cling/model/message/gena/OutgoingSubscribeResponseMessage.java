package org.fourthline.cling.model.message.gena;

import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.header.ServerHeader;
import org.fourthline.cling.model.message.header.SubscriptionIdHeader;
import org.fourthline.cling.model.message.header.TimeoutHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;

public class OutgoingSubscribeResponseMessage
  extends StreamResponseMessage
{
  public OutgoingSubscribeResponseMessage(UpnpResponse.Status status)
  {
    super(status);
  }
  
  public OutgoingSubscribeResponseMessage(LocalGENASubscription subscription)
  {
    super(new UpnpResponse(UpnpResponse.Status.OK));
    
    getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());
    getHeaders().add(UpnpHeader.Type.SID, new SubscriptionIdHeader(subscription.getSubscriptionId()));
    getHeaders().add(UpnpHeader.Type.TIMEOUT, new TimeoutHeader(subscription.getActualDurationSeconds()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\gena\OutgoingSubscribeResponseMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */