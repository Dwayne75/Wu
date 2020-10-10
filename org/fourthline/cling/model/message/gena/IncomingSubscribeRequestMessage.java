package org.fourthline.cling.model.message.gena;

import java.net.URL;
import java.util.List;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.CallbackHeader;
import org.fourthline.cling.model.message.header.NTEventHeader;
import org.fourthline.cling.model.message.header.SubscriptionIdHeader;
import org.fourthline.cling.model.message.header.TimeoutHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.LocalService;

public class IncomingSubscribeRequestMessage
  extends StreamRequestMessage
{
  private final LocalService service;
  
  public IncomingSubscribeRequestMessage(StreamRequestMessage source, LocalService service)
  {
    super(source);
    this.service = service;
  }
  
  public LocalService getService()
  {
    return this.service;
  }
  
  public List<URL> getCallbackURLs()
  {
    CallbackHeader header = (CallbackHeader)getHeaders().getFirstHeader(UpnpHeader.Type.CALLBACK, CallbackHeader.class);
    return header != null ? (List)header.getValue() : null;
  }
  
  public boolean hasNotificationHeader()
  {
    return getHeaders().getFirstHeader(UpnpHeader.Type.NT, NTEventHeader.class) != null;
  }
  
  public Integer getRequestedTimeoutSeconds()
  {
    TimeoutHeader timeoutHeader = (TimeoutHeader)getHeaders().getFirstHeader(UpnpHeader.Type.TIMEOUT, TimeoutHeader.class);
    return timeoutHeader != null ? (Integer)timeoutHeader.getValue() : null;
  }
  
  public String getSubscriptionId()
  {
    SubscriptionIdHeader header = (SubscriptionIdHeader)getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class);
    return header != null ? (String)header.getValue() : null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\gena\IncomingSubscribeRequestMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */