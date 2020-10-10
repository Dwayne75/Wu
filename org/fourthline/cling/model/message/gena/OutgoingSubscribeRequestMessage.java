package org.fourthline.cling.model.message.gena;

import java.net.URL;
import java.util.List;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest.Method;
import org.fourthline.cling.model.message.header.CallbackHeader;
import org.fourthline.cling.model.message.header.NTEventHeader;
import org.fourthline.cling.model.message.header.TimeoutHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;

public class OutgoingSubscribeRequestMessage
  extends StreamRequestMessage
{
  public OutgoingSubscribeRequestMessage(RemoteGENASubscription subscription, List<URL> callbackURLs, UpnpHeaders extraHeaders)
  {
    super(UpnpRequest.Method.SUBSCRIBE, subscription.getEventSubscriptionURL());
    
    getHeaders().add(UpnpHeader.Type.CALLBACK, new CallbackHeader(callbackURLs));
    
    getHeaders().add(UpnpHeader.Type.NT, new NTEventHeader());
    
    getHeaders().add(UpnpHeader.Type.TIMEOUT, new TimeoutHeader(subscription
    
      .getRequestedDurationSeconds()));
    if (extraHeaders != null) {
      getHeaders().putAll(extraHeaders);
    }
  }
  
  public boolean hasCallbackURLs()
  {
    CallbackHeader callbackHeader = (CallbackHeader)getHeaders().getFirstHeader(UpnpHeader.Type.CALLBACK, CallbackHeader.class);
    return ((List)callbackHeader.getValue()).size() > 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\gena\OutgoingSubscribeRequestMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */