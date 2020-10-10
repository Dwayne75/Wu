package org.fourthline.cling;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderImpl;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.transport.impl.DatagramIOConfigurationImpl;
import org.fourthline.cling.transport.impl.DatagramIOImpl;
import org.fourthline.cling.transport.impl.GENAEventProcessorImpl;
import org.fourthline.cling.transport.impl.MulticastReceiverConfigurationImpl;
import org.fourthline.cling.transport.impl.MulticastReceiverImpl;
import org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl;
import org.fourthline.cling.transport.impl.SOAPActionProcessorImpl;
import org.fourthline.cling.transport.impl.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.impl.StreamClientImpl;
import org.fourthline.cling.transport.impl.StreamServerConfigurationImpl;
import org.fourthline.cling.transport.impl.StreamServerImpl;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.fourthline.cling.transport.spi.MulticastReceiver;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;

@ApplicationScoped
public class ManagedUpnpServiceConfiguration
  implements UpnpServiceConfiguration
{
  private static Logger log = Logger.getLogger(DefaultUpnpServiceConfiguration.class.getName());
  private int streamListenPort;
  private ExecutorService defaultExecutorService;
  @Inject
  protected DatagramProcessor datagramProcessor;
  private SOAPActionProcessor soapActionProcessor;
  private GENAEventProcessor genaEventProcessor;
  private DeviceDescriptorBinder deviceDescriptorBinderUDA10;
  private ServiceDescriptorBinder serviceDescriptorBinderUDA10;
  private Namespace namespace;
  
  @PostConstruct
  public void init()
  {
    if (ModelUtil.ANDROID_RUNTIME) {
      throw new Error("Unsupported runtime environment, use org.fourthline.cling.android.AndroidUpnpServiceConfiguration");
    }
    this.streamListenPort = 0;
    
    this.defaultExecutorService = createDefaultExecutorService();
    
    this.soapActionProcessor = createSOAPActionProcessor();
    this.genaEventProcessor = createGENAEventProcessor();
    
    this.deviceDescriptorBinderUDA10 = createDeviceDescriptorBinderUDA10();
    this.serviceDescriptorBinderUDA10 = createServiceDescriptorBinderUDA10();
    
    this.namespace = createNamespace();
  }
  
  public DatagramProcessor getDatagramProcessor()
  {
    return this.datagramProcessor;
  }
  
  public SOAPActionProcessor getSoapActionProcessor()
  {
    return this.soapActionProcessor;
  }
  
  public GENAEventProcessor getGenaEventProcessor()
  {
    return this.genaEventProcessor;
  }
  
  public StreamClient createStreamClient()
  {
    return new StreamClientImpl(new StreamClientConfigurationImpl(getSyncProtocolExecutorService()));
  }
  
  public MulticastReceiver createMulticastReceiver(NetworkAddressFactory networkAddressFactory)
  {
    return new MulticastReceiverImpl(new MulticastReceiverConfigurationImpl(networkAddressFactory.getMulticastGroup(), networkAddressFactory.getMulticastPort()));
  }
  
  public DatagramIO createDatagramIO(NetworkAddressFactory networkAddressFactory)
  {
    return new DatagramIOImpl(new DatagramIOConfigurationImpl());
  }
  
  public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory)
  {
    return new StreamServerImpl(new StreamServerConfigurationImpl(networkAddressFactory.getStreamListenPort()));
  }
  
  public Executor getMulticastReceiverExecutor()
  {
    return getDefaultExecutorService();
  }
  
  public Executor getDatagramIOExecutor()
  {
    return getDefaultExecutorService();
  }
  
  public ExecutorService getStreamServerExecutorService()
  {
    return getDefaultExecutorService();
  }
  
  public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10()
  {
    return this.deviceDescriptorBinderUDA10;
  }
  
  public ServiceDescriptorBinder getServiceDescriptorBinderUDA10()
  {
    return this.serviceDescriptorBinderUDA10;
  }
  
  public ServiceType[] getExclusiveServiceTypes()
  {
    return new ServiceType[0];
  }
  
  public boolean isReceivedSubscriptionTimeoutIgnored()
  {
    return false;
  }
  
  public UpnpHeaders getDescriptorRetrievalHeaders(RemoteDeviceIdentity identity)
  {
    return null;
  }
  
  public UpnpHeaders getEventSubscriptionHeaders(RemoteService service)
  {
    return null;
  }
  
  public int getRegistryMaintenanceIntervalMillis()
  {
    return 1000;
  }
  
  public int getAliveIntervalMillis()
  {
    return 0;
  }
  
  public Integer getRemoteDeviceMaxAgeSeconds()
  {
    return null;
  }
  
  public Executor getAsyncProtocolExecutor()
  {
    return getDefaultExecutorService();
  }
  
  public ExecutorService getSyncProtocolExecutorService()
  {
    return getDefaultExecutorService();
  }
  
  public Namespace getNamespace()
  {
    return this.namespace;
  }
  
  public Executor getRegistryMaintainerExecutor()
  {
    return getDefaultExecutorService();
  }
  
  public Executor getRegistryListenerExecutor()
  {
    return getDefaultExecutorService();
  }
  
  public NetworkAddressFactory createNetworkAddressFactory()
  {
    return createNetworkAddressFactory(this.streamListenPort);
  }
  
  public void shutdown()
  {
    log.fine("Shutting down default executor service");
    getDefaultExecutorService().shutdownNow();
  }
  
  protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort)
  {
    return new NetworkAddressFactoryImpl(streamListenPort);
  }
  
  protected SOAPActionProcessor createSOAPActionProcessor()
  {
    return new SOAPActionProcessorImpl();
  }
  
  protected GENAEventProcessor createGENAEventProcessor()
  {
    return new GENAEventProcessorImpl();
  }
  
  protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10()
  {
    return new UDA10DeviceDescriptorBinderImpl();
  }
  
  protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10()
  {
    return new UDA10ServiceDescriptorBinderImpl();
  }
  
  protected Namespace createNamespace()
  {
    return new Namespace();
  }
  
  protected ExecutorService getDefaultExecutorService()
  {
    return this.defaultExecutorService;
  }
  
  protected ExecutorService createDefaultExecutorService()
  {
    return new DefaultUpnpServiceConfiguration.ClingExecutor();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\ManagedUpnpServiceConfiguration.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */