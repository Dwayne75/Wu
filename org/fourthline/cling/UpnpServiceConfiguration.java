package org.fourthline.cling;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.fourthline.cling.transport.spi.MulticastReceiver;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;

public abstract interface UpnpServiceConfiguration
{
  public abstract NetworkAddressFactory createNetworkAddressFactory();
  
  public abstract DatagramProcessor getDatagramProcessor();
  
  public abstract SOAPActionProcessor getSoapActionProcessor();
  
  public abstract GENAEventProcessor getGenaEventProcessor();
  
  public abstract StreamClient createStreamClient();
  
  public abstract MulticastReceiver createMulticastReceiver(NetworkAddressFactory paramNetworkAddressFactory);
  
  public abstract DatagramIO createDatagramIO(NetworkAddressFactory paramNetworkAddressFactory);
  
  public abstract StreamServer createStreamServer(NetworkAddressFactory paramNetworkAddressFactory);
  
  public abstract Executor getMulticastReceiverExecutor();
  
  public abstract Executor getDatagramIOExecutor();
  
  public abstract ExecutorService getStreamServerExecutorService();
  
  public abstract DeviceDescriptorBinder getDeviceDescriptorBinderUDA10();
  
  public abstract ServiceDescriptorBinder getServiceDescriptorBinderUDA10();
  
  public abstract ServiceType[] getExclusiveServiceTypes();
  
  public abstract int getRegistryMaintenanceIntervalMillis();
  
  public abstract int getAliveIntervalMillis();
  
  public abstract boolean isReceivedSubscriptionTimeoutIgnored();
  
  public abstract Integer getRemoteDeviceMaxAgeSeconds();
  
  public abstract UpnpHeaders getDescriptorRetrievalHeaders(RemoteDeviceIdentity paramRemoteDeviceIdentity);
  
  public abstract UpnpHeaders getEventSubscriptionHeaders(RemoteService paramRemoteService);
  
  public abstract Executor getAsyncProtocolExecutor();
  
  public abstract ExecutorService getSyncProtocolExecutorService();
  
  public abstract Namespace getNamespace();
  
  public abstract Executor getRegistryMaintainerExecutor();
  
  public abstract Executor getRegistryListenerExecutor();
  
  public abstract void shutdown();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\UpnpServiceConfiguration.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */