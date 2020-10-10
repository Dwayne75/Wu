package org.fourthline.cling.transport.impl;

import org.fourthline.cling.transport.spi.ServletContainerAdapter;
import org.fourthline.cling.transport.spi.StreamServerConfiguration;

public class AsyncServletStreamServerConfigurationImpl
  implements StreamServerConfiguration
{
  protected ServletContainerAdapter servletContainerAdapter;
  protected int listenPort = 0;
  protected int asyncTimeoutSeconds = 60;
  
  public AsyncServletStreamServerConfigurationImpl(ServletContainerAdapter servletContainerAdapter)
  {
    this.servletContainerAdapter = servletContainerAdapter;
  }
  
  public AsyncServletStreamServerConfigurationImpl(ServletContainerAdapter servletContainerAdapter, int listenPort)
  {
    this.servletContainerAdapter = servletContainerAdapter;
    this.listenPort = listenPort;
  }
  
  public AsyncServletStreamServerConfigurationImpl(ServletContainerAdapter servletContainerAdapter, int listenPort, int asyncTimeoutSeconds)
  {
    this.servletContainerAdapter = servletContainerAdapter;
    this.listenPort = listenPort;
    this.asyncTimeoutSeconds = asyncTimeoutSeconds;
  }
  
  public int getListenPort()
  {
    return this.listenPort;
  }
  
  public void setListenPort(int listenPort)
  {
    this.listenPort = listenPort;
  }
  
  public int getAsyncTimeoutSeconds()
  {
    return this.asyncTimeoutSeconds;
  }
  
  public void setAsyncTimeoutSeconds(int asyncTimeoutSeconds)
  {
    this.asyncTimeoutSeconds = asyncTimeoutSeconds;
  }
  
  public ServletContainerAdapter getServletContainerAdapter()
  {
    return this.servletContainerAdapter;
  }
  
  public void setServletContainerAdapter(ServletContainerAdapter servletContainerAdapter)
  {
    this.servletContainerAdapter = servletContainerAdapter;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\AsyncServletStreamServerConfigurationImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */