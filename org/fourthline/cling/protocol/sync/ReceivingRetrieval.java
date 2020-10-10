package org.fourthline.cling.protocol.sync;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.UpnpResponse.Status;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.ServerHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.resource.DeviceDescriptorResource;
import org.fourthline.cling.model.resource.IconResource;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.resource.ServiceDescriptorResource;
import org.fourthline.cling.protocol.ReceivingSync;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;

public class ReceivingRetrieval
  extends ReceivingSync<StreamRequestMessage, StreamResponseMessage>
{
  private static final Logger log = Logger.getLogger(ReceivingRetrieval.class.getName());
  
  public ReceivingRetrieval(UpnpService upnpService, StreamRequestMessage inputMessage)
  {
    super(upnpService, inputMessage);
  }
  
  protected StreamResponseMessage executeSync()
    throws RouterException
  {
    if (!((StreamRequestMessage)getInputMessage()).hasHostHeader())
    {
      log.fine("Ignoring message, missing HOST header: " + getInputMessage());
      return new StreamResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
    }
    URI requestedURI = ((UpnpRequest)((StreamRequestMessage)getInputMessage()).getOperation()).getURI();
    
    Resource foundResource = getUpnpService().getRegistry().getResource(requestedURI);
    if (foundResource == null)
    {
      foundResource = onResourceNotFound(requestedURI);
      if (foundResource == null)
      {
        log.fine("No local resource found: " + getInputMessage());
        return null;
      }
    }
    return createResponse(requestedURI, foundResource);
  }
  
  protected StreamResponseMessage createResponse(URI requestedURI, Resource resource)
  {
    StreamResponseMessage response;
    try
    {
      StreamResponseMessage response;
      if (DeviceDescriptorResource.class.isAssignableFrom(resource.getClass()))
      {
        log.fine("Found local device matching relative request URI: " + requestedURI);
        LocalDevice device = (LocalDevice)resource.getModel();
        
        DeviceDescriptorBinder deviceDescriptorBinder = getUpnpService().getConfiguration().getDeviceDescriptorBinderUDA10();
        String deviceDescriptor = deviceDescriptorBinder.generate(device, 
        
          getRemoteClientInfo(), 
          getUpnpService().getConfiguration().getNamespace());
        
        response = new StreamResponseMessage(deviceDescriptor, new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE));
      }
      else
      {
        StreamResponseMessage response;
        if (ServiceDescriptorResource.class.isAssignableFrom(resource.getClass()))
        {
          log.fine("Found local service matching relative request URI: " + requestedURI);
          LocalService service = (LocalService)resource.getModel();
          
          ServiceDescriptorBinder serviceDescriptorBinder = getUpnpService().getConfiguration().getServiceDescriptorBinderUDA10();
          String serviceDescriptor = serviceDescriptorBinder.generate(service);
          response = new StreamResponseMessage(serviceDescriptor, new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE));
        }
        else
        {
          StreamResponseMessage response;
          if (IconResource.class.isAssignableFrom(resource.getClass()))
          {
            log.fine("Found local icon matching relative request URI: " + requestedURI);
            Icon icon = (Icon)resource.getModel();
            response = new StreamResponseMessage(icon.getData(), icon.getMimeType());
          }
          else
          {
            log.fine("Ignoring GET for found local resource: " + resource);
            return null;
          }
        }
      }
    }
    catch (DescriptorBindingException ex)
    {
      StreamResponseMessage response;
      log.warning("Error generating requested device/service descriptor: " + ex.toString());
      log.log(Level.WARNING, "Exception root cause: ", Exceptions.unwrap(ex));
      response = new StreamResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
    }
    response.getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());
    
    return response;
  }
  
  protected Resource onResourceNotFound(URI requestedURIPath)
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\protocol\sync\ReceivingRetrieval.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */