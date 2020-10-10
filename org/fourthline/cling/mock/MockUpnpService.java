package org.fourthline.cling.mock;

import javax.enterprise.inject.Alternative;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.controlpoint.ControlPointImpl;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ProtocolFactoryImpl;
import org.fourthline.cling.protocol.async.SendingNotificationAlive;
import org.fourthline.cling.protocol.async.SendingSearch;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryImpl;
import org.fourthline.cling.registry.RegistryMaintainer;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;

@Alternative
public class MockUpnpService
  implements UpnpService
{
  protected final UpnpServiceConfiguration configuration;
  protected final ControlPoint controlPoint;
  protected final ProtocolFactory protocolFactory;
  protected final Registry registry;
  protected final MockRouter router;
  protected final NetworkAddressFactory networkAddressFactory;
  
  public MockUpnpService()
  {
    this(false, new MockUpnpServiceConfiguration(false, false));
  }
  
  public MockUpnpService(MockUpnpServiceConfiguration configuration)
  {
    this(false, configuration);
  }
  
  public MockUpnpService(boolean sendsAlive, boolean maintainsRegistry)
  {
    this(sendsAlive, new MockUpnpServiceConfiguration(maintainsRegistry, false));
  }
  
  public MockUpnpService(boolean sendsAlive, boolean maintainsRegistry, boolean multiThreaded)
  {
    this(sendsAlive, new MockUpnpServiceConfiguration(maintainsRegistry, multiThreaded));
  }
  
  public MockUpnpService(boolean sendsAlive, final MockUpnpServiceConfiguration configuration)
  {
    this.configuration = configuration;
    
    this.protocolFactory = createProtocolFactory(this, sendsAlive);
    
    this.registry = new RegistryImpl(this)
    {
      protected RegistryMaintainer createRegistryMaintainer()
      {
        return configuration.isMaintainsRegistry() ? super.createRegistryMaintainer() : null;
      }
    };
    this.networkAddressFactory = this.configuration.createNetworkAddressFactory();
    
    this.router = createRouter();
    
    this.controlPoint = new ControlPointImpl(configuration, this.protocolFactory, this.registry);
  }
  
  protected ProtocolFactory createProtocolFactory(UpnpService service, boolean sendsAlive)
  {
    return new MockProtocolFactory(service, sendsAlive);
  }
  
  protected MockRouter createRouter()
  {
    return new MockRouter(getConfiguration(), getProtocolFactory());
  }
  
  public static class MockProtocolFactory
    extends ProtocolFactoryImpl
  {
    private boolean sendsAlive;
    
    public MockProtocolFactory(UpnpService upnpService, boolean sendsAlive)
    {
      super();
      this.sendsAlive = sendsAlive;
    }
    
    public SendingNotificationAlive createSendingNotificationAlive(LocalDevice localDevice)
    {
      new SendingNotificationAlive(getUpnpService(), localDevice)
      {
        protected void execute()
          throws RouterException
        {
          if (MockUpnpService.MockProtocolFactory.this.sendsAlive) {
            super.execute();
          }
        }
      };
    }
    
    public SendingSearch createSendingSearch(UpnpHeader searchTarget, int mxSeconds)
    {
      new SendingSearch(getUpnpService(), searchTarget, mxSeconds)
      {
        public int getBulkIntervalMilliseconds()
        {
          return 0;
        }
      };
    }
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
  
  public MockRouter getRouter()
  {
    return this.router;
  }
  
  public void shutdown()
  {
    getRegistry().shutdown();
    getConfiguration().shutdown();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\mock\MockUpnpService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */