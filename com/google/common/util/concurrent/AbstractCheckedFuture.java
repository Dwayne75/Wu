package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Beta
public abstract class AbstractCheckedFuture<V, X extends Exception>
  extends ForwardingListenableFuture.SimpleForwardingListenableFuture<V>
  implements CheckedFuture<V, X>
{
  protected AbstractCheckedFuture(ListenableFuture<V> delegate)
  {
    super(delegate);
  }
  
  protected abstract X mapException(Exception paramException);
  
  public V checkedGet()
    throws Exception
  {
    try
    {
      return (V)get();
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
      throw mapException(e);
    }
    catch (CancellationException e)
    {
      throw mapException(e);
    }
    catch (ExecutionException e)
    {
      throw mapException(e);
    }
  }
  
  public V checkedGet(long timeout, TimeUnit unit)
    throws TimeoutException, Exception
  {
    try
    {
      return (V)get(timeout, unit);
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
      throw mapException(e);
    }
    catch (CancellationException e)
    {
      throw mapException(e);
    }
    catch (ExecutionException e)
    {
      throw mapException(e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\AbstractCheckedFuture.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */