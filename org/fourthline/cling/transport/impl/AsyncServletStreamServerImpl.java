package org.fourthline.cling.transport.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.message.Connection;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.ServletContainerAdapter;
import org.fourthline.cling.transport.spi.StreamServer;

public class AsyncServletStreamServerImpl
  implements StreamServer<AsyncServletStreamServerConfigurationImpl>
{
  private static final Logger log = Logger.getLogger(StreamServer.class.getName());
  protected final AsyncServletStreamServerConfigurationImpl configuration;
  protected int localPort;
  protected String hostAddress;
  
  public AsyncServletStreamServerImpl(AsyncServletStreamServerConfigurationImpl configuration)
  {
    this.configuration = configuration;
  }
  
  public AsyncServletStreamServerConfigurationImpl getConfiguration()
  {
    return this.configuration;
  }
  
  public synchronized void init(InetAddress bindAddress, Router router)
    throws InitializationException
  {
    try
    {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Setting executor service on servlet container adapter");
      }
      getConfiguration().getServletContainerAdapter().setExecutorService(router
        .getConfiguration().getStreamServerExecutorService());
      if (log.isLoggable(Level.FINE)) {
        log.fine("Adding connector: " + bindAddress + ":" + getConfiguration().getListenPort());
      }
      this.hostAddress = bindAddress.getHostAddress();
      this.localPort = getConfiguration().getServletContainerAdapter().addConnector(this.hostAddress, 
      
        getConfiguration().getListenPort());
      
      String contextPath = router.getConfiguration().getNamespace().getBasePath().getPath();
      getConfiguration().getServletContainerAdapter().registerServlet(contextPath, createServlet(router));
    }
    catch (Exception ex)
    {
      throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex.toString(), ex);
    }
  }
  
  public synchronized int getPort()
  {
    return this.localPort;
  }
  
  public synchronized void stop()
  {
    getConfiguration().getServletContainerAdapter().removeConnector(this.hostAddress, this.localPort);
  }
  
  public void run()
  {
    getConfiguration().getServletContainerAdapter().startIfNotRunning();
  }
  
  private int mCounter = 0;
  
  protected Servlet createServlet(final Router router)
  {
    new HttpServlet()
    {
      protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
      {
        final long startTime = System.currentTimeMillis();
        int counter = AsyncServletStreamServerImpl.access$008(AsyncServletStreamServerImpl.this);
        if (AsyncServletStreamServerImpl.log.isLoggable(Level.FINE)) {
          AsyncServletStreamServerImpl.log.fine(String.format("HttpServlet.service(): id: %3d, request URI: %s", new Object[] { Integer.valueOf(counter), req.getRequestURI() }));
        }
        AsyncContext async = req.startAsync();
        async.setTimeout(AsyncServletStreamServerImpl.this.getConfiguration().getAsyncTimeoutSeconds() * 1000);
        
        async.addListener(new AsyncListener()
        {
          public void onTimeout(AsyncEvent arg0)
            throws IOException
          {
            long duration = System.currentTimeMillis() - startTime;
            if (AsyncServletStreamServerImpl.log.isLoggable(Level.FINE)) {
              AsyncServletStreamServerImpl.log.fine(String.format("AsyncListener.onTimeout(): id: %3d, duration: %,4d, request: %s", new Object[] { Integer.valueOf(this.val$counter), Long.valueOf(duration), arg0.getSuppliedRequest() }));
            }
          }
          
          public void onStartAsync(AsyncEvent arg0)
            throws IOException
          {
            if (AsyncServletStreamServerImpl.log.isLoggable(Level.FINE)) {
              AsyncServletStreamServerImpl.log.fine(String.format("AsyncListener.onStartAsync(): id: %3d, request: %s", new Object[] { Integer.valueOf(this.val$counter), arg0.getSuppliedRequest() }));
            }
          }
          
          public void onError(AsyncEvent arg0)
            throws IOException
          {
            long duration = System.currentTimeMillis() - startTime;
            if (AsyncServletStreamServerImpl.log.isLoggable(Level.FINE)) {
              AsyncServletStreamServerImpl.log.fine(String.format("AsyncListener.onError(): id: %3d, duration: %,4d, response: %s", new Object[] { Integer.valueOf(this.val$counter), Long.valueOf(duration), arg0.getSuppliedResponse() }));
            }
          }
          
          public void onComplete(AsyncEvent arg0)
            throws IOException
          {
            long duration = System.currentTimeMillis() - startTime;
            if (AsyncServletStreamServerImpl.log.isLoggable(Level.FINE)) {
              AsyncServletStreamServerImpl.log.fine(String.format("AsyncListener.onComplete(): id: %3d, duration: %,4d, response: %s", new Object[] { Integer.valueOf(this.val$counter), Long.valueOf(duration), arg0.getSuppliedResponse() }));
            }
          }
        });
        AsyncServletUpnpStream stream = new AsyncServletUpnpStream(router.getProtocolFactory(), async, req)
        {
          protected Connection createConnection()
          {
            return new AsyncServletStreamServerImpl.AsyncServletConnection(AsyncServletStreamServerImpl.this, getRequest());
          }
        };
        router.received(stream);
      }
    };
  }
  
  protected boolean isConnectionOpen(HttpServletRequest request)
  {
    return true;
  }
  
  protected class AsyncServletConnection
    implements Connection
  {
    protected HttpServletRequest request;
    
    public AsyncServletConnection(HttpServletRequest request)
    {
      this.request = request;
    }
    
    public HttpServletRequest getRequest()
    {
      return this.request;
    }
    
    public boolean isOpen()
    {
      return AsyncServletStreamServerImpl.this.isConnectionOpen(getRequest());
    }
    
    public InetAddress getRemoteAddress()
    {
      try
      {
        return InetAddress.getByName(getRequest().getRemoteAddr());
      }
      catch (UnknownHostException ex)
      {
        throw new RuntimeException(ex);
      }
    }
    
    public InetAddress getLocalAddress()
    {
      try
      {
        return InetAddress.getByName(getRequest().getLocalAddr());
      }
      catch (UnknownHostException ex)
      {
        throw new RuntimeException(ex);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\AsyncServletStreamServerImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */