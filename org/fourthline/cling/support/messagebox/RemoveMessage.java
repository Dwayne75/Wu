package org.fourthline.cling.support.messagebox;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.messagebox.model.Message;

public abstract class RemoveMessage
  extends ActionCallback
{
  public RemoveMessage(Service service, Message message)
  {
    this(service, message.getId());
  }
  
  public RemoveMessage(Service service, int id)
  {
    super(new ActionInvocation(service.getAction("RemoveMessage")));
    getActionInvocation().setInput("MessageID", Integer.valueOf(id));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\messagebox\RemoveMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */