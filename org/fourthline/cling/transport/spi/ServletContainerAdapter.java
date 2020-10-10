package org.fourthline.cling.transport.spi;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import javax.servlet.Servlet;

public abstract interface ServletContainerAdapter
{
  public abstract void setExecutorService(ExecutorService paramExecutorService);
  
  public abstract int addConnector(String paramString, int paramInt)
    throws IOException;
  
  public abstract void removeConnector(String paramString, int paramInt);
  
  public abstract void registerServlet(String paramString, Servlet paramServlet);
  
  public abstract void startIfNotRunning();
  
  public abstract void stopIfRunning();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\ServletContainerAdapter.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */