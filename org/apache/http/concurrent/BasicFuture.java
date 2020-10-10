package org.apache.http.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BasicFuture<T>
  implements Future<T>, Cancellable
{
  private final FutureCallback<T> callback;
  private volatile boolean completed;
  private volatile boolean cancelled;
  private volatile T result;
  private volatile Exception ex;
  
  public BasicFuture(FutureCallback<T> callback)
  {
    this.callback = callback;
  }
  
  public boolean isCancelled()
  {
    return this.cancelled;
  }
  
  public boolean isDone()
  {
    return this.completed;
  }
  
  private T getResult()
    throws ExecutionException
  {
    if (this.ex != null) {
      throw new ExecutionException(this.ex);
    }
    return (T)this.result;
  }
  
  public synchronized T get()
    throws InterruptedException, ExecutionException
  {
    while (!this.completed) {
      wait();
    }
    return (T)getResult();
  }
  
  public synchronized T get(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException
  {
    long msecs = unit.toMillis(timeout);
    long startTime = msecs <= 0L ? 0L : System.currentTimeMillis();
    long waitTime = msecs;
    if (this.completed) {
      return (T)getResult();
    }
    if (waitTime <= 0L) {
      throw new TimeoutException();
    }
    do
    {
      wait(waitTime);
      if (this.completed) {
        return (T)getResult();
      }
      waitTime = msecs - (System.currentTimeMillis() - startTime);
    } while (waitTime > 0L);
    throw new TimeoutException();
  }
  
  public boolean completed(T result)
  {
    synchronized (this)
    {
      if (this.completed) {
        return false;
      }
      this.completed = true;
      this.result = result;
      notifyAll();
    }
    if (this.callback != null) {
      this.callback.completed(result);
    }
    return true;
  }
  
  public boolean failed(Exception exception)
  {
    synchronized (this)
    {
      if (this.completed) {
        return false;
      }
      this.completed = true;
      this.ex = exception;
      notifyAll();
    }
    if (this.callback != null) {
      this.callback.failed(exception);
    }
    return true;
  }
  
  public boolean cancel(boolean mayInterruptIfRunning)
  {
    synchronized (this)
    {
      if (this.completed) {
        return false;
      }
      this.completed = true;
      this.cancelled = true;
      notifyAll();
    }
    if (this.callback != null) {
      this.callback.cancelled();
    }
    return true;
  }
  
  public boolean cancel()
  {
    return cancel(true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\concurrent\BasicFuture.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */