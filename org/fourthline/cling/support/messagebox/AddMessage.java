package org.fourthline.cling.support.messagebox;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.messagebox.model.Message;
import org.seamless.util.MimeType;

public abstract class AddMessage
  extends ActionCallback
{
  protected final MimeType mimeType = MimeType.valueOf("text/xml;charset=\"utf-8\"");
  
  public AddMessage(Service service, Message message)
  {
    super(new ActionInvocation(service.getAction("AddMessage")));
    
    getActionInvocation().setInput("MessageID", Integer.toString(message.getId()));
    getActionInvocation().setInput("MessageType", this.mimeType.toString());
    getActionInvocation().setInput("Message", message.toString());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\messagebox\AddMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */