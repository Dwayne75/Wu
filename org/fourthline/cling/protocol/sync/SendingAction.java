package org.fourthline.cling.protocol.sync;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionCancelledException;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.control.IncomingActionResponseMessage;
import org.fourthline.cling.model.message.control.OutgoingActionRequestMessage;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.protocol.SendingSync;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.seamless.util.Exceptions;

public class SendingAction
  extends SendingSync<OutgoingActionRequestMessage, IncomingActionResponseMessage>
{
  private static final Logger log = Logger.getLogger(SendingAction.class.getName());
  protected final ActionInvocation actionInvocation;
  
  public SendingAction(UpnpService upnpService, ActionInvocation actionInvocation, URL controlURL)
  {
    super(upnpService, new OutgoingActionRequestMessage(actionInvocation, controlURL));
    this.actionInvocation = actionInvocation;
  }
  
  protected IncomingActionResponseMessage executeSync()
    throws RouterException
  {
    return invokeRemote((OutgoingActionRequestMessage)getInputMessage());
  }
  
  protected IncomingActionResponseMessage invokeRemote(OutgoingActionRequestMessage requestMessage)
    throws RouterException
  {
    Device device = this.actionInvocation.getAction().getService().getDevice();
    
    log.fine("Sending outgoing action call '" + this.actionInvocation.getAction().getName() + "' to remote service of: " + device);
    IncomingActionResponseMessage responseMessage = null;
    try
    {
      StreamResponseMessage streamResponse = sendRemoteRequest(requestMessage);
      if (streamResponse == null)
      {
        log.fine("No connection or no no response received, returning null");
        this.actionInvocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, "Connection error or no response received"));
        return null;
      }
      responseMessage = new IncomingActionResponseMessage(streamResponse);
      if (responseMessage.isFailedNonRecoverable())
      {
        log.fine("Response was a non-recoverable failure: " + responseMessage);
        
        throw new ActionException(ErrorCode.ACTION_FAILED, "Non-recoverable remote execution failure: " + ((UpnpResponse)responseMessage.getOperation()).getResponseDetails());
      }
      if (responseMessage.isFailedRecoverable()) {
        handleResponseFailure(responseMessage);
      } else {
        handleResponse(responseMessage);
      }
      return responseMessage;
    }
    catch (ActionException ex)
    {
      log.fine("Remote action invocation failed, returning Internal Server Error message: " + ex.getMessage());
      this.actionInvocation.setFailure(ex);
      if ((responseMessage == null) || (!((UpnpResponse)responseMessage.getOperation()).isFailed())) {
        return new IncomingActionResponseMessage(new UpnpResponse(UpnpResponse.Status.INTERNAL_SERVER_ERROR));
      }
    }
    return responseMessage;
  }
  
  protected StreamResponseMessage sendRemoteRequest(OutgoingActionRequestMessage requestMessage)
    throws ActionException, RouterException
  {
    try
    {
      log.fine("Writing SOAP request body of: " + requestMessage);
      getUpnpService().getConfiguration().getSoapActionProcessor().writeBody(requestMessage, this.actionInvocation);
      
      log.fine("Sending SOAP body of message as stream to remote device");
      return getUpnpService().getRouter().send(requestMessage);
    }
    catch (RouterException ex)
    {
      Throwable cause = Exceptions.unwrap(ex);
      if ((cause instanceof InterruptedException))
      {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Sending action request message was interrupted: " + cause);
        }
        throw new ActionCancelledException((InterruptedException)cause);
      }
      throw ex;
    }
    catch (UnsupportedDataException ex)
    {
      if (log.isLoggable(Level.FINE))
      {
        log.fine("Error writing SOAP body: " + ex);
        log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
      }
      throw new ActionException(ErrorCode.ACTION_FAILED, "Error writing request message. " + ex.getMessage());
    }
  }
  
  protected void handleResponse(IncomingActionResponseMessage responseMsg)
    throws ActionException
  {
    try
    {
      log.fine("Received response for outgoing call, reading SOAP response body: " + responseMsg);
      getUpnpService().getConfiguration().getSoapActionProcessor().readBody(responseMsg, this.actionInvocation);
    }
    catch (UnsupportedDataException ex)
    {
      log.fine("Error reading SOAP body: " + ex);
      log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
      
      throw new ActionException(ErrorCode.ACTION_FAILED, "Error reading SOAP response message. " + ex.getMessage(), false);
    }
  }
  
  protected void handleResponseFailure(IncomingActionResponseMessage responseMsg)
    throws ActionException
  {
    try
    {
      log.fine("Received response with Internal Server Error, reading SOAP failure message");
      getUpnpService().getConfiguration().getSoapActionProcessor().readBody(responseMsg, this.actionInvocation);
    }
    catch (UnsupportedDataException ex)
    {
      log.fine("Error reading SOAP body: " + ex);
      log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
      
      throw new ActionException(ErrorCode.ACTION_FAILED, "Error reading SOAP response failure message. " + ex.getMessage(), false);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\sync\SendingAction.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */