package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

final class ListenerCallQueue<L>
  implements Runnable
{
  private static final Logger logger = Logger.getLogger(ListenerCallQueue.class.getName());
  private final L listener;
  private final Executor executor;
  
  static abstract class Callback<L>
  {
    private final String methodCall;
    
    Callback(String methodCall)
    {
      this.methodCall = methodCall;
    }
    
    abstract void call(L paramL);
    
    void enqueueOn(Iterable<ListenerCallQueue<L>> queues)
    {
      for (ListenerCallQueue<L> queue : queues) {
        queue.add(this);
      }
    }
  }
  
  @GuardedBy("this")
  private final Queue<Callback<L>> waitQueue = Queues.newArrayDeque();
  @GuardedBy("this")
  private boolean isThreadScheduled;
  
  ListenerCallQueue(L listener, Executor executor)
  {
    this.listener = Preconditions.checkNotNull(listener);
    this.executor = ((Executor)Preconditions.checkNotNull(executor));
  }
  
  synchronized void add(Callback<L> callback)
  {
    this.waitQueue.add(callback);
  }
  
  void execute()
  {
    boolean scheduleTaskRunner = false;
    synchronized (this)
    {
      if (!this.isThreadScheduled)
      {
        this.isThreadScheduled = true;
        scheduleTaskRunner = true;
      }
    }
    if (scheduleTaskRunner) {
      try
      {
        this.executor.execute(this);
      }
      catch (RuntimeException e)
      {
        synchronized (this)
        {
          this.isThreadScheduled = false;
        }
        ??? = String.valueOf(String.valueOf(this.listener));String str = String.valueOf(String.valueOf(this.executor));logger.log(Level.SEVERE, 42 + ((String)???).length() + str.length() + "Exception while running callbacks for " + (String)??? + " on " + str, e);
        
        throw e;
      }
    }
  }
  
  public void run()
  {
    boolean stillRunning = true;
    try
    {
      for (;;)
      {
        Callback<L> nextToRun;
        synchronized (this)
        {
          Preconditions.checkState(this.isThreadScheduled);
          nextToRun = (Callback)this.waitQueue.poll();
          if (nextToRun == null)
          {
            this.isThreadScheduled = false;
            stillRunning = false;
            break;
          }
        }
        try
        {
          nextToRun.call(this.listener);
        }
        catch (RuntimeException e)
        {
          String str1 = String.valueOf(String.valueOf(this.listener));String str2 = String.valueOf(String.valueOf(nextToRun.methodCall));logger.log(Level.SEVERE, 37 + str1.length() + str2.length() + "Exception while executing callback: " + str1 + "." + str2, e);
        }
      }
    }
    finally
    {
      if (stillRunning) {
        synchronized (this)
        {
          this.isThreadScheduled = false;
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\ListenerCallQueue.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */