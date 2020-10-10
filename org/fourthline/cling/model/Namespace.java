package org.fourthline.cling.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDN;
import org.seamless.util.URIUtil;

public class Namespace
{
  private static final Logger log = Logger.getLogger(Namespace.class.getName());
  public static final String DEVICE = "/dev";
  public static final String SERVICE = "/svc";
  public static final String CONTROL = "/action";
  public static final String EVENTS = "/event";
  public static final String DESCRIPTOR_FILE = "/desc";
  public static final String CALLBACK_FILE = "/cb";
  protected final URI basePath;
  protected final String decodedPath;
  
  public Namespace()
  {
    this("");
  }
  
  public Namespace(String basePath)
  {
    this(URI.create(basePath));
  }
  
  public Namespace(URI basePath)
  {
    this.basePath = basePath;
    this.decodedPath = basePath.getPath();
  }
  
  public URI getBasePath()
  {
    return this.basePath;
  }
  
  public URI getPath(Device device)
  {
    return appendPathToBaseURI(getDevicePath(device));
  }
  
  public URI getPath(Service service)
  {
    return appendPathToBaseURI(getServicePath(service));
  }
  
  public URI getDescriptorPath(Device device)
  {
    return appendPathToBaseURI(getDevicePath(device.getRoot()) + "/desc");
  }
  
  public String getDescriptorPathString(Device device)
  {
    return this.decodedPath + getDevicePath(device.getRoot()) + "/desc";
  }
  
  public URI getDescriptorPath(Service service)
  {
    return appendPathToBaseURI(getServicePath(service) + "/desc");
  }
  
  public URI getControlPath(Service service)
  {
    return appendPathToBaseURI(getServicePath(service) + "/action");
  }
  
  public URI getIconPath(Icon icon)
  {
    return appendPathToBaseURI(getDevicePath(icon.getDevice()) + "/" + icon.getUri().toString());
  }
  
  public URI getEventSubscriptionPath(Service service)
  {
    return appendPathToBaseURI(getServicePath(service) + "/event");
  }
  
  public URI getEventCallbackPath(Service service)
  {
    return appendPathToBaseURI(getServicePath(service) + "/event" + "/cb");
  }
  
  public String getEventCallbackPathString(Service service)
  {
    return this.decodedPath + getServicePath(service) + "/event" + "/cb";
  }
  
  public URI prefixIfRelative(Device device, URI uri)
  {
    if ((!uri.isAbsolute()) && (!uri.getPath().startsWith("/"))) {
      return appendPathToBaseURI(getDevicePath(device) + "/" + uri);
    }
    return uri;
  }
  
  public boolean isControlPath(URI uri)
  {
    return uri.toString().endsWith("/action");
  }
  
  public boolean isEventSubscriptionPath(URI uri)
  {
    return uri.toString().endsWith("/event");
  }
  
  public boolean isEventCallbackPath(URI uri)
  {
    return uri.toString().endsWith("/cb");
  }
  
  public Resource[] getResources(Device device)
    throws ValidationException
  {
    if (!device.isRoot()) {
      return null;
    }
    Set<Resource> resources = new HashSet();
    List<ValidationError> errors = new ArrayList();
    
    log.fine("Discovering local resources of device graph");
    Resource[] discoveredResources = device.discoverResources(this);
    for (Resource resource : discoveredResources)
    {
      log.finer("Discovered: " + resource);
      if (!resources.add(resource))
      {
        log.finer("Local resource already exists, queueing validation error");
        errors.add(new ValidationError(
          getClass(), "resources", "Local URI namespace conflict between resources of device: " + resource));
      }
    }
    if (errors.size() > 0) {
      throw new ValidationException("Validation of device graph failed, call getErrors() on exception", errors);
    }
    return (Resource[])resources.toArray(new Resource[resources.size()]);
  }
  
  protected URI appendPathToBaseURI(String path)
  {
    try
    {
      return new URI(this.basePath.getScheme(), null, this.basePath.getHost(), this.basePath.getPort(), this.decodedPath + path, null, null);
    }
    catch (URISyntaxException e) {}
    return URI.create(this.basePath + path);
  }
  
  protected String getDevicePath(Device device)
  {
    if (device.getIdentity().getUdn() == null) {
      throw new IllegalStateException("Can't generate local URI prefix without UDN");
    }
    StringBuilder s = new StringBuilder();
    s.append("/dev").append("/");
    
    s.append(URIUtil.encodePathSegment(device.getIdentity().getUdn().getIdentifierString()));
    return s.toString();
  }
  
  protected String getServicePath(Service service)
  {
    if (service.getServiceId() == null) {
      throw new IllegalStateException("Can't generate local URI prefix without service ID");
    }
    StringBuilder s = new StringBuilder();
    s.append("/svc");
    s.append("/");
    s.append(service.getServiceId().getNamespace());
    s.append("/");
    s.append(service.getServiceId().getId());
    return getDevicePath(service.getDevice()) + s.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\Namespace.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */