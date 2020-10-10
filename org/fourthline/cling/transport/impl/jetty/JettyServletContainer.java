package org.fourthline.cling.transport.impl.jetty;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.fourthline.cling.transport.spi.ServletContainerAdapter;

public class JettyServletContainer
  implements ServletContainerAdapter
{
  private static final Logger log = Logger.getLogger(JettyServletContainer.class.getName());
  public static final JettyServletContainer INSTANCE = new JettyServletContainer();
  protected Server server;
  
  private JettyServletContainer()
  {
    resetServer();
  }
  
  public synchronized void setExecutorService(ExecutorService executorService)
  {
    if (INSTANCE.server.getThreadPool() == null) {
      INSTANCE.server.setThreadPool(new ExecutorThreadPool(executorService)
      {
        protected void doStop()
          throws Exception
        {}
      });
    }
  }
  
  public synchronized int addConnector(String host, int port)
    throws IOException
  {
    SocketConnector connector = new SocketConnector();
    connector.setHost(host);
    connector.setPort(port);
    
    connector.open();
    
    this.server.addConnector(connector);
    if (this.server.isStarted()) {
      try
      {
        connector.start();
      }
      catch (Exception ex)
      {
        log.severe("Couldn't start connector: " + connector + " " + ex);
        throw new RuntimeException(ex);
      }
    }
    return connector.getLocalPort();
  }
  
  public synchronized void removeConnector(String host, int port)
  {
    Connector[] connectors = this.server.getConnectors();
    for (Connector connector : connectors) {
      if ((connector.getHost().equals(host)) && (connector.getLocalPort() == port))
      {
        if ((connector.isStarted()) || (connector.isStarting())) {
          try
          {
            connector.stop();
          }
          catch (Exception ex)
          {
            log.severe("Couldn't stop connector: " + connector + " " + ex);
            throw new RuntimeException(ex);
          }
        }
        this.server.removeConnector(connector);
        if (connectors.length != 1) {
          break;
        }
        log.info("No more connectors, stopping Jetty server");
        stopIfRunning(); break;
      }
    }
  }
  
  public synchronized void registerServlet(String contextPath, Servlet servlet)
  {
    if (this.server.getHandler() != null) {
      return;
    }
    log.info("Registering UPnP servlet under context path: " + contextPath);
    ServletContextHandler servletHandler = new ServletContextHandler(0);
    if ((contextPath != null) && (contextPath.length() > 0)) {
      servletHandler.setContextPath(contextPath);
    }
    ServletHolder s = new ServletHolder(servlet);
    servletHandler.addServlet(s, "/*");
    this.server.setHandler(servletHandler);
  }
  
  public synchronized void startIfNotRunning()
  {
    if ((!this.server.isStarted()) && (!this.server.isStarting()))
    {
      log.info("Starting Jetty server... ");
      try
      {
        this.server.start();
      }
      catch (Exception ex)
      {
        log.severe("Couldn't start Jetty server: " + ex);
        throw new RuntimeException(ex);
      }
    }
  }
  
  public synchronized void stopIfRunning()
  {
    if ((!this.server.isStopped()) && (!this.server.isStopping()))
    {
      log.info("Stopping Jetty server...");
      try
      {
        this.server.stop();
      }
      catch (Exception ex)
      {
        log.severe("Couldn't stop Jetty server: " + ex);
        throw new RuntimeException(ex);
      }
      finally
      {
        resetServer();
      }
    }
  }
  
  protected void resetServer()
  {
    this.server = new Server();
    this.server.setGracefulShutdown(1000);
  }
  
  public static boolean isConnectionOpen(HttpServletRequest request)
  {
    return isConnectionOpen(request, " ".getBytes());
  }
  
  public static boolean isConnectionOpen(HttpServletRequest request, byte[] heartbeat)
  {
    Request jettyRequest = (Request)request;
    AbstractHttpConnection connection = jettyRequest.getConnection();
    Socket socket = (Socket)connection.getEndPoint().getTransport();
    if (log.isLoggable(Level.FINE)) {
      log.fine("Checking if client connection is still open: " + socket.getRemoteSocketAddress());
    }
    try
    {
      socket.getOutputStream().write(heartbeat);
      socket.getOutputStream().flush();
      return true;
    }
    catch (IOException ex)
    {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Client connection has been closed: " + socket.getRemoteSocketAddress());
      }
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\jetty\JettyServletContainer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */