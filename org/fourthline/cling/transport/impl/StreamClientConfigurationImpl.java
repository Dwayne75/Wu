package org.fourthline.cling.transport.impl;

import java.util.concurrent.ExecutorService;
import org.fourthline.cling.transport.spi.AbstractStreamClientConfiguration;

public class StreamClientConfigurationImpl
  extends AbstractStreamClientConfiguration
{
  private boolean usePersistentConnections = false;
  
  public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService)
  {
    super(timeoutExecutorService);
  }
  
  public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService, int timeoutSeconds)
  {
    super(timeoutExecutorService, timeoutSeconds);
  }
  
  public boolean isUsePersistentConnections()
  {
    return this.usePersistentConnections;
  }
  
  public void setUsePersistentConnections(boolean usePersistentConnections)
  {
    this.usePersistentConnections = usePersistentConnections;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\StreamClientConfigurationImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */