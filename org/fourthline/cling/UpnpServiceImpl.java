package org.fourthline.cling;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.controlpoint.ControlPointImpl;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ProtocolFactoryImpl;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryImpl;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.RouterImpl;
import org.seamless.util.Exceptions;

@Alternative
public class UpnpServiceImpl
  implements UpnpService
{
  private static Logger log = Logger.getLogger(UpnpServiceImpl.class.getName());
  protected final UpnpServiceConfiguration configuration;
  protected final ControlPoint controlPoint;
  protected final ProtocolFactory protocolFactory;
  protected final Registry registry;
  protected final Router router;
  
  public UpnpServiceImpl()
  {
    this(new DefaultUpnpServiceConfiguration(), new RegistryListener[0]);
  }
  
  public UpnpServiceImpl(RegistryListener... registryListeners)
  {
    this(new DefaultUpnpServiceConfiguration(), registryListeners);
  }
  
  public UpnpServiceImpl(UpnpServiceConfiguration configuration, RegistryListener... registryListeners)
  {
    this.configuration = configuration;
    
    log.info(">>> Starting UPnP service...");
    
    log.info("Using configuration: " + getConfiguration().getClass().getName());
    
    this.protocolFactory = createProtocolFactory();
    
    this.registry = createRegistry(this.protocolFactory);
    for (RegistryListener registryListener : registryListeners) {
      this.registry.addListener(registryListener);
    }
    this.router = createRouter(this.protocolFactory, this.registry);
    try
    {
      this.router.enable();
    }
    catch (RouterException ex)
    {
      throw new RuntimeException("Enabling network router failed: " + ex, ex);
    }
    this.controlPoint = createControlPoint(this.protocolFactory, this.registry);
    
    log.info("<<< UPnP service started successfully");
  }
  
  protected ProtocolFactory createProtocolFactory()
  {
    return new ProtocolFactoryImpl(this);
  }
  
  protected Registry createRegistry(ProtocolFactory protocolFactory)
  {
    return new RegistryImpl(this);
  }
  
  protected Router createRouter(ProtocolFactory protocolFactory, Registry registry)
  {
    return new RouterImpl(getConfiguration(), protocolFactory);
  }
  
  protected ControlPoint createControlPoint(ProtocolFactory protocolFactory, Registry registry)
  {
    return new ControlPointImpl(getConfiguration(), protocolFactory, registry);
  }
  
  public UpnpServiceConfiguration getConfiguration()
  {
    return this.configuration;
  }
  
  public ControlPoint getControlPoint()
  {
    return this.controlPoint;
  }
  
  public ProtocolFactory getProtocolFactory()
  {
    return this.protocolFactory;
  }
  
  public Registry getRegistry()
  {
    return this.registry;
  }
  
  public Router getRouter()
  {
    return this.router;
  }
  
  public synchronized void shutdown()
  {
    shutdown(false);
  }
  
  protected void shutdown(boolean separateThread)
  {
    Runnable shutdown = new Runnable()
    {
      public void run()
      {
        UpnpServiceImpl.log.info(">>> Shutting down UPnP service...");
        UpnpServiceImpl.this.shutdownRegistry();
        UpnpServiceImpl.this.shutdownRouter();
        UpnpServiceImpl.this.shutdownConfiguration();
        UpnpServiceImpl.log.info("<<< UPnP service shutdown completed");
      }
    };
    if (separateThread) {
      new Thread(shutdown).start();
    } else {
      shutdown.run();
    }
  }
  
  protected void shutdownRegistry()
  {
    getRegistry().shutdown();
  }
  
  protected void shutdownRouter()
  {
    try
    {
      getRouter().shutdown();
    }
    catch (RouterException ex)
    {
      Throwable cause = Exceptions.unwrap(ex);
      if ((cause instanceof InterruptedException)) {
        log.log(Level.INFO, "Router shutdown was interrupted: " + ex, cause);
      } else {
        log.log(Level.SEVERE, "Router error on shutdown: " + ex, cause);
      }
    }
  }
  
  protected void shutdownConfiguration()
  {
    getConfiguration().shutdown();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\UpnpServiceImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */