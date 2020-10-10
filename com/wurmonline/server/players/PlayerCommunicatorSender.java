package com.wurmonline.server.players;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerCommunicatorSender
  implements Runnable
{
  private static Logger logger = Logger.getLogger(PlayerCommunicatorSender.class.getName());
  
  public PlayerCommunicatorSender()
  {
    logger.info("Creating");
  }
  
  public void run()
  {
    logger.info("Starting on " + Thread.currentThread());
    try
    {
      Player lPlayer = null;
      for (;;)
      {
        PlayerMessage lMessage = (PlayerMessage)PlayerCommunicatorQueued.getMessageQueue().take();
        if (lMessage != null)
        {
          if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Removed " + lMessage);
          }
          try
          {
            lPlayer = Players.getInstance().getPlayer(lMessage.getPlayerId());
            SocketConnection lConnection = lPlayer.getCommunicator().getConnection();
            if ((lPlayer.hasLink()) && (lConnection.isConnected()))
            {
              ByteBuffer lBuffer = lConnection.getBuffer();
              lBuffer.put(lMessage.getMessageBytes());
              lConnection.flush();
              if (!lConnection.tickWriting(1000000L)) {
                logger.warning("Could not get a lock within 1ms to send message: " + lMessage);
              } else if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Sent message through connection: " + lMessage);
              }
            }
            else if (logger.isLoggable(Level.FINEST))
            {
              logger.finest("Player is not connected so cannot send message: " + lMessage);
            }
          }
          catch (NoSuchPlayerException e)
          {
            logger.log(Level.WARNING, "Could not find Player for Message: " + lMessage + " - " + e.getMessage(), e);
          }
          catch (IOException e)
          {
            logger.log(Level.WARNING, lPlayer.getName() + ": Message: " + lMessage + " - " + e.getMessage(), e);
            lPlayer.setLink(false);
          }
          Thread.yield();
        }
        else
        {
          logger.warning("Removed null message from Queue");
        }
      }
    }
    catch (RuntimeException e)
    {
      logger.log(Level.WARNING, "Problem running - " + e.getMessage(), e);
      
      Server.getInstance().initialisePlayerCommunicatorSender();
    }
    catch (InterruptedException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
      Server.getInstance().initialisePlayerCommunicatorSender();
    }
    finally
    {
      logger.info("Finished");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\PlayerCommunicatorSender.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */