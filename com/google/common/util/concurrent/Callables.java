package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;

public final class Callables
{
  public static <T> Callable<T> returning(@Nullable T value)
  {
    new Callable()
    {
      public T call()
      {
        return (T)this.val$value;
      }
    };
  }
  
  static <T> Callable<T> threadRenaming(final Callable<T> callable, Supplier<String> nameSupplier)
  {
    Preconditions.checkNotNull(nameSupplier);
    Preconditions.checkNotNull(callable);
    new Callable()
    {
      public T call()
        throws Exception
      {
        Thread currentThread = Thread.currentThread();
        String oldName = currentThread.getName();
        boolean restoreName = Callables.trySetName((String)this.val$nameSupplier.get(), currentThread);
        try
        {
          return (T)callable.call();
        }
        finally
        {
          if (restoreName) {
            Callables.trySetName(oldName, currentThread);
          }
        }
      }
    };
  }
  
  static Runnable threadRenaming(final Runnable task, Supplier<String> nameSupplier)
  {
    Preconditions.checkNotNull(nameSupplier);
    Preconditions.checkNotNull(task);
    new Runnable()
    {
      public void run()
      {
        Thread currentThread = Thread.currentThread();
        String oldName = currentThread.getName();
        boolean restoreName = Callables.trySetName((String)this.val$nameSupplier.get(), currentThread);
        try
        {
          task.run();
        }
        finally
        {
          if (restoreName) {
            Callables.trySetName(oldName, currentThread);
          }
        }
      }
    };
  }
  
  private static boolean trySetName(String threadName, Thread currentThread)
  {
    try
    {
      currentThread.setName(threadName);
      return true;
    }
    catch (SecurityException e) {}
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\Callables.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */