package org.fourthline.cling.transport.spi;

import java.util.concurrent.ExecutorService;

public abstract interface StreamClientConfiguration
{
  public abstract ExecutorService getRequestExecutorService();
  
  public abstract int getTimeoutSeconds();
  
  public abstract int getLogWarningSeconds();
  
  public abstract String getUserAgentValue(int paramInt1, int paramInt2);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\StreamClientConfiguration.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */