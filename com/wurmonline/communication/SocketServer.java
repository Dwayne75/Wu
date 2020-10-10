package com.wurmonline.communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketServer
{
  private final ServerSocketChannel ssc;
  private final ServerListener serverListener;
  private final List<SocketConnection> connections = new LinkedList();
  public static final ReentrantReadWriteLock CONNECTIONS_RW_LOCK = new ReentrantReadWriteLock();
  private final int acceptedPort;
  public boolean intraServer = false;
  private static final Logger logger = Logger.getLogger(SocketServer.class.getName());
  private static Map<String, Long> connectedIps = new HashMap();
  public static long MIN_MILLIS_BETWEEN_CONNECTIONS = 1000L;
  
  public SocketServer(byte[] ips, int port, int acceptedPort, ServerListener serverListener)
    throws IOException
  {
    this.serverListener = serverListener;
    this.acceptedPort = acceptedPort;
    InetAddress hostip = InetAddress.getByAddress(ips);
    logger.info("Creating Wurm SocketServer on " + hostip + ':' + port);
    this.ssc = ServerSocketChannel.open();
    this.ssc.socket().bind(new InetSocketAddress(hostip, port));
    
    this.ssc.configureBlocking(false);
  }
  
  public void tick()
    throws IOException
  {
    SocketChannel socketChannel;
    while ((socketChannel = this.ssc.accept()) != null) {
      try
      {
        if (socketChannel.socket().getPort() != this.acceptedPort)
        {
          if (!this.intraServer) {
            logger.log(Level.INFO, "Accepted player connection: " + socketChannel.socket());
          }
        }
        else if (!this.intraServer) {
          logger.log(Level.INFO, socketChannel.socket().getRemoteSocketAddress() + " connected from the correct port");
        }
        boolean keepGoing = true;
        if ((!this.intraServer) && (MIN_MILLIS_BETWEEN_CONNECTIONS > 0L))
        {
          String remoteIp = socketChannel.socket().getRemoteSocketAddress().toString().substring(0, socketChannel.socket().getRemoteSocketAddress().toString().indexOf(":"));
          Long lastConnTime = (Long)connectedIps.get(remoteIp);
          if (lastConnTime != null)
          {
            long lct = lastConnTime.longValue();
            if (System.currentTimeMillis() - lct < MIN_MILLIS_BETWEEN_CONNECTIONS)
            {
              logger.log(Level.INFO, "Disconnecting " + remoteIp + " due to too many connections.");
              if ((socketChannel != null) && (socketChannel.socket() != null)) {
                try
                {
                  socketChannel.socket().close();
                }
                catch (IOException iox)
                {
                  iox.printStackTrace();
                }
              }
              if (socketChannel != null) {
                try
                {
                  socketChannel.close();
                }
                catch (IOException iox)
                {
                  iox.printStackTrace();
                }
              }
              keepGoing = false;
            }
          }
          else
          {
            connectedIps.put(remoteIp, new Long(System.currentTimeMillis()));
          }
        }
        if (keepGoing)
        {
          socketChannel.configureBlocking(false);
          SocketConnection socketConnection = new SocketConnection(socketChannel, true, this.intraServer);
          CONNECTIONS_RW_LOCK.writeLock().lock();
          try
          {
            this.connections.add(socketConnection);
          }
          finally
          {
            CONNECTIONS_RW_LOCK.writeLock().unlock();
          }
          this.serverListener.clientConnected(socketConnection);
        }
      }
      catch (IOException e)
      {
        try
        {
          socketChannel.close();
        }
        catch (Exception localException1) {}
        throw e;
      }
    }
    CONNECTIONS_RW_LOCK.writeLock().lock();
    try
    {
      for (it = this.connections.iterator(); it.hasNext();)
      {
        SocketConnection socketConnection = (SocketConnection)it.next();
        if (!socketConnection.isConnected())
        {
          socketConnection.disconnect();
          this.serverListener.clientException(socketConnection, new Exception());
          it.remove();
        }
        else
        {
          try
          {
            socketConnection.tick();
          }
          catch (Exception e)
          {
            socketConnection.disconnect();
            this.serverListener.clientException(socketConnection, e);
            it.remove();
          }
        }
      }
    }
    finally
    {
      Iterator<SocketConnection> it;
      CONNECTIONS_RW_LOCK.writeLock().unlock();
    }
  }
  
  public int getNumberOfConnections()
  {
    CONNECTIONS_RW_LOCK.readLock().lock();
    try
    {
      int i;
      if (this.connections != null) {
        return this.connections.size();
      }
      return 0;
    }
    finally
    {
      CONNECTIONS_RW_LOCK.readLock().unlock();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\communication\SocketServer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */