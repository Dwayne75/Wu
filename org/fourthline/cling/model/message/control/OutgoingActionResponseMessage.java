package org.fourthline.cling.model.message.control;

import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.EXTHeader;
import org.fourthline.cling.model.message.header.ServerHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.QueryStateVariableAction;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ServiceType;

public class OutgoingActionResponseMessage
  extends StreamResponseMessage
  implements ActionResponseMessage
{
  private String actionNamespace;
  
  public OutgoingActionResponseMessage(Action action)
  {
    this(UpnpResponse.Status.OK, action);
  }
  
  public OutgoingActionResponseMessage(UpnpResponse.Status status)
  {
    this(status, null);
  }
  
  public OutgoingActionResponseMessage(UpnpResponse.Status status, Action action)
  {
    super(new UpnpResponse(status));
    if (action != null) {
      if ((action instanceof QueryStateVariableAction)) {
        this.actionNamespace = "urn:schemas-upnp-org:control-1-0";
      } else {
        this.actionNamespace = action.getService().getServiceType().toString();
      }
    }
    addHeaders();
  }
  
  protected void addHeaders()
  {
    getHeaders().add(UpnpHeader.Type.CONTENT_TYPE, new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8));
    
    getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());
    
    getHeaders().add(UpnpHeader.Type.EXT, new EXTHeader());
  }
  
  public String getActionNamespace()
  {
    return this.actionNamespace;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\control\OutgoingActionResponseMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */