package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

final class SerializingExecutor
  implements Executor
{
  private static final Logger log = Logger.getLogger(SerializingExecutor.class.getName());
  private final Executor executor;
  @GuardedBy("internalLock")
  private final Queue<Runnable> waitQueue = new ArrayDeque();
  @GuardedBy("internalLock")
  private boolean isThreadScheduled = false;
  private final TaskRunner taskRunner = new TaskRunner(null);
  
  public SerializingExecutor(Executor executor)
  {
    Preconditions.checkNotNull(executor, "'executor' must not be null.");
    this.executor = executor;
  }
  
  private final Object internalLock = new Object()
  {
    /* Error */
    public String toString()
    {
      // Byte code:
      //   0: ldc 3
      //   2: aload_0
      //   3: invokespecial 4	java/lang/Object:toString	()Ljava/lang/String;
      //   6: invokestatic 5	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
      //   9: dup
      //   10: invokevirtual 6	java/lang/String:length	()I
      //   13: ifeq +9 -> 22
      //   16: invokevirtual 7	java/lang/String:concat	(Ljava/lang/String;)Ljava/lang/String;
      //   19: goto +12 -> 31
      //   22: pop
      //   23: new 8	java/lang/String
      //   26: dup_x1
      //   27: swap
      //   28: invokespecial 9	java/lang/String:<init>	(Ljava/lang/String;)V
      //   31: areturn
      // Line number table:
      //   Java source line #83	-> byte code offset #0
      // Local variable table:
      //   start	length	slot	name	signature
      //   0	32	0	this	1
    }
  };
  
  public void execute(Runnable r)
  {
    Preconditions.checkNotNull(r, "'r' must not be null.");
    boolean scheduleTaskRunner = false;
    synchronized (this.internalLock)
    {
      this.waitQueue.add(r);
      if (!this.isThreadScheduled)
      {
        this.isThreadScheduled = true;
        scheduleTaskRunner = true;
      }
    }
    if (scheduleTaskRunner)
    {
      boolean threw = true;
      try
      {
        this.executor.execute(this.taskRunner);
        threw = false;
      }
      finally
      {
        if (threw) {
          synchronized (this.internalLock)
          {
            this.isThreadScheduled = false;
          }
        }
      }
    }
  }
  
  private class TaskRunner
    implements Runnable
  {
    private TaskRunner() {}
    
    public void run()
    {
      boolean stillRunning = true;
      try
      {
        for (;;)
        {
          Preconditions.checkState(SerializingExecutor.this.isThreadScheduled);
          Runnable nextToRun;
          synchronized (SerializingExecutor.this.internalLock)
          {
            nextToRun = (Runnable)SerializingExecutor.this.waitQueue.poll();
            if (nextToRun == null)
            {
              SerializingExecutor.this.isThreadScheduled = false;
              stillRunning = false;
              break;
            }
          }
          try
          {
            nextToRun.run();
          }
          catch (RuntimeException e)
          {
            String str = String.valueOf(String.valueOf(nextToRun));SerializingExecutor.log.log(Level.SEVERE, 35 + str.length() + "Exception while executing runnable " + str, e);
          }
        }
      }
      finally
      {
        if (stillRunning) {
          synchronized (SerializingExecutor.this.internalLock)
          {
            SerializingExecutor.this.isThreadScheduled = false;
          }
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\SerializingExecutor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */