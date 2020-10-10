package org.fourthline.cling.protocol.sync;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionCancelledException;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionExecutor;
import org.fourthline.cling.model.action.RemoteActionInvocation;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.control.IncomingActionRequestMessage;
import org.fourthline.cling.model.message.control.OutgoingActionResponseMessage;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.resource.ServiceControlResource;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.protocol.ReceivingSync;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.seamless.util.Exceptions;

public class ReceivingAction
  extends ReceivingSync<StreamRequestMessage, StreamResponseMessage>
{
  private static final Logger log = Logger.getLogger(ReceivingAction.class.getName());
  
  public ReceivingAction(UpnpService upnpService, StreamRequestMessage inputMessage)
  {
    super(upnpService, inputMessage);
  }
  
  protected StreamResponseMessage executeSync()
    throws RouterException
  {
    ContentTypeHeader contentTypeHeader = (ContentTypeHeader)((StreamRequestMessage)getInputMessage()).getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class);
    if ((contentTypeHeader != null) && (!contentTypeHeader.isUDACompliantXML()))
    {
      log.warning("Received invalid Content-Type '" + contentTypeHeader + "': " + getInputMessage());
      return new StreamResponseMessage(new UpnpResponse(UpnpResponse.Status.UNSUPPORTED_MEDIA_TYPE));
    }
    if (contentTypeHeader == null) {
      log.warning("Received without Content-Type: " + getInputMessage());
    }
    ServiceControlResource resource = (ServiceControlResource)getUpnpService().getRegistry().getResource(ServiceControlResource.class, 
    
      ((StreamRequestMessage)getInputMessage()).getUri());
    if (resource == null)
    {
      log.fine("No local resource found: " + getInputMessage());
      return null;
    }
    log.fine("Found local action resource matching relative request URI: " + ((StreamRequestMessage)getInputMessage()).getUri());
    
    OutgoingActionResponseMessage responseMessage = null;
    RemoteActionInvocation invocation;
    try
    {
      IncomingActionRequestMessage requestMessage = new IncomingActionRequestMessage((StreamRequestMessage)getInputMessage(), (LocalService)resource.getModel());
      
      log.finer("Created incoming action request message: " + requestMessage);
      RemoteActionInvocation invocation = new RemoteActionInvocation(requestMessage.getAction(), getRemoteClientInfo());
      
      log.fine("Reading body of request message");
      getUpnpService().getConfiguration().getSoapActionProcessor().readBody(requestMessage, invocation);
      
      log.fine("Executing on local service: " + invocation);
      ((LocalService)resource.getModel()).getExecutor(invocation.getAction()).execute(invocation);
      if (invocation.getFailure() == null)
      {
        responseMessage = new OutgoingActionResponseMessage(invocation.getAction());
      }
      else
      {
        if ((invocation.getFailure() instanceof ActionCancelledException))
        {
          log.fine("Action execution was cancelled, returning 404 to client");
          
          return null;
        }
        responseMessage = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR, invocation.getAction());
      }
    }
    catch (ActionException ex)
    {
      log.finer("Error executing local action: " + ex);
      
      invocation = new RemoteActionInvocation(ex, getRemoteClientInfo());
      responseMessage = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
    }
    catch (UnsupportedDataException ex)
    {
      log.log(Level.WARNING, "Error reading action request XML body: " + ex.toString(), Exceptions.unwrap(ex));
      
      invocation = new RemoteActionInvocation((Exceptions.unwrap(ex) instanceof ActionException) ? (ActionException)Exceptions.unwrap(ex) : new ActionException(ErrorCode.ACTION_FAILED, ex.getMessage()), getRemoteClientInfo());
      
      responseMessage = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
    }
    try
    {
      log.fine("Writing body of response message");
      getUpnpService().getConfiguration().getSoapActionProcessor().writeBody(responseMessage, invocation);
      
      log.fine("Returning finished response message: " + responseMessage);
      return responseMessage;
    }
    catch (UnsupportedDataException ex)
    {
      log.warning("Failure writing body of response message, sending '500 Internal Server Error' without body");
      log.log(Level.WARNING, "Exception root cause: ", Exceptions.unwrap(ex));
    }
    return new StreamResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\sync\ReceivingAction.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */