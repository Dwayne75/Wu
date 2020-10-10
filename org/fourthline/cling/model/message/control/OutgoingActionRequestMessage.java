package org.fourthline.cling.model.message.control;

import java.net.URL;
import java.util.logging.Logger;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.action.RemoteActionInvocation;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpRequest.Method;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.SoapActionHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.message.header.UserAgentHeader;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.QueryStateVariableAction;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.profile.ClientInfo;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.model.types.SoapActionType;

public class OutgoingActionRequestMessage
  extends StreamRequestMessage
  implements ActionRequestMessage
{
  private static Logger log = Logger.getLogger(OutgoingActionRequestMessage.class.getName());
  private final String actionNamespace;
  
  public OutgoingActionRequestMessage(ActionInvocation actionInvocation, URL controlURL)
  {
    this(actionInvocation.getAction(), new UpnpRequest(UpnpRequest.Method.POST, controlURL));
    if ((actionInvocation instanceof RemoteActionInvocation))
    {
      RemoteActionInvocation remoteActionInvocation = (RemoteActionInvocation)actionInvocation;
      if ((remoteActionInvocation.getRemoteClientInfo() != null) && 
        (remoteActionInvocation.getRemoteClientInfo().getRequestUserAgent() != null)) {
        getHeaders().add(UpnpHeader.Type.USER_AGENT, new UserAgentHeader(remoteActionInvocation
        
          .getRemoteClientInfo().getRequestUserAgent()));
      }
    }
    else if (actionInvocation.getClientInfo() != null)
    {
      getHeaders().putAll(actionInvocation.getClientInfo().getRequestHeaders());
    }
  }
  
  public OutgoingActionRequestMessage(Action action, UpnpRequest operation)
  {
    super(operation);
    
    getHeaders().add(UpnpHeader.Type.CONTENT_TYPE, new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8));
    SoapActionHeader soapActionHeader;
    SoapActionHeader soapActionHeader;
    if ((action instanceof QueryStateVariableAction))
    {
      log.fine("Adding magic control SOAP action header for state variable query action");
      
      soapActionHeader = new SoapActionHeader(new SoapActionType("schemas-upnp-org", "control-1-0", null, action.getName()));
    }
    else
    {
      soapActionHeader = new SoapActionHeader(new SoapActionType(action.getService().getServiceType(), action.getName()));
    }
    this.actionNamespace = ((SoapActionType)soapActionHeader.getValue()).getTypeString();
    if (((UpnpRequest)getOperation()).getMethod().equals(UpnpRequest.Method.POST))
    {
      getHeaders().add(UpnpHeader.Type.SOAPACTION, soapActionHeader);
      log.fine("Added SOAP action header: " + soapActionHeader);
    }
    else
    {
      throw new IllegalArgumentException("Can't send action with request method: " + ((UpnpRequest)getOperation()).getMethod());
    }
  }
  
  public String getActionNamespace()
  {
    return this.actionNamespace;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\control\OutgoingActionRequestMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */