package org.fourthline.cling.model.message.gena;

import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;

public class OutgoingEventResponseMessage
  extends StreamResponseMessage
{
  public OutgoingEventResponseMessage()
  {
    super(new UpnpResponse(UpnpResponse.Status.OK));
    getHeaders().add(UpnpHeader.Type.CONTENT_TYPE, new ContentTypeHeader());
  }
  
  public OutgoingEventResponseMessage(UpnpResponse operation)
  {
    super(operation);
    getHeaders().add(UpnpHeader.Type.CONTENT_TYPE, new ContentTypeHeader());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\gena\OutgoingEventResponseMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */