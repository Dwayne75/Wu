package com.google.common.util.concurrent;

import javax.annotation.Nullable;

public final class SettableFuture<V>
  extends AbstractFuture<V>
{
  public static <V> SettableFuture<V> create()
  {
    return new SettableFuture();
  }
  
  public boolean set(@Nullable V value)
  {
    return super.set(value);
  }
  
  public boolean setException(Throwable throwable)
  {
    return super.setException(throwable);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\SettableFuture.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */