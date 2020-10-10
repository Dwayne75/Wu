package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class ForwardingFuture<V>
  extends ForwardingObject
  implements Future<V>
{
  protected abstract Future<V> delegate();
  
  public boolean cancel(boolean mayInterruptIfRunning)
  {
    return delegate().cancel(mayInterruptIfRunning);
  }
  
  public boolean isCancelled()
  {
    return delegate().isCancelled();
  }
  
  public boolean isDone()
  {
    return delegate().isDone();
  }
  
  public V get()
    throws InterruptedException, ExecutionException
  {
    return (V)delegate().get();
  }
  
  public V get(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException
  {
    return (V)delegate().get(timeout, unit);
  }
  
  public static abstract class SimpleForwardingFuture<V>
    extends ForwardingFuture<V>
  {
    private final Future<V> delegate;
    
    protected SimpleForwardingFuture(Future<V> delegate)
    {
      this.delegate = ((Future)Preconditions.checkNotNull(delegate));
    }
    
    protected final Future<V> delegate()
    {
      return this.delegate;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\ForwardingFuture.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */