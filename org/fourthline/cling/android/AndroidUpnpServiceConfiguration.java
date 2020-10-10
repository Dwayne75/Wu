package org.fourthline.cling.android;

import android.os.Build.VERSION;
import java.util.concurrent.ExecutorService;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.ServerClientTokens;
import org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl;
import org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl;
import org.fourthline.cling.transport.impl.RecoveringGENAEventProcessorImpl;
import org.fourthline.cling.transport.impl.RecoveringSOAPActionProcessorImpl;
import org.fourthline.cling.transport.impl.jetty.JettyServletContainer;
import org.fourthline.cling.transport.impl.jetty.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.impl.jetty.StreamClientImpl;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;

public class AndroidUpnpServiceConfiguration
  extends DefaultUpnpServiceConfiguration
{
  public AndroidUpnpServiceConfiguration()
  {
    this(0);
  }
  
  public AndroidUpnpServiceConfiguration(int streamListenPort)
  {
    super(streamListenPort, false);
    
    System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
  }
  
  protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort)
  {
    return new AndroidNetworkAddressFactory(streamListenPort);
  }
  
  protected Namespace createNamespace()
  {
    return new Namespace("/upnp");
  }
  
  public StreamClient createStreamClient()
  {
    new StreamClientImpl(new StreamClientConfigurationImpl(getSyncProtocolExecutorService())
    {
      public String getUserAgentValue(int majorVersion, int minorVersion)
      {
        ServerClientTokens tokens = new ServerClientTokens(majorVersion, minorVersion);
        tokens.setOsName("Android");
        tokens.setOsVersion(Build.VERSION.RELEASE);
        return tokens.toString();
      }
    });
  }
  
  public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory)
  {
    return new AsyncServletStreamServerImpl(new AsyncServletStreamServerConfigurationImpl(JettyServletContainer.INSTANCE, networkAddressFactory.getStreamListenPort()));
  }
  
  protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10()
  {
    return new RecoveringUDA10DeviceDescriptorBinderImpl();
  }
  
  protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10()
  {
    return new UDA10ServiceDescriptorBinderSAXImpl();
  }
  
  protected SOAPActionProcessor createSOAPActionProcessor()
  {
    return new RecoveringSOAPActionProcessorImpl();
  }
  
  protected GENAEventProcessor createGENAEventProcessor()
  {
    return new RecoveringGENAEventProcessorImpl();
  }
  
  public int getRegistryMaintenanceIntervalMillis()
  {
    return 3000;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\android\AndroidUpnpServiceConfiguration.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */