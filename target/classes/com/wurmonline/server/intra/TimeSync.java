package com.wurmonline.server.intra;

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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\intra\TimeSync.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */