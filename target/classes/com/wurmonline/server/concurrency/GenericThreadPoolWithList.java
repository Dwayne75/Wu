package com.wurmonline.server.concurrency;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericThreadPoolWithList
{
  private static Logger logger = Logger.getLogger(GenericThreadPoolWithList.class.getName());
  public static final String VERSION = "$Revision: 1.0 $";
  
  public static void multiThreadedPoll(List<? extends Pollable> lInputList, int aNumberOfTasks)
  {
    System.out.println("Polling banks");
    
    ExecutorService execSvc = Executors.newCachedThreadPool();
    
    int lLastID = lInputList.size();
    int lFirstID = 0;
    
    List toRun = new ArrayList();
    int lNumberOfTasks = Math.min(aNumberOfTasks, lInputList.size());
    for (int i = 1; i <= aNumberOfTasks; i++)
    {
      if (lNumberOfTasks > i) {
        break;
      }
      int m = lLastID * i / aNumberOfTasks;
      if (logger.isLoggable(Level.FINEST)) {
        logger.log(Level.FINEST, i + " - First: " + lFirstID + ", last: " + m);
      }
      toRun.add(new GenericPollerWithList(lFirstID, m, lInputList));
      System.out.println("ADDED A TASK");
      lFirstID = m + 1;
    }
    long start = System.nanoTime();
    try
    {
      execSvc.invokeAll(toRun);
      if (logger.isLoggable(Level.FINEST)) {
        logger.log(Level.FINEST, "invokeAll took " + (float)(System.nanoTime() - start) / 1000000.0F + "ms");
      }
    }
    catch (InterruptedException e)
    {
      logger.log(Level.WARNING, "task invocation interrupted", e);
    }
    catch (RejectedExecutionException e)
    {
      if (!execSvc.isShutdown()) {
        logger.log(Level.WARNING, "task submission rejected", e);
      }
    }
    execSvc.shutdown();
    if ((execSvc instanceof ThreadPoolExecutor))
    {
      ThreadPoolExecutor tpe = (ThreadPoolExecutor)execSvc;
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "ThreadPoolExecutor CorePoolSize: " + tpe.getCorePoolSize() + ", LargestPoolSize: " + tpe.getLargestPoolSize() + ", TaskCount: " + tpe
          .getTaskCount());
      }
    }
    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "execSvc.isTerminated(): " + execSvc.isTerminated() + " took: " + (float)(System.nanoTime() - start) / 1000000.0F + "ms");
    }
    try
    {
      if (!execSvc.awaitTermination(30L, TimeUnit.SECONDS)) {
        logger.log(Level.WARNING, "ThreadPoolExceutor timed out instead of terminating");
      }
    }
    catch (InterruptedException e)
    {
      logger.log(Level.WARNING, "task awaitTermination interrupted", e);
    }
    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "execSvc.isTerminated(): " + execSvc.isTerminated() + " took: " + (float)(System.nanoTime() - start) / 1000000.0F + "ms");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\concurrency\GenericThreadPoolWithList.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */