package org.fourthline.cling.model.message.gena;

import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.SubscriptionIdHeader;
import org.fourthline.cling.model.message.header.TimeoutHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;

public class IncomingSubscribeResponseMessage
  extends StreamResponseMessage
{
  public IncomingSubscribeResponseMessage(StreamResponseMessage source)
  {
    super(source);
  }
  
  public boolean isValidHeaders()
  {
    return (getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class) != null) && (getHeaders().getFirstHeader(UpnpHeader.Type.TIMEOUT, TimeoutHeader.class) != null);
  }
  
  public String getSubscriptionId()
  {
    return (String)((SubscriptionIdHeader)getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class)).getValue();
  }
  
  public int getSubscriptionDurationSeconds()
  {
    return ((Integer)((TimeoutHeader)getHeaders().getFirstHeader(UpnpHeader.Type.TIMEOUT, TimeoutHeader.class)).getValue()).intValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\gena\IncomingSubscribeResponseMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */