package org.fourthline.cling.controlpoint;

import java.net.URL;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionExecutor;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.control.IncomingActionResponseMessage;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.sync.SendingAction;

public abstract class ActionCallback
  implements Runnable
{
  protected final ActionInvocation actionInvocation;
  protected ControlPoint controlPoint;
  
  public static final class Default
    extends ActionCallback
  {
    public Default(ActionInvocation actionInvocation, ControlPoint controlPoint)
    {
      super(controlPoint);
    }
    
    public void success(ActionInvocation invocation) {}
    
    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {}
  }
  
  protected ActionCallback(ActionInvocation actionInvocation, ControlPoint controlPoint)
  {
    this.actionInvocation = actionInvocation;
    this.controlPoint = controlPoint;
  }
  
  protected ActionCallback(ActionInvocation actionInvocation)
  {
    this.actionInvocation = actionInvocation;
  }
  
  public ActionInvocation getActionInvocation()
  {
    return this.actionInvocation;
  }
  
  public synchronized ControlPoint getControlPoint()
  {
    return this.controlPoint;
  }
  
  public synchronized ActionCallback setControlPoint(ControlPoint controlPoint)
  {
    this.controlPoint = controlPoint;
    return this;
  }
  
  public void run()
  {
    Service service = this.actionInvocation.getAction().getService();
    if ((service instanceof LocalService))
    {
      LocalService localService = (LocalService)service;
      
      localService.getExecutor(this.actionInvocation.getAction()).execute(this.actionInvocation);
      if (this.actionInvocation.getFailure() != null) {
        failure(this.actionInvocation, null);
      } else {
        success(this.actionInvocation);
      }
    }
    else if ((service instanceof RemoteService))
    {
      if (getControlPoint() == null) {
        throw new IllegalStateException("Callback must be executed through ControlPoint");
      }
      RemoteService remoteService = (RemoteService)service;
      try
      {
        controLURL = ((RemoteDevice)remoteService.getDevice()).normalizeURI(remoteService.getControlURI());
      }
      catch (IllegalArgumentException e)
      {
        URL controLURL;
        failure(this.actionInvocation, null, "bad control URL: " + remoteService.getControlURI()); return;
      }
      URL controLURL;
      SendingAction prot = getControlPoint().getProtocolFactory().createSendingAction(this.actionInvocation, controLURL);
      prot.run();
      
      IncomingActionResponseMessage response = (IncomingActionResponseMessage)prot.getOutputMessage();
      if (response == null) {
        failure(this.actionInvocation, null);
      } else if (((UpnpResponse)response.getOperation()).isFailed()) {
        failure(this.actionInvocation, (UpnpResponse)response.getOperation());
      } else {
        success(this.actionInvocation);
      }
    }
  }
  
  protected String createDefaultFailureMessage(ActionInvocation invocation, UpnpResponse operation)
  {
    String message = "Error: ";
    ActionException exception = invocation.getFailure();
    if (exception != null) {
      message = message + exception.getMessage();
    }
    if (operation != null) {
      message = message + " (HTTP response was: " + operation.getResponseDetails() + ")";
    }
    return message;
  }
  
  protected void failure(ActionInvocation invocation, UpnpResponse operation)
  {
    failure(invocation, operation, createDefaultFailureMessage(invocation, operation));
  }
  
  public abstract void success(ActionInvocation paramActionInvocation);
  
  public abstract void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse, String paramString);
  
  public String toString()
  {
    return "(ActionCallback) " + this.actionInvocation;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\controlpoint\ActionCallback.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */