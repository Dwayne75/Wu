package org.seamless.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

public class Threads
{
  public static ThreadGroup getRootThreadGroup()
  {
    ThreadGroup tg = Thread.currentThread().getThreadGroup();
    ThreadGroup ptg;
    while ((ptg = tg.getParent()) != null) {
      tg = ptg;
    }
    return tg;
  }
  
  public static Thread[] getAllThreads()
  {
    ThreadGroup root = getRootThreadGroup();
    ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
    int nAlloc = thbean.getThreadCount();
    int n = 0;
    Thread[] threads;
    do
    {
      nAlloc *= 2;
      threads = new Thread[nAlloc];
      n = root.enumerate(threads, true);
    } while (n == nAlloc);
    return (Thread[])Arrays.copyOf(threads, n);
  }
  
  public static Thread getThread(long id)
  {
    Thread[] threads = getAllThreads();
    for (Thread thread : threads) {
      if (thread.getId() == id) {
        return thread;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\Threads.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */