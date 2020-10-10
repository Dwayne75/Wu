package org.fourthline.cling.transport.impl.jetty;

import java.util.concurrent.ExecutorService;
import org.fourthline.cling.transport.spi.AbstractStreamClientConfiguration;

public class StreamClientConfigurationImpl
  extends AbstractStreamClientConfiguration
{
  public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService)
  {
    super(timeoutExecutorService);
  }
  
  public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService, int timeoutSeconds)
  {
    super(timeoutExecutorService, timeoutSeconds);
  }
  
  public int getRequestRetryCount()
  {
    return 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\jetty\StreamClientConfigurationImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */