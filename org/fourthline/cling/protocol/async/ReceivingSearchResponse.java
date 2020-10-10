package org.fourthline.cling.protocol.async;

import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.discovery.IncomingSearchResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.ReceivingAsync;
import org.fourthline.cling.protocol.RetrieveRemoteDescriptors;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.RouterException;

public class ReceivingSearchResponse
  extends ReceivingAsync<IncomingSearchResponse>
{
  private static final Logger log = Logger.getLogger(ReceivingSearchResponse.class.getName());
  
  public ReceivingSearchResponse(UpnpService upnpService, IncomingDatagramMessage<UpnpResponse> inputMessage)
  {
    super(upnpService, new IncomingSearchResponse(inputMessage));
  }
  
  protected void execute()
    throws RouterException
  {
    if (!((IncomingSearchResponse)getInputMessage()).isSearchResponseMessage())
    {
      log.fine("Ignoring invalid search response message: " + getInputMessage());
      return;
    }
    UDN udn = ((IncomingSearchResponse)getInputMessage()).getRootDeviceUDN();
    if (udn == null)
    {
      log.fine("Ignoring search response message without UDN: " + getInputMessage());
      return;
    }
    RemoteDeviceIdentity rdIdentity = new RemoteDeviceIdentity((IncomingSearchResponse)getInputMessage());
    log.fine("Received device search response: " + rdIdentity);
    if (getUpnpService().getRegistry().update(rdIdentity))
    {
      log.fine("Remote device was already known: " + udn);
      return;
    }
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
    getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(new RetrieveRemoteDescriptors(
      getUpnpService(), rd));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\async\ReceivingSearchResponse.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */