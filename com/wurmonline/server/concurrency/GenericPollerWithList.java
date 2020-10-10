package com.wurmonline.server.concurrency;

import com.wurmonline.server.banks.Bank;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericPollerWithList<V>
  implements Callable
{
  private static Logger logger = Logger.getLogger(GenericPollerWithList.class.getName());
  private int iFirstID;
  private int iLastID;
  private List<? extends Pollable> iTaskList;
  
  public GenericPollerWithList(int aFirstID, int aLastID, List<? extends Pollable> aTaskList)
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.entering(GenericPollerWithList.class.getName(), "GenericPollerWithList()", new Object[] {
        Integer.valueOf(aFirstID), Integer.valueOf(aLastID), aTaskList });
    }
    if (aTaskList != null) {
      this.iTaskList = aTaskList;
    } else {
      throw new IllegalArgumentException("GenericPollerWithList TaskList argument must not be null");
    }
    if (aFirstID < 0) {
      this.iFirstID = 0;
    } else {
      this.iFirstID = aFirstID;
    }
    if (aLastID < this.iFirstID) {
      this.iLastID = this.iFirstID;
    } else if (aLastID > aTaskList.size()) {
      this.iLastID = aTaskList.size();
    } else {
      this.iLastID = aLastID;
    }
  }
  
  public Long call()
    throws Exception
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.entering(GenericPollerWithList.class.getName(), "call()");
    }
    long start = System.nanoTime();
    System.out.println("TASK CALLED");
    for (int i = this.iFirstID; i < this.iLastID; i++)
    {
      Pollable lTask = (Pollable)this.iTaskList.get(i);
      if ((lTask != null) && ((lTask instanceof Bank))) {
        ((Bank)lTask).poll(System.currentTimeMillis());
      } else {
        logger.log(Level.WARNING, "Unsupported Pollable Class: " + lTask);
      }
    }
    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "Tasks from " + this.iFirstID + " to " + this.iLastID + " took " + (float)(System.nanoTime() - start) / 1000000.0F + "ms");
    }
    return Long.valueOf(System.nanoTime() - start);
  }
  
  public String toString()
  {
    return "GenericPollerWithList + First ID: " + this.iFirstID + ", Last ID: " + this.iLastID + ", Number of Tasks: " + this.iTaskList.size();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\concurrency\GenericPollerWithList.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */