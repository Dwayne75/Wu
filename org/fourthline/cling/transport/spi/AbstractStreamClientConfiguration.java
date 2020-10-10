package org.fourthline.cling.transport.spi;

import java.util.concurrent.ExecutorService;
import org.fourthline.cling.model.ServerClientTokens;

public abstract class AbstractStreamClientConfiguration
  implements StreamClientConfiguration
{
  protected ExecutorService requestExecutorService;
  protected int timeoutSeconds = 60;
  protected int logWarningSeconds = 5;
  
  protected AbstractStreamClientConfiguration(ExecutorService requestExecutorService)
  {
    this.requestExecutorService = requestExecutorService;
  }
  
  protected AbstractStreamClientConfiguration(ExecutorService requestExecutorService, int timeoutSeconds)
  {
    this.requestExecutorService = requestExecutorService;
    this.timeoutSeconds = timeoutSeconds;
  }
  
  protected AbstractStreamClientConfiguration(ExecutorService requestExecutorService, int timeoutSeconds, int logWarningSeconds)
  {
    this.requestExecutorService = requestExecutorService;
    this.timeoutSeconds = timeoutSeconds;
    this.logWarningSeconds = logWarningSeconds;
  }
  
  public ExecutorService getRequestExecutorService()
  {
    return this.requestExecutorService;
  }
  
  public void setRequestExecutorService(ExecutorService requestExecutorService)
  {
    this.requestExecutorService = requestExecutorService;
  }
  
  public int getTimeoutSeconds()
  {
    return this.timeoutSeconds;
  }
  
  public void setTimeoutSeconds(int timeoutSeconds)
  {
    this.timeoutSeconds = timeoutSeconds;
  }
  
  public int getLogWarningSeconds()
  {
    return this.logWarningSeconds;
  }
  
  public void setLogWarningSeconds(int logWarningSeconds)
  {
    this.logWarningSeconds = logWarningSeconds;
  }
  
  public String getUserAgentValue(int majorVersion, int minorVersion)
  {
    return new ServerClientTokens(majorVersion, minorVersion).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\AbstractStreamClientConfiguration.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */