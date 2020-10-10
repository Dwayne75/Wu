package org.fourthline.cling.protocol.async;

import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.discovery.IncomingNotificationRequest;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.ReceivingAsync;
import org.fourthline.cling.protocol.RetrieveRemoteDescriptors;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.RouterException;

public class ReceivingNotification
  extends ReceivingAsync<IncomingNotificationRequest>
{
  private static final Logger log = Logger.getLogger(ReceivingNotification.class.getName());
  
  public ReceivingNotification(UpnpService upnpService, IncomingDatagramMessage<UpnpRequest> inputMessage)
  {
    super(upnpService, new IncomingNotificationRequest(inputMessage));
  }
  
  protected void execute()
    throws RouterException
  {
    UDN udn = ((IncomingNotificationRequest)getInputMessage()).getUDN();
    if (udn == null)
    {
      log.fine("Ignoring notification message without UDN: " + getInputMessage());
      return;
    }
    RemoteDeviceIdentity rdIdentity = new RemoteDeviceIdentity((IncomingNotificationRequest)getInputMessage());
    log.fine("Received device notification: " + rdIdentity);
    try
    {
      rd = new RemoteDevice(rdIdentity);
    }
    catch (ValidationException ex)
    {
      RemoteDevice rd;
      log.warning("Validation errors of device during discovery: " + rdIdentity);
      for (ValidationError validationError : ex.getErrors()) {
        log.warning(validationError.toString());
      }
      return;
    }
    RemoteDevice rd;
    if (((IncomingNotificationRequest)getInputMessage()).isAliveMessage())
    {
      log.fine("Received device ALIVE advertisement, descriptor location is: " + rdIdentity.getDescriptorURL());
      if (rdIdentity.getDescriptorURL() == null)
      {
        log.finer("Ignoring message without location URL header: " + getInputMessage());
        return;
      }
      if (rdIdentity.getMaxAgeSeconds() == null)
      {
        log.finer("Ignoring message without max-age header: " + getInputMessage());
        return;
      }
      if (getUpnpService().getRegistry().update(rdIdentity))
      {
        log.finer("Remote device was already known: " + udn);
        return;
      }
      getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(new RetrieveRemoteDescriptors(
        getUpnpService(), rd));
    }
    else if (((IncomingNotificationRequest)getInputMessage()).isByeByeMessage())
    {
      log.fine("Received device BYEBYE advertisement");
      boolean removed = getUpnpService().getRegistry().removeDevice(rd);
      if (removed) {
        log.fine("Removed remote device from registry: " + rd);
      }
    }
    else
    {
      log.finer("Ignoring unknown notification message: " + getInputMessage());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\async\ReceivingNotification.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */