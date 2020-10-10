package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import javax.annotation.Nullable;

final class AsyncSettableFuture<V>
  extends ForwardingListenableFuture<V>
{
  public static <V> AsyncSettableFuture<V> create()
  {
    return new AsyncSettableFuture();
  }
  
  private final NestedFuture<V> nested = new NestedFuture(null);
  private final ListenableFuture<V> dereferenced = Futures.dereference(this.nested);
  
  protected ListenableFuture<V> delegate()
  {
    return this.dereferenced;
  }
  
  public boolean setFuture(ListenableFuture<? extends V> future)
  {
    return this.nested.setFuture((ListenableFuture)Preconditions.checkNotNull(future));
  }
  
  public boolean setValue(@Nullable V value)
  {
    return setFuture(Futures.immediateFuture(value));
  }
  
  public boolean setException(Throwable exception)
  {
    return setFuture(Futures.immediateFailedFuture(exception));
  }
  
  public boolean isSet()
  {
    return this.nested.isDone();
  }
  
  private static final class NestedFuture<V>
    extends AbstractFuture<ListenableFuture<? extends V>>
  {
    boolean setFuture(ListenableFuture<? extends V> value)
    {
      boolean result = set(value);
      if (isCancelled()) {
        value.cancel(wasInterrupted());
      }
      return result;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\AsyncSettableFuture.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */