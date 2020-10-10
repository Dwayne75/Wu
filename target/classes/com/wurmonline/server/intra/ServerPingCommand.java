package com.wurmonline.server.intra;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ServerPingCommand
  extends IntraCommand
{
  private static final Logger logger = Logger.getLogger(ServerPingCommand.class.getName());
  private final ServerEntry server;
  private boolean done = false;
  private IntraClient client;
  private boolean pinging = false;
  
  ServerPingCommand(ServerEntry serverEntry)
  {
    this.server = serverEntry;
  }
  
  public boolean poll()
  {
    if (this.server.id == Servers.localServer.id) {
      return true;
    }
    if (this.client == null)
    {
      if (logger.isLoggable(Level.FINER)) {
        logger.finer("1 " + getLoginServerIntraServerAddress() + ", " + getLoginServerIntraServerPort() + ", " + 
          getLoginServerIntraServerPassword());
      }
      try
      {
        this.client = new IntraClient(getLoginServerIntraServerAddress(), Integer.parseInt(getLoginServerIntraServerPort()), this);
        this.client.login(this.server.INTRASERVERPASSWORD, true);
        logger.log(Level.INFO, "connecting to " + this.server.id);
      }
      catch (IOException iox)
      {
        this.server.setAvailable(false, false, 0, 0, 0, 10);
        
        this.client.disconnect("Failed.");
        this.client = null;
        logger.log(Level.INFO, "Failed");
        this.done = true;
      }
    }
    if ((this.client != null) && (!this.done))
    {
      if (System.currentTimeMillis() > this.timeOutAt) {
        this.done = true;
      }
      if (!this.done) {
        try
        {
          if (this.client.loggedIn) {
            if (!this.pinging)
            {
              this.client.executePingCommand();
              this.pinging = true;
              this.timeOutAt = (System.currentTimeMillis() + this.timeOutTime);
            }
          }
          if (!this.done) {
            this.client.update();
          }
        }
        catch (IOException iox)
        {
          this.server.setAvailable(false, false, 0, 0, 0, 10);
          
          this.done = true;
        }
      }
      if ((this.done) && (this.client != null))
      {
        this.client.disconnect("Done");
        this.client = null;
      }
    }
    return this.done;
  }
  
  public void commandExecuted(IntraClient aClient)
  {
    this.done = true;
  }
  
  public void commandFailed(IntraClient aClient)
  {
    this.server.setAvailable(false, false, 0, 0, 0, 10);
    
    this.done = true;
  }
  
  public void dataReceived(IntraClient aClient)
  {
    this.done = true;
  }
  
  public void reschedule(IntraClient aClient)
  {
    this.server.setAvailable(false, false, 0, 0, 0, 10);
    this.done = true;
  }
  
  public void remove(IntraClient aClient)
  {
    this.done = true;
  }
  
  public void receivingData(ByteBuffer buffer)
  {
    boolean maintaining = (buffer.get() & 0x1) == 1;
    int numsPlaying = buffer.getInt();
    int maxLimit = buffer.getInt();
    int secsToShutdown = buffer.getInt();
    int meshSize = buffer.getInt();
    this.server.setAvailable(true, maintaining, numsPlaying, maxLimit, secsToShutdown, meshSize);
    
    this.timeOutAt = (System.currentTimeMillis() + this.timeOutTime);
    this.done = true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\intra\ServerPingCommand.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */