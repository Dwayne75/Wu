package org.fourthline.cling.mock;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;

@Alternative
public class MockUpnpServiceConfiguration
  extends DefaultUpnpServiceConfiguration
{
  protected final boolean maintainsRegistry;
  protected final boolean multiThreaded;
  
  public MockUpnpServiceConfiguration()
  {
    this(false, false);
  }
  
  public MockUpnpServiceConfiguration(boolean maintainsRegistry)
  {
    this(maintainsRegistry, false);
  }
  
  public MockUpnpServiceConfiguration(boolean maintainsRegistry, boolean multiThreaded)
  {
    super(false);
    this.maintainsRegistry = maintainsRegistry;
    this.multiThreaded = multiThreaded;
  }
  
  public boolean isMaintainsRegistry()
  {
    return this.maintainsRegistry;
  }
  
  public boolean isMultiThreaded()
  {
    return this.multiThreaded;
  }
  
  protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort)
  {
    new NetworkAddressFactoryImpl(streamListenPort)
    {
      protected boolean isUsableNetworkInterface(NetworkInterface iface)
        throws Exception
      {
        return iface.isLoopback();
      }
      
      protected boolean isUsableAddress(NetworkInterface networkInterface, InetAddress address)
      {
        return (address.isLoopbackAddress()) && ((address instanceof Inet4Address));
      }
    };
  }
  
  public Executor getRegistryMaintainerExecutor()
  {
    if (isMaintainsRegistry()) {
      new Executor()
      {
        public void execute(Runnable runnable)
        {
          new Thread(runnable).start();
        }
      };
    }
    return getDefaultExecutorService();
  }
  
  protected ExecutorService getDefaultExecutorService()
  {
    if (isMultiThreaded()) {
      return super.getDefaultExecutorService();
    }
    new AbstractExecutorService()
    {
      boolean terminated;
      
      public void shutdown()
      {
        this.terminated = true;
      }
      
      public List<Runnable> shutdownNow()
      {
        shutdown();
        return null;
      }
      
      public boolean isShutdown()
      {
        return this.terminated;
      }
      
      public boolean isTerminated()
      {
        return this.terminated;
      }
      
      public boolean awaitTermination(long l, TimeUnit timeUnit)
        throws InterruptedException
      {
        shutdown();
        return this.terminated;
      }
      
      public void execute(Runnable runnable)
      {
        runnable.run();
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\mock\MockUpnpServiceConfiguration.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */