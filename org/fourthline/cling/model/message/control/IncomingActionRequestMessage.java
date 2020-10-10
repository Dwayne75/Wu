package org.fourthline.cling.model.message.control;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.SoapActionHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.SoapActionType;

public class IncomingActionRequestMessage
  extends StreamRequestMessage
  implements ActionRequestMessage
{
  private final Action action;
  private final String actionNamespace;
  
  public IncomingActionRequestMessage(StreamRequestMessage source, LocalService service)
    throws ActionException
  {
    super(source);
    
    SoapActionHeader soapActionHeader = (SoapActionHeader)getHeaders().getFirstHeader(UpnpHeader.Type.SOAPACTION, SoapActionHeader.class);
    if (soapActionHeader == null) {
      throw new ActionException(ErrorCode.INVALID_ACTION, "Missing SOAP action header");
    }
    SoapActionType actionType = (SoapActionType)soapActionHeader.getValue();
    
    this.action = service.getAction(actionType.getActionName());
    if (this.action == null) {
      throw new ActionException(ErrorCode.INVALID_ACTION, "Service doesn't implement action: " + actionType.getActionName());
    }
    if ((!"QueryStateVariable".equals(actionType.getActionName())) && 
      (!service.getServiceType().implementsVersion(actionType.getServiceType()))) {
      throw new ActionException(ErrorCode.INVALID_ACTION, "Service doesn't support the requested service version");
    }
    this.actionNamespace = actionType.getTypeString();
  }
  
  public Action getAction()
  {
    return this.action;
  }
  
  public String getActionNamespace()
  {
    return this.actionNamespace;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\control\IncomingActionRequestMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */