package org.fourthline.cling.transport.impl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.Connection;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.StreamServer;

public class StreamServerImpl
  implements StreamServer<StreamServerConfigurationImpl>
{
  private static Logger log = Logger.getLogger(StreamServer.class.getName());
  protected final StreamServerConfigurationImpl configuration;
  protected HttpServer server;
  
  public StreamServerImpl(StreamServerConfigurationImpl configuration)
  {
    this.configuration = configuration;
  }
  
  public synchronized void init(InetAddress bindAddress, Router router)
    throws InitializationException
  {
    try
    {
      InetSocketAddress socketAddress = new InetSocketAddress(bindAddress, this.configuration.getListenPort());
      
      this.server = HttpServer.create(socketAddress, this.configuration.getTcpConnectionBacklog());
      this.server.createContext("/", new RequestHttpHandler(router));
      
      log.info("Created server (for receiving TCP streams) on: " + this.server.getAddress());
    }
    catch (Exception ex)
    {
      throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex.toString(), ex);
    }
  }
  
  public synchronized int getPort()
  {
    return this.server.getAddress().getPort();
  }
  
  public StreamServerConfigurationImpl getConfiguration()
  {
    return this.configuration;
  }
  
  public synchronized void run()
  {
    log.fine("Starting StreamServer...");
    
    this.server.start();
  }
  
  public synchronized void stop()
  {
    log.fine("Stopping StreamServer...");
    if (this.server != null) {
      this.server.stop(1);
    }
  }
  
  protected class RequestHttpHandler
    implements HttpHandler
  {
    private final Router router;
    
    public RequestHttpHandler(Router router)
    {
      this.router = router;
    }
    
    public void handle(final HttpExchange httpExchange)
      throws IOException
    {
      StreamServerImpl.log.fine("Received HTTP exchange: " + httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI());
      this.router.received(new HttpExchangeUpnpStream(this.router
        .getProtocolFactory(), httpExchange)
        {
          protected Connection createConnection()
          {
            return new StreamServerImpl.HttpServerConnection(StreamServerImpl.this, httpExchange);
          }
        });
    }
  }
  
  protected boolean isConnectionOpen(HttpExchange exchange)
  {
    log.warning("Can't check client connection, socket access impossible on JDK webserver!");
    return true;
  }
  
  protected class HttpServerConnection
    implements Connection
  {
    protected HttpExchange exchange;
    
    public HttpServerConnection(HttpExchange exchange)
    {
      this.exchange = exchange;
    }
    
    public boolean isOpen()
    {
      return StreamServerImpl.this.isConnectionOpen(this.exchange);
    }
    
    public InetAddress getRemoteAddress()
    {
      return this.exchange.getRemoteAddress() != null ? this.exchange.getRemoteAddress().getAddress() : null;
    }
    
    public InetAddress getLocalAddress()
    {
      return this.exchange.getLocalAddress() != null ? this.exchange.getLocalAddress().getAddress() : null;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\StreamServerImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */