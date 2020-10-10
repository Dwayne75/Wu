package org.fourthline.cling.model.message.control;

import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;

public class IncomingActionResponseMessage
  extends StreamResponseMessage
  implements ActionResponseMessage
{
  public IncomingActionResponseMessage(StreamResponseMessage source)
  {
    super(source);
  }
  
  public IncomingActionResponseMessage(UpnpResponse operation)
  {
    super(operation);
  }
  
  public String getActionNamespace()
  {
    return null;
  }
  
  public boolean isFailedNonRecoverable()
  {
    int statusCode = ((UpnpResponse)getOperation()).getStatusCode();
    
    return (((UpnpResponse)getOperation()).isFailed()) && (statusCode != UpnpResponse.Status.METHOD_NOT_SUPPORTED.getStatusCode()) && ((statusCode != UpnpResponse.Status.INTERNAL_SERVER_ERROR.getStatusCode()) || (!hasBody()));
  }
  
  public boolean isFailedRecoverable()
  {
    return (hasBody()) && (((UpnpResponse)getOperation()).getStatusCode() == UpnpResponse.Status.INTERNAL_SERVER_ERROR.getStatusCode());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\control\IncomingActionResponseMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */