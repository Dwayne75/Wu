package org.fourthline.cling;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.enterprise.inject.Alternative;
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
import org.fourthline.cling.transport.impl.DatagramProcessorImpl;
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
import org.seamless.util.Exceptions;

@Alternative
public class DefaultUpnpServiceConfiguration
  implements UpnpServiceConfiguration
{
  private static Logger log = Logger.getLogger(DefaultUpnpServiceConfiguration.class.getName());
  private final int streamListenPort;
  private final ExecutorService defaultExecutorService;
  private final DatagramProcessor datagramProcessor;
  private final SOAPActionProcessor soapActionProcessor;
  private final GENAEventProcessor genaEventProcessor;
  private final DeviceDescriptorBinder deviceDescriptorBinderUDA10;
  private final ServiceDescriptorBinder serviceDescriptorBinderUDA10;
  private final Namespace namespace;
  
  public DefaultUpnpServiceConfiguration()
  {
    this(0);
  }
  
  public DefaultUpnpServiceConfiguration(int streamListenPort)
  {
    this(streamListenPort, true);
  }
  
  protected DefaultUpnpServiceConfiguration(boolean checkRuntime)
  {
    this(0, checkRuntime);
  }
  
  protected DefaultUpnpServiceConfiguration(int streamListenPort, boolean checkRuntime)
  {
    if ((checkRuntime) && (ModelUtil.ANDROID_RUNTIME)) {
      throw new Error("Unsupported runtime environment, use org.fourthline.cling.android.AndroidUpnpServiceConfiguration");
    }
    this.streamListenPort = streamListenPort;
    
    this.defaultExecutorService = createDefaultExecutorService();
    
    this.datagramProcessor = createDatagramProcessor();
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
  
  protected DatagramProcessor createDatagramProcessor()
  {
    return new DatagramProcessorImpl();
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
    return new ClingExecutor();
  }
  
  public static class ClingExecutor
    extends ThreadPoolExecutor
  {
    public ClingExecutor()
    {
      this(new DefaultUpnpServiceConfiguration.ClingThreadFactory(), new ThreadPoolExecutor.DiscardPolicy()
      {
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor)
        {
          DefaultUpnpServiceConfiguration.log.info("Thread pool rejected execution of " + runnable.getClass());
          super.rejectedExecution(runnable, threadPoolExecutor);
        }
      });
    }
    
    public ClingExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedHandler)
    {
      super(Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue(), threadFactory, rejectedHandler);
    }
    
    protected void afterExecute(Runnable runnable, Throwable throwable)
    {
      super.afterExecute(runnable, throwable);
      if (throwable != null)
      {
        Throwable cause = Exceptions.unwrap(throwable);
        if ((cause instanceof InterruptedException)) {
          return;
        }
        DefaultUpnpServiceConfiguration.log.warning("Thread terminated " + runnable + " abruptly with exception: " + throwable);
        DefaultUpnpServiceConfiguration.log.warning("Root cause: " + cause);
      }
    }
  }
  
  public static class ClingThreadFactory
    implements ThreadFactory
  {
    protected final ThreadGroup group;
    protected final AtomicInteger threadNumber = new AtomicInteger(1);
    protected final String namePrefix = "cling-";
    
    public ClingThreadFactory()
    {
      SecurityManager s = System.getSecurityManager();
      this.group = (s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup());
    }
    
    public Thread newThread(Runnable r)
    {
      Thread t = new Thread(this.group, r, "cling-" + this.threadNumber.getAndIncrement(), 0L);
      if (t.isDaemon()) {
        t.setDaemon(false);
      }
      if (t.getPriority() != 5) {
        t.setPriority(5);
      }
      return t;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\DefaultUpnpServiceConfiguration.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */