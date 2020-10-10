package com.google.common.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;

public class ListenableFutureTask<V>
  extends FutureTask<V>
  implements ListenableFuture<V>
{
  private final ExecutionList executionList = new ExecutionList();
  
  public static <V> ListenableFutureTask<V> create(Callable<V> callable)
  {
    return new ListenableFutureTask(callable);
  }
  
  public static <V> ListenableFutureTask<V> create(Runnable runnable, @Nullable V result)
  {
    return new ListenableFutureTask(runnable, result);
  }
  
  ListenableFutureTask(Callable<V> callable)
  {
    super(callable);
  }
  
  ListenableFutureTask(Runnable runnable, @Nullable V result)
  {
    super(runnable, result);
  }
  
  public void addListener(Runnable listener, Executor exec)
  {
    this.executionList.add(listener, exec);
  }
  
  protected void done()
  {
    this.executionList.execute();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\ListenableFutureTask.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */