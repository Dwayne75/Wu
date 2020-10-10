package com.wurmonline.server.intra;

import com.wurmonline.server.Constants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TimeSync
  extends IntraCommand
{
  private static final Logger logger = Logger.getLogger(TimeSync.class.getName());
  private boolean done = false;
  private IntraClient client;
  private boolean sentCommand = false;
  private boolean started = false;
  
  public boolean poll()
  {
    if (isThisLoginServer()) {
      return true;
    }
    if ((this.client == null) && ((System.currentTimeMillis() > this.timeOutAt) || (!this.started))) {
      try
      {
        this.timeOutAt = (System.currentTimeMillis() + this.timeOutTime);
        
        this.client = new IntraClient(getLoginServerIntraServerAddress(), Integer.parseInt(getLoginServerIntraServerPort()), this);
        this.client.login(getLoginServerIntraServerPassword(), true);
        this.started = true;
      }
      catch (IOException iox)
      {
        this.done = true;
      }
    }
    if ((this.client != null) && (!this.done))
    {
      if (System.currentTimeMillis() > this.timeOutAt)
      {
        this.done = true;
      }
      else if ((this.client != null) && (this.client.loggedIn))
      {
        if (logger.isLoggable(Level.FINER)) {
          logger.finer("3.5 sentcommand=" + this.sentCommand);
        }
        if (!this.sentCommand) {
          try
          {
            this.client.executeSyncCommand();
            this.timeOutAt = (System.currentTimeMillis() + this.timeOutTime);
            this.sentCommand = true;
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, iox.getMessage(), iox);
            this.done = true;
          }
        }
      }
      if (!this.done) {
        try
        {
          this.client.update();
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, ex.getMessage(), ex);
          this.done = true;
        }
      }
    }
    if ((this.client != null) && (this.done))
    {
      this.client.disconnect("Done");
      this.client = null;
    }
    return this.done;
  }
  
  public void commandExecuted(IntraClient aClient)
  {
    this.done = true;
  }
  
  public void commandFailed(IntraClient aClient)
  {
    logger.warning("Command failed for Client: " + aClient);
    this.done = true;
  }
  
  public void dataReceived(IntraClient aClient)
  {
    this.done = true;
  }
  
  public void reschedule(IntraClient aClient)
  {
    this.done = true;
  }
  
  public void remove(IntraClient aClient)
  {
    this.done = true;
  }
  
  public void receivingData(ByteBuffer buffer)
  {
    this.done = true;
  }
  
  public static class TimeSyncSender
    implements Runnable
  {
    public TimeSyncSender()
    {
      if (Servers.isThisLoginServer()) {
        throw new IllegalArgumentException("Do not send TimeSync commands from the LoginServer");
      }
    }
    
    public void run()
    {
      if (TimeSync.logger.isLoggable(Level.FINER)) {
        TimeSync.logger.finer("Running newSingleThreadScheduledExecutor for sending TimeSync commands");
      }
      try
      {
        long now = System.currentTimeMillis();
        
        TimeSync synch = new TimeSync();
        Server.getInstance().addIntraCommand(synch);
        
        long lElapsedTime = System.currentTimeMillis() - now;
        if (lElapsedTime > Constants.lagThreshold) {
          TimeSync.logger.info("Finished sending TimeSync command, which took " + lElapsedTime + " millis.");
        }
      }
      catch (RuntimeException e)
      {
        TimeSync.logger.log(Level.WARNING, "Caught exception in ScheduledExecutorService while sending TimeSync command", e);
        throw e;
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\intra\TimeSync.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */