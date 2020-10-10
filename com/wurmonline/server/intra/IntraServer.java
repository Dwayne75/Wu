package com.wurmonline.server.intra;

import com.wurmonline.communication.ServerListener;
import com.wurmonline.communication.SocketConnection;
import com.wurmonline.communication.SocketServer;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.ServerMonitoring;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class IntraServer
  implements ServerListener, CounterTypes, MiscConstants, TimeConstants
{
  private static final Logger logger = Logger.getLogger(IntraServer.class.getName());
  public SocketServer socketServer;
  private final ServerMonitoring wurmserver;
  
  public IntraServer(ServerMonitoring server)
    throws IOException
  {
    this.wurmserver = server;
    this.socketServer = new SocketServer(server.getInternalIp(), server.getIntraServerPort(), server.getIntraServerPort() + 1, this);
    
    this.socketServer.intraServer = true;
    logger.log(Level.INFO, "Intraserver listening on " + 
    
      InetAddress.getByAddress(server.getInternalIp()) + ':' + server
      .getIntraServerPort());
  }
  
  public void clientConnected(SocketConnection serverConnection)
  {
    try
    {
      IntraServerConnection conn = new IntraServerConnection(serverConnection, this.wurmserver);
      serverConnection.setConnectionListener(conn);
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("IntraServer client connected from IP " + serverConnection.getIp());
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "Failed to create intraserver connection: " + serverConnection + '.', ex);
    }
  }
  
  public void clientException(SocketConnection conn, Exception ex)
  {
    if (logger.isLoggable(Level.FINE)) {
      logger.log(Level.FINE, "Remote server lost link on connection: " + conn + " - cause:" + ex.getMessage(), ex);
    }
    if (conn != null)
    {
      try
      {
        conn.flush();
      }
      catch (Exception localException) {}
      conn.sendShutdown();
      try
      {
        conn.disconnect();
      }
      catch (Exception localException1) {}
      conn.closeChannel();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\intra\IntraServer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */